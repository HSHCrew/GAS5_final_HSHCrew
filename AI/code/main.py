import os
from typing import Dict, List, Optional
from dotenv import load_dotenv
from fastapi import FastAPI, HTTPException, Depends, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from redis import asyncio as aioredis
from sqlalchemy import select, delete, and_, update, text
from sqlalchemy import func
import json
from contextlib import asynccontextmanager
from fastapi.responses import StreamingResponse
import asyncio
from datetime import datetime
from pydantic import BaseModel

# Local imports
from summarizer.Summarizer import Summarizer
from chatbot.config import RedisSettings, ChatbotSettings
from chatbot.chatbot_manager import ChatbotManager
from chatbot.exceptions import ChatbotSessionError
from summarizer.config import DatabaseSettings

from chatbot.models import ChatRequest, UserMedicationsRequest
from chatbot.database import (
    AsyncSessionLocal,
    get_db,
    Base
)
from chatbot.database.models import (
    UserProfile,
    Medication,
    UserMedication,
    MedicationSummary
)
from chatbot.db_models import VoiceTranscription

# 환경 변수 로드

load_dotenv()
DB_NAME = os.getenv('DB_NAME', 'altari')  # 기본값으로 'altari' 설정

# 데이터베이스 설정 초기화
db_settings = DatabaseSettings()

# engine 초기화
engine = create_async_engine(
    db_settings.DATABASE_URL,
    echo=True,
    pool_pre_ping=True,
    pool_size=5,
    max_overflow=10
)

@asynccontextmanager
async def lifespan(app: FastAPI):
    try:
        print(f"Connecting to database: {db_settings.DATABASE_URL}")
        async with engine.begin() as conn:
            await conn.run_sync(Base.metadata.create_all)
        print("Database connection successful")
        yield
    except Exception as e:
        print(f"Startup failed: {str(e)}")
        raise
    finally:
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
@app.post('/user/{user_profile_id}/medications/chat/message')
async def chat_message(
    user_profile_id: int,
    request: ChatRequest,
    background_tasks: BackgroundTasks,
    db: AsyncSession = Depends(get_db)
):
    try:
        chatbot = await chatbot_manager.get_chatbot(user_profile_id, db)
        
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
        chatbot = await chatbot_manager.get_chatbot(request.user_profile_id, db)
        initial_response = await chatbot.start_chat()
        return {"message": initial_response, "user_profile_id": request.user_profile_id}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post('/user/medications/chat/reset')
async def chat_reset(
    request: ChatRequest,
    db: AsyncSession = Depends(get_db)
):
    try:
        print(f"\n[DEBUG] Starting chat reset endpoint for user {request.user_profile_id}")
        
        # 리셋 전 세션 상태 확인
        session_before = await chatbot_manager.session_service.get_session(request.user_profile_id)
        print(f"[DEBUG] Session exists before reset: {session_before is not None}")
        
        # ChatbotManager의 reset_chat 메서드를 사용하여 세션 초기화
        new_chatbot = await chatbot_manager.reset_chat(request.user_profile_id, db)
        
        # 리셋 후 세션 상태 확인
        session_after = await chatbot_manager.session_service.get_session(request.user_profile_id)
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
            # user_medication 테이블에서 medication_ids 조회
            user_meds_query = await db.execute(
                select(UserMedication.medication_id)
                .where(UserMedication.user_profile_id == user_medications.user_profile_id)
            )
            medication_ids = [row[0] for row in user_meds_query.all()]

            if not medication_ids:
                raise HTTPException(
                    status_code=404,
                    detail=f"No medications found for user {user_medications.user_profile_id}"
                )

            # medication 테이블에서 상세 정보 조회
            medications_query = await db.execute(
                select(
                    Medication.medication_id,
                    Medication.medication_name,
                    Medication.medication_caution_info,
                    Medication.medication_storage_method_info,
                    Medication.medication_interaction_info,
                    Medication.medication_caution_warning_info,
                    Medication.medication_efficacy_info,
                    Medication.medication_item_dur,
                    Medication.medication_se_info,
                    Medication.medication_use_info,
                    Medication.taking_info,
                    Medication.ingredient
                ).where(Medication.medication_id.in_(medication_ids))
            )
            medications = medications_query.all()

            # 약물 정보를 medication_id를 키로 하는 딕셔너리로 변환
            medication_details = {}
            for med in medications:
                medication_details[med.medication_id] = str({
                    "약물정보": {
                        "약물명": med.medication_name,
                        "주의사항": med.medication_caution_info,
                        "보관방법": med.medication_storage_method_info,
                        "상호작용": med.medication_interaction_info,
                        "경고": med.medication_caution_warning_info,
                        "효능": med.medication_efficacy_info,
                        "DUR정보": med.medication_item_dur,
                        "부작용": med.medication_se_info,
                        "사용법": med.medication_use_info,
                        "복용방법": med.taking_info,
                        "성분": med.ingredient
                    }
                })

            # Summarizer 처리를 위한 리스트 생성
            med_info_list = [medication_details[med_id] for med_id in medication_ids]
            
            # Summarizer 처리
            summary_results = await summarizer.process_medication_infos(
                contents=med_info_list,
                user_id=str(user_medications.user_profile_id),
                medication_ids=medication_ids
            )
            
            # 요약 결과 저장 (upsert 방식)
            for med_id, result in zip(medication_ids, summary_results):
                # 기존 요약 확인
                existing_summary = await db.execute(
                    select(MedicationSummary).where(
                        MedicationSummary.user_profile_id == user_medications.user_profile_id,
                        MedicationSummary.medication_id == med_id
                    )
                )
                summary = existing_summary.scalar_one_or_none()

                if summary:
                    # 기존 요약 업데이트
                    await db.execute(
                        update(MedicationSummary)
                        .where(
                            MedicationSummary.user_profile_id == user_medications.user_profile_id,
                            MedicationSummary.medication_id == med_id
                        )
                        .values(
                            restructured=result.restructured,
                            summary=result.summary,
                            fewshots=result.fewshots,
                            failed=str(result.failed),
                            medication_summary_updated_at=func.now()
                        )
                    )
                else:
                    # 새 요약 추가
                    new_summary = MedicationSummary(
                        user_profile_id=user_medications.user_profile_id,
                        medication_id=med_id,
                        restructured=result.restructured,
                        summary=result.summary,
                        fewshots=result.fewshots,
                        failed=str(result.failed)
                    )
                    db.add(new_summary)

            await db.commit()
            
        return {"message": "Medications processed and summaries saved successfully"}

    except HTTPException as he:
        raise he
    except Exception as e:
        print(f"Error processing medications: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))
    
    
from dataclasses import dataclass
from typing import Optional, Any
@dataclass
class TestProcessResult:
   restructured: str
   summary: str
   fewshots: str
   failed: Any
   index: Optional[int] = None
@app.post("/test/medication-summary")
async def test_save_medication_summary(
   user_profile_id: int,
   medication_id: int,
   db: AsyncSession = Depends(get_db)
):
   try:
       # 테스트용 요약 결과 생성
       test_result = TestProcessResult(
           restructured="""```markdown
 알테렌정 (Medicine Name)
## 기본 정보 (Basic Information)
**효능**: 위염 증상 개선
**주요 적응증**: 급성위염, 만성위염
```
""",
           summary="💊 알테렌정은 위염과 같은 위장 질환의 증상을 완화하는 데 사용됩니다...",
           fewshots="Previous Summary (Attempt 1):\n💊 약품 개요\n알테렌정은...",
           failed=False
       )    
       async with db.begin():
           # 기존 요약 확인
            existing_summary = await db.execute(
               select(MedicationSummary).where(
                   MedicationSummary.user_profile_id == user_profile_id,
                   MedicationSummary.medication_id == medication_id
                )
            )
            summary = existing_summary.scalar_one_or_none()
            if summary:
                # 기존 요약 업데이트
               await db.execute(
                   update(MedicationSummary)
                   .where(
                    MedicationSummary.user_profile_id == user_profile_id,
                        MedicationSummary.medication_id == medication_id
                    )
                    .values(
                        restructured=test_result.restructured,
                        summary=test_result.summary,
                        fewshots=test_result.fewshots,
                        failed=str(test_result.failed),
                        medication_summary_updated_at=func.now()
                    )
                )
            else:
                # 새 요약 추가
                new_summary = MedicationSummary(
                    user_profile_id=user_profile_id,
                    medication_id=medication_id,
                    restructured=test_result.restructured,
                    summary=test_result.summary,
                    fewshots=test_result.fewshots,
                    failed=str(test_result.failed)
                )
                db.add(new_summary)
            await db.commit()
            return {
                "status": "success",
                "message": "Test summary saved successfully",
                "data": {
                "user_profile_id": user_profile_id,
                "medication_id": medication_id,
                "action": "updated" if summary else "created"
                }
            }
   except Exception as e:
        print(f"Error saving test summary: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

    
@app.get("/user/{user_profile_id}/medication/summaries")
async def get_user_medication_summaries(
    user_profile_id: int,
    db: AsyncSession = Depends(get_db)
) -> Dict[str, List[str]]:
    try:
        # 사용자의 모든 약물 요약 정보 조회
        query = select(MedicationSummary).where(MedicationSummary.user_profile_id == user_profile_id)
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
@app.get('/user/{user_profile_id}/medications/chat/follow-up')
async def get_follow_up(
    user_profile_id: int,
    last_checked_timestamp: Optional[float] = None,
    db: AsyncSession = Depends(get_db)
):
    try:
        # 가장 최근의 후속 메시지 조회
        chatbot = await chatbot_manager.get_chatbot(user_profile_id, db)
        messages = await chatbot.chat_service.get_chat_history(user_profile_id)
        
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
@app.post('/user/{user_profile_id}/medications/chat/message/stream')
async def chat_message_stream(
    user_profile_id: int,
    request: ChatRequest,
    background_tasks: BackgroundTasks,
    db: AsyncSession = Depends(get_db)
):
    try:
        chatbot = await chatbot_manager.get_chatbot(user_profile_id, db)
        
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

@app.get("/test/transcription/{user_profile_id}")
async def get_user_transcriptions(
    user_profile_id: int,
    db: AsyncSession = Depends(get_db)
):
    """특정 사용자의 음성 대화 내용 조회"""
    try:
        result = await db.execute(
            select(VoiceTranscription)
            .where(VoiceTranscription.user_profile_id == user_profile_id)
            .order_by(VoiceTranscription.created_at.desc())
        )
        
        transcriptions = result.scalars().all()
        
        return {
            "status": "success",
            "user_profile_id": user_profile_id,
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

@app.get("/health/database")
async def health_check(db: AsyncSession = Depends(get_db)):
    try:
        async with db.begin():
            await db.execute("SELECT 1")
        return {"status": "healthy"}
    except Exception as e:
        return {"status": "unhealthy", "error": str(e)}

@app.get("/user/{user_profile_id}/medications/check")
async def check_user_medications(
    user_profile_id: int,
    db: AsyncSession = Depends(get_db)
):
    try:
        # user_profile 존재 여부 확인
        user_profile_query = await db.execute(
            select(UserProfile).where(UserProfile.user_profile_id == user_profile_id)
        )
        user_profile = user_profile_query.scalar_one_or_none()
        
        if not user_profile:
            return {
                "exists": False,
                "message": f"User profile {user_profile_id} not found"
            }

        # user_medication 데이터 확인
        user_meds_query = await db.execute(
            select(
                UserMedication.user_medication_id,
                UserMedication.medication_id
            ).where(UserMedication.user_profile_id == user_profile_id)
        )
        medications = user_meds_query.all()
        
        return {
            "exists": True,
            "user_profile_id": user_profile_id,
            "medication_count": len(medications),
            "medications": [
                {
                    "user_medication_id": med.user_medication_id,
                    "medication_id": med.medication_id
                } for med in medications
            ]
        }
        
    except Exception as e:
        print(f"Error checking medications: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/test/user-medication")
async def create_test_data(
    user_profile_id: int,
    medication_id: int,
    db: AsyncSession = Depends(get_db)
):
    try:
        # user_profile이 없다면 생성
        user_profile_query = await db.execute(
            select(UserProfile).where(UserProfile.user_profile_id == user_profile_id)
        )
        user_profile = user_profile_query.scalar_one_or_none()
        
        if not user_profile:
            user_profile = UserProfile(user_profile_id=user_profile_id)
            db.add(user_profile)
        
        # user_medication 생성
        user_medication = UserMedication(
            user_profile_id=user_profile_id,
            medication_id=medication_id
        )
        db.add(user_medication)
        await db.commit()
        
        return {
            "status": "success",
            "message": f"Created user_medication for user {user_profile_id} with medication {medication_id}"
        }
        
    except Exception as e:
        await db.rollback()
        print(f"Error creating test data: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/test/check-table-charset")
async def check_table_charset(db: AsyncSession = Depends(get_db)):
    try:
        async with db.begin():
            # 테이블 정보 조회
            result = await db.execute(text(f"""
                SHOW TABLE STATUS 
                FROM {DB_NAME} 
                WHERE Name = 'medication_summary';
            """))
            table_info = result.first()
            
            if not table_info:
                return {
                    "status": "error",
                    "message": f"Table 'medication_summary' not found in database '{DB_NAME}'"
                }
            
            # 컬럼 정보 조회
            result = await db.execute(text(f"""
                SHOW FULL COLUMNS 
                FROM medication_summary;
            """))
            column_info = result.fetchall()
            
            return {
                "table": {
                    "name": table_info.Name,
                    "collation": table_info.Collation
                },
                "columns": [
                    {
                        "name": col.Field,
                        "type": col.Type,
                        "collation": col.Collation
                    } for col in column_info
                    if col.Collation is not None  # TEXT, VARCHAR 등의 문자열 컬럼만 필터링
                ]
            }
    except Exception as e:
        print(f"Error checking table charset: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/test/update-table-charset")
async def update_table_charset(db: AsyncSession = Depends(get_db)):
    try:
        async with db.begin():
            # 테이블 문자셋 변경
            await db.execute(text("""
                ALTER TABLE medication_summary 
                CONVERT TO CHARACTER SET utf8mb4 
                COLLATE utf8mb4_unicode_ci;
            """))
            
            # 각 TEXT 컬럼의 문자셋 변경
            for column in ['failed', 'fewshots', 'restructured', 'summary']:
                await db.execute(text(f"""
                    ALTER TABLE medication_summary 
                    MODIFY COLUMN {column} TEXT 
                    CHARACTER SET utf8mb4 
                    COLLATE utf8mb4_unicode_ci;
                """))
            
            return {"message": "Table charset updated successfully"}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

def main():
    uvicorn.run(app, host="0.0.0.0", port=8000)

if __name__ == "__main__":
    main()