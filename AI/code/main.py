import os
from typing import Dict, List, Optional
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException, Depends, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.ext.asyncio import AsyncSession
from redis import asyncio as aioredis
from sqlalchemy import select, delete, and_, update
from sqlalchemy import func
import json
from contextlib import asynccontextmanager
from fastapi.responses import StreamingResponse
import asyncio
from datetime import datetime

# Local imports
from summarizer.Summarizer import Summarizer
from chatbot.config import RedisSettings, ChatbotSettings
from chatbot.chatbot_manager import ChatbotManager
from chatbot.exceptions import ChatbotSessionError

from chatbot.models import ChatRequest, UserMedicationsRequest
from chatbot.database import (
    engine,
    AsyncSessionLocal,
    get_db,
    Base
)
from chatbot.database.models import (
    User,
    Medication,
    UserMedication,
    MedicationSummary
)
from chatbot.db_models import VoiceTranscription

# 환경 변수 로드

load_dotenv()

@asynccontextmanager
async def lifespan(app: FastAPI):
    # startup 이벤트
    try:
        await redis.ping()
        async with engine.begin() as conn:
            await conn.run_sync(Base.metadata.create_all)
        print("Application startup completed")
        yield
    except Exception as e:
        print(f"Startup failed: {str(e)}")
        raise
    finally:
        # shutdown 이벤트
        # 챗봇 인스턴스 정리
        # await chatbot_manager.cleanup_all()
        await redis.close()
        await engine.dispose()
        print("Application shutdown completed")

# FastAPI 인스턴스 생성 시 lifespan 핸들러 등록
app = FastAPI(lifespan=lifespan)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["*"],
)

# Redis 설정 및 초기화
redis_settings = RedisSettings()
redis = aioredis.from_url(
    url=redis_settings.REDIS_URL,
    encoding=redis_settings.REDIS_ENCODING,
    decode_responses=redis_settings.REDIS_DECODE_RESPONSES
)

# 서비스 초기화
chatbot_manager = ChatbotManager(redis)
summarizer = Summarizer()

# 헬스체크 엔드포인트
@app.get("/redis/health")
async def redis_health_check():
    try:
        await redis.ping()
        async with AsyncSessionLocal() as db:
            await db.execute("SELECT 1")
        return {"status": "healthy"}
    except Exception as e:
        return {"status": "unhealthy", "error": str(e)}

# 챗봇 엔드포인트
@app.post('/user/{user_id}/medications/chat/message')
async def chat_message(
    user_id: int,
    request: ChatRequest,
    background_tasks: BackgroundTasks,
    db: AsyncSession = Depends(get_db)
):
    try:
        chatbot = await chatbot_manager.get_chatbot(user_id, db)
        
        # 첫 번째 응답 즉시 생성
        main_response = await chatbot.respond(request.message)
        
        # 후속 메시지 생성을 백그라운드 작업으로 예약
        background_tasks.add_task(
            chatbot.generate_follow_up,
            request.message,
            main_response
        )
        
        return {
            "response": main_response,
            "has_follow_up": True  # 프론트엔드에 후속 메시지가 올 것임을 알림
        }
        
    except ChatbotSessionError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post('/user/medications/chat/start')
async def chat_start(
    request: ChatRequest,
    db: AsyncSession = Depends(get_db)
):
    try:
        chatbot = await chatbot_manager.get_chatbot(request.user_id, db)
        initial_response = await chatbot.start_chat()
        return {"message": initial_response, "user_id": request.user_id}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post('/user/medications/chat/reset')
async def chat_reset(
    request: ChatRequest,
    db: AsyncSession = Depends(get_db)
):
    try:
        print(f"\n[DEBUG] Starting chat reset endpoint for user {request.user_id}")
        
        # 리셋 전 세션 상태 확인
        session_before = await chatbot_manager.session_service.get_session(request.user_id)
        print(f"[DEBUG] Session exists before reset: {session_before is not None}")
        
        # ChatbotManager의 reset_chat 메서드를 사용하여 세션 초기화
        new_chatbot = await chatbot_manager.reset_chat(request.user_id, db)
        
        # 리셋 후 세션 상태 확인
        session_after = await chatbot_manager.session_service.get_session(request.user_id)
        print(f"[DEBUG] Session exists after reset: {session_after is not None}")
        
        # 초기 응답 받기
        initial_response = await new_chatbot.start_chat()
        
        return {
            "message": "Chat session reset successfully",
            "initial_response": initial_response
        }
    except Exception as e:
        print(f"Error resetting chat: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

# 약물 정보 처리 엔드포인트
@app.post("/user/medications")
async def receive_medications(
    user_medications: UserMedicationsRequest,
    db: AsyncSession = Depends(get_db)
):
    try:
        async with db.begin():
            # 사용자 확인
            user_query = await db.execute(
                select(User).where(User.id == user_medications.user_id)
            )
            user = user_query.scalar_one_or_none()
            if not user:
                raise HTTPException(status_code=404, detail="User not found")

            # 기존 사용자-약물 관계 삭제 및 새로운 관계 추가
            await db.execute(
                delete(UserMedication).where(UserMedication.user_id == user_medications.user_id)
            )
            for med in user_medications.medications:
                new_user_med = UserMedication(
                    user_id=user_medications.user_id,
                    medication_id=med.medication_id
                )
                db.add(new_user_med)

            # 약물 정보 조회 및 요약 처리
            # 약물 정보 조회 및 구조화
            medication_details = {}
            for med in user_medications.medications:
                med_query = await db.execute(
                    select(Medication).where(Medication.id == med.medication_id)
                )
                medication = med_query.scalar_one_or_none()
                if medication:
                    details_dict = json.loads(medication.details)
                    dur_dict = json.loads(medication.dur_info)
                    
                    medication_details[medication.id] = str({
                        "약물정보": {
                            "약물명": medication.name,
                            **details_dict
                        },
                        "DUR정보": dur_dict
                    })

            # Summarizer 처리
            med_info_list = list(medication_details.values())
            summary_results = await summarizer.process_medication_infos(
                contents=med_info_list,
                user_id=str(user_medications.user_id),
                medication_ids=[med.medication_id for med in user_medications.medications]
            )

            # 요약 결과 저장 (upsert 방식)
            for med_id, result in zip(medication_details.keys(), summary_results):
                # 기존 요약 확인
                existing_summary = await db.execute(
                    select(MedicationSummary).where(
                        MedicationSummary.user_id == user_medications.user_id,
                        MedicationSummary.medication_id == med_id
                    )
                )
                summary = existing_summary.scalar_one_or_none()

                if summary:
                    # 기존 요약 업데이트
                    await db.execute(
                        update(MedicationSummary)
                        .where(
                            MedicationSummary.user_id == user_medications.user_id,
                            MedicationSummary.medication_id == med_id
                        )
                        .values(
                            restructured=result.restructured,
                            summary=result.summary,
                            fewshots=result.fewshots,
                            failed=str(result.failed),
                            last_updated=func.now()
                        )
                    )
                else:
                    # 새 요약 추가
                    new_summary = MedicationSummary(
                        user_id=user_medications.user_id,
                        medication_id=med_id,
                        restructured=result.restructured,
                        summary=result.summary,
                        fewshots=result.fewshots,
                        failed=str(result.failed)
                    )
                    db.add(new_summary)

        return {"message": "Medications processed successfully"}

    except Exception as e:
        print(f"Error processing medications: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))
    
@app.get("/user/{user_id}/medication/summaries")
async def get_user_medication_summaries(
    user_id: int,
    db: AsyncSession = Depends(get_db)
) -> Dict[str, List[str]]:
    try:
        # 사용자의 모든 약물 요약 정보 조회
        query = select(MedicationSummary).where(MedicationSummary.user_id == user_id)
        result = await db.execute(query)
        summaries = result.all()
        
        if not summaries:
            raise HTTPException(status_code=404, detail="No medication summaries found for this user")
        
        # restructured 정보만 추출
        medication_info = [summary[0].restructured for summary in summaries]
        
        return {"medication_info": medication_info}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 후속 메시지를 조회하는 새로운 엔드포인트 추가
@app.get('/user/{user_id}/medications/chat/follow-up')
async def get_follow_up(
    user_id: int,
    last_checked_timestamp: Optional[float] = None,
    db: AsyncSession = Depends(get_db)
):
    try:
        # 가장 최근의 후속 메시지 조회
        chatbot = await chatbot_manager.get_chatbot(user_id, db)
        messages = await chatbot.chat_service.get_chat_history(user_id)
        
        # 백그라운드 작업 상태 확인
        if await chatbot.is_processing_follow_up():
            return {
                "follow_up": None,
                "status": "processing"
            }
            
        # 마지막 메시지가 assistant의 메시지이고, 
        # 그 이전 메시지도 assistant의 메시지인 경우를 후속 메시지로 간주
        if len(messages) >= 2 and \
           messages[-1].role == "assistant" and \
           messages[-2].role == "assistant":
            return {
                "follow_up": messages[-1].content,
                "status": "completed"
            }
            
        return {
            "follow_up": None,
            "status": "completed"  # 후속 메시지가 없음이 확인됨
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 챗봇 스트리밍 엔드포인트
@app.post('/user/{user_id}/medications/chat/message/stream')
async def chat_message_stream(
    user_id: int,
    request: ChatRequest,
    background_tasks: BackgroundTasks,
    db: AsyncSession = Depends(get_db)
):
    try:
        chatbot = await chatbot_manager.get_chatbot(user_id, db)
        
        async def generate():
            async for token in chatbot.respond_stream(request.message):
                yield f"data: {json.dumps({'token': token})}\n\n"
                
        # 후속 메시지 생성을 백그라운드 작업으로 예약
        background_tasks.add_task(
            chatbot.generate_follow_up,
            request.message,
            ""  # 전체 응답은 스트리밍 완료 후 저장됨
        )
        
        return StreamingResponse(
            generate(),
            media_type="text/event-stream"
        )
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

# 테스트용 엔드포인트 추가
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
                user_id=1,  # 테스트용 사용자 ID
                transcription=transcription,
                original_message_id=test_stream_id
            )
            db.add(db_transcription)
            await db.commit()
            await db.refresh(db_transcription)
            
            saved_records.append({
                "id": db_transcription.id,
                "transcription": db_transcription.transcription,
                "original_message_id": db_transcription.original_message_id,
                "created_at": db_transcription.created_at.isoformat()
            })
        
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

def main():
    uvicorn.run(app, host="0.0.0.0", port=8000)

if __name__ == "__main__":
    main()