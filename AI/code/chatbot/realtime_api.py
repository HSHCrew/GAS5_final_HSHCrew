import os
import json
import base64
import asyncio
import websockets
from fastapi import FastAPI, WebSocket, Request, Depends
from fastapi.responses import HTMLResponse, JSONResponse
from fastapi.websockets import WebSocketDisconnect
from twilio.twiml.voice_response import VoiceResponse, Connect, Say, Stream
from dotenv import load_dotenv
from twilio.rest import Client
from google.cloud.speech import RecognitionConfig, StreamingRecognitionConfig,StreamingRecognizeRequest
from google.cloud import speech
import queue
import threading
from SpeechClientBridge import SpeechClientBridge
from sqlalchemy.ext.asyncio import AsyncSession
from .database import get_db
from .db_models import VoiceTranscription, User  # db_models에서 import
from .models import VoiceTranscriptionResponse  # Pydantic 모델
from datetime import datetime
from sqlalchemy import select


load_dotenv()
# Configuration
OPENAI_API_KEY = os.getenv('OPENAI_API_KEY') # OPENAI API KEY
PORT = int(os.getenv('PORT', 5050)) # 포트 설정
    
TWILIO_ACCOUNT_SID = os.getenv('TWILIO_ACCOUNT_SID')
TWILIO_AUTH_TOKEN = os.getenv('TWILIO_AUTH_TOKEN')
TWILIO_PHONE_NUMBER = os.getenv('TWILIO_PHONE_NUMBER')  # Twilio 전화번호
EXPERT_PHONE_NUMBER = os.getenv('EXPERT_PHONE_NUMBER')  # 전문가 전화번호
google_credentials_path = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")


SYSTEM_MESSAGE = (
    "Please speak first when the call is connected."
    "당신은 우선 전화를 받고 상대방에게 알타리 서비스의 AI 비서라는 것을 밝힙니다."
) # GPT 모델 초기 입력 프롬프트

VOICE = 'ash' # 보이스 모델 
  
LOG_EVENT_TYPES = [
    'response.content.done', 'rate_limits.updated', 'response.done',
    'input_audio_buffer.committed', 'input_audio_buffer.speech_stopped',
    'input_audio_buffer.speech_started', 'session.created'
] # 이벤트 종류 리스트


app = FastAPI() # FastAPI 인스턴스 생성 (이용할 서버 인스턴스에 맞춰서 엔드포인트들에 넣어주시면 됩니다)


config = RecognitionConfig(
    encoding=RecognitionConfig.AudioEncoding.MULAW,
    sample_rate_hertz=8000,
    language_code="ko-KR",
)
streaming_config = StreamingRecognitionConfig(config=config, interim_results=True)

if not OPENAI_API_KEY: # API 키 누락 예외 처리
    raise ValueError('Missing the OpenAI API key. Please set it in the .env file.')

@app.get("/", response_class=JSONResponse) # Twilio 실행 확인 엔드포인트
async def index_page():
    return {"message": "Twilio Media Stream Server is running!"}

@app.api_route("/incoming-call", methods=["GET", "POST"]) # incoming-call 엔드포인트
# 해당 엔드포인트는 Twilio 계정 전화번호의 Voice Configuration 설정에 
# Webhook to GET: https://b437-115-95-222-202.ngrok-free.app/incoming-call 으로 설정되어 있습니다(수정 불필요)
async def handle_incoming_call(request: Request):
    """Twilio 전화 요청을 수신 후 TwiML 형식의 음성 응답 생성하여 Twilio에게 반환   
       (twilio 음성)
       
       Twilio, 웹 소켓 연결(/media-stream 엔드포인트 사용)"""
    response = VoiceResponse()
    response.say("안녕하세요, 알타리 서비스 입니다. AI 비서 연결을 위해 잠시 기다려주세요", language="ko-KR", voice="Seoyeon")
    response.pause(length=0.5)
    response.say("연결이 완료 되었습니다.", language="ko-KR", voice="Seoyeon")
    host = request.url.hostname 
    connect = Connect() 
    connect.stream(url=f'wss://{host}/media-stream')
    response.append(connect)
    return HTMLResponse(content=str(response), media_type="application/xml")


@app.post("/make-call-to-expert")
async def handle_incoming_call(request: Request):
    """Twilio 전화 요청을 수신 후 TwiML 형식의 음성 응답 생성하여 Twilio에게 반환
        (twilio 음성)
       
        Twilio, 웹 소켓 연결(/media-stream 엔드포인트 사용)
       

        해당 엔드포인트는 Twilio 계정 전화번호의 Voice Configuration 설정에 
        Webhook 부분에 엔드포인트로 넣어야 합니다
        """

    twilio_client = Client(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN)

    call = twilio_client.calls.create(
    to=EXPERT_PHONE_NUMBER,  # 전화 걸 대상
    from_=TWILIO_PHONE_NUMBER,  # Twilio 번호
    url="https://34a3-115-95-222-202.ngrok-free.app/incoming-call"  # TwiML 응답 URL
)
    return "good"

    
@app.websocket("/media-stream") 
async def handle_media_stream(websocket: WebSocket):
    """Twilio <-> OpenAI 를 웹소켓을 이용하여 전송 및 처리 엔드포인트"""
    print("Client connected")
    await websocket.accept()

    bridge = SpeechClientBridge(streaming_config, on_transcription_response)
    bridge_thread = threading.Thread(target=bridge.start)
    bridge_thread.start()
    
    async with websockets.connect( # OpenAI realTimeAPI 연결 
        'wss://api.openai.com/v1/realtime?model=gpt-4o-realtime-preview-2024-10-01',
        extra_headers={
            "Authorization": f"Bearer {OPENAI_API_KEY}",
            "OpenAI-Beta": "realtime=v1"
        }
    ) as openai_ws:
        
        
        await send_session_update(openai_ws) # 설정한 세션 전송
        stream_sid = None # 스트리밍 세션 ID 저장
        
        async def receive_from_twilio():
            """수신한 음성 데이터 처리"""
            nonlocal stream_sid
            try:
                async for message in websocket.iter_text():
                    # print(message)
                    data = json.loads(message)
                    # print("openai_ws",openai_ws)
                    # print("openai_ws.message", openai_ws)
                    if data['event'] == 'media' and openai_ws.open:

                        audio_append = {
                            "type": "input_audio_buffer.append",
                            "audio": data['media']['payload']
                        }
                        chunk = base64.b64decode(data['media']['payload'])
                        bridge.add_request(chunk)
                        # print("data_media_payload:", data['media']['payload'])
                        await openai_ws.send(json.dumps(audio_append))

                    elif data['event'] == 'start':
                        stream_sid = data['start']['streamSid']
                        print(f"Incoming stream has started {stream_sid}")
                # transcript = stream_audio_to_google_cloud(audio_stream)
                # print(f"Transcript from Google Cloud STT: {transcript}")

                        
            except WebSocketDisconnect:
                print("Client disconnected.")

                # Bridge 종료 및 transcription 데이터 반환
                bridge.terminate()
                bridge_thread.join()

                transcriptions = bridge.get_transcriptions()
                
                if transcriptions:
                    try:
                        # 비동기 DB 세션 생성
                        async with get_db() as db:
                            # 현재 통화에 대한 transcription 저장
                            for transcription in transcriptions:
                                await save_transcription(
                                    db=db,
                                    user_id=1,  # 실제 구현시 사용자 ID를 적절히 전달받아야 함
                                    transcription=transcription,
                                    original_message_id=stream_sid  # Twilio 스트림 ID를 원본 메시지 ID로 사용
                                )
                    except Exception as e:
                        print(f"Error saving transcriptions: {e}")
                
                if openai_ws.open:
                    await openai_ws.close()

                    
                

        async def send_to_twilio():
            """OpenAI에서 받은 음성 Twilio로 전송"""
            nonlocal stream_sid
            try:
                async for openai_message in openai_ws:
                    response = json.loads(openai_message)
                    if response['type'] in LOG_EVENT_TYPES:
                        print(f"Received event: {response['type']}", response)
                    if response['type'] == 'session.updated':
                        print("Session updated successfully:", response)
                    if response['type'] == 'response.audio.delta' and response.get('delta'):
                        # Audio from OpenAI
                        try:
                            audio_payload = base64.b64encode(base64.b64decode(response['delta'])).decode('utf-8')
                            audio_delta = {
                                "event": "media",
                                "streamSid": stream_sid,
                                "media": {
                                    "payload": audio_payload
                                }
                            }
                            await websocket.send_json(audio_delta)
                        except Exception as e:
                            print(f"Error processing audio data: {e}")
            except Exception as e:
                print(f"Error in send_to_twilio: {e}")

        await asyncio.gather(receive_from_twilio(), send_to_twilio())

        
        

def on_transcription_response(response):
    if not response.results:
        return

    result = response.results[0]
    if not result.alternatives:
        return

    transcription = result.alternatives[0].transcript
    # print("Transcription: " + transcription)
    return transcription


async def send_session_update(openai_ws):
    """OpenAI 서버로 세션 업데이트 메시지 전송
       설정한 보이스, 초기 프롬프트 전송"""
    session_update = {
        "type": "session.update",
        "session": {
            "turn_detection": {"type": "server_vad"},
            "input_audio_format": "g711_ulaw",
            "output_audio_format": "g711_ulaw",
            "voice": VOICE,
            "instructions": SYSTEM_MESSAGE,
            "modalities": ["text", "audio"],
            "temperature": 0.8,
        }
    }
    print('Sending session update:', json.dumps(session_update))
    await openai_ws.send(json.dumps(session_update))

async def save_transcription(
    db: AsyncSession, 
    user_id: int, 
    transcription: str, 
    original_message_id: str = None
):
    """음성 대화 내용을 데이터베이스에 저장"""
    try:
        db_transcription = VoiceTranscription(
            user_id=user_id,
            transcription=transcription,
            original_message_id=original_message_id
        )
        db.add(db_transcription)
        await db.commit()
        await db.refresh(db_transcription)
        return db_transcription
    except Exception as e:
        await db.rollback()
        print(f"Error saving transcription: {e}")
        raise

# 테스트용 엔드포인트
@app.post("/test/transcription")
async def test_transcription_save(
    db: AsyncSession = Depends(get_db)
):
    """음성 대화 내용 저장 테스트"""
    try:
        # 테스트용 데이터
        test_transcriptions = [
            "안녕하세요, 테스트 메시지입니다.",
            "두 번째 테스트 메시지입니다.",
            "마지막 테스트 메시지입니다."
        ]
        
        saved_records = []
        test_stream_id = f"test_stream_{datetime.now().timestamp()}"
        
        for transcription in test_transcriptions:
            db_transcription = VoiceTranscription(
                user_id=1,
                transcription=transcription,
                original_message_id=test_stream_id
            )
            db.add(db_transcription)
            await db.flush()  # flush 추가
            
            saved_records.append({
                "id": db_transcription.id,
                "transcription": db_transcription.transcription,
                "original_message_id": db_transcription.original_message_id,
                "created_at": db_transcription.created_at.isoformat()
            })
        
        await db.commit()  # 한 번에 커밋
        
        return {
            "status": "success",
            "message": f"Saved {len(saved_records)} transcriptions",
            "saved_records": saved_records
        }
        
    except Exception as e:
        await db.rollback()
        return {
            "status": "error",
            "message": str(e)
        }

# 저장된 데이터 조회 테스트 엔드포인트
@app.get("/test/transcription/{user_id}")
async def get_user_transcriptions(
    user_id: int,
    db: AsyncSession = Depends(get_db)
):
    """특정 사용자의 음성 대화 내용 조회"""
    try:
        result = await db.execute(
            select(VoiceTranscription)
            .where(VoiceTranscription.user_id == user_id)
            .order_by(VoiceTranscription.created_at.desc())
        )
        
        transcriptions = result.scalars().all()
        
        return {
            "status": "success",
            "user_id": user_id,
            "transcriptions": [
                {
                    "id": t.id,
                    "transcription": t.transcription,
                    "original_message_id": t.original_message_id,
                    "created_at": t.created_at.isoformat()
                }
                for t in transcriptions
            ]
        }
        
    except Exception as e:
        return {
            "status": "error",
            "message": str(e)
        }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=PORT)