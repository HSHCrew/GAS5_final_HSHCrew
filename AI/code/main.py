import os
import json
import time
from typing import Dict, List, Optional

from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from sqlalchemy.orm import sessionmaker, declarative_base
from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, func
from redis import asyncio as aioredis
from pydantic import BaseModel
from sqlalchemy import and_
from sqlalchemy import select, delete

# Local imports - 상대 경로로 변경
from summarizer.Summarizer import Summarizer
from chatbot.config import RedisSettings, ChatbotSettings
from chatbot.medication_chatbot import MedicationChatbot
from chatbot.exceptions import (
    ChatbotError,
    ChatbotSessionError,
    MessageError,
    ValidationError,
    StorageError,
    ResourceError
)
from chatbot.database.models import User, Medication, MedicationSummary
from chatbot.models import (
    ChatRequest,
    MedicationItem,
    UserMedicationsRequest
)

app = FastAPI()

# Redis 설정 초기화
redis_settings = RedisSettings()

# Redis 클라이언트 초기화
redis = aioredis.from_url(
    url=redis_settings.REDIS_URL,
    password=redis_settings.REDIS_PASSWORD,
    encoding=redis_settings.REDIS_ENCODING,
    decode_responses=redis_settings.REDIS_DECODE_RESPONSES
)

# ChatbotManager를 별도 파일로 분리하고 import
from chatbot.manager import ChatbotManager

# ChatbotManager 인스턴스 생성
chatbot_manager = ChatbotManager(redis)

# Redis 상태 확인 엔드포인트
@app.get("/redis-health")
async def check_redis():
    try:
        await redis.ping()
        return {
            "status": "healthy",
            "url": redis_settings.REDIS_URL  # URL 확인용
        }
    except Exception as e:
        return {
            "status": "unhealthy", 
            "error": str(e),
            "url": redis_settings.REDIS_URL  # 문제 발생시 URL 확인
        }

# 데이터베이스 연결 정보
DATABASE_URL = "sqlite+aiosqlite:///./test.db"

# 데이터베이스 엔진 생성
engine = create_async_engine(DATABASE_URL, echo=True)
SessionLocal = sessionmaker(
    bind=engine,
    class_=AsyncSession,
    expire_on_commit=False
)
Base = declarative_base()

# 데이터베이스 세션 관리를 위한 의존성 함수 추가
async def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        await db.close()

# SQLAlchemy 모델 정의
class User(Base):
    __tablename__ = "users"
    
    id = Column(Integer, primary_key=True)
    name = Column(String(50))

class Medication(Base):
    __tablename__ = "medications"
    
    id = Column(Integer, primary_key=True)
    name = Column(String(100))
    details = Column(String)  # JSON 형식의 상세 정보
    dur_info = Column(String)  # JSON 형식의 DUR 정보

class UserMedication(Base):
    __tablename__ = "user_medications"
    
    id = Column(Integer, primary_key=True)
    user_id = Column(Integer, ForeignKey('users.id'))
    medication_id = Column(Integer, ForeignKey('medications.id'))

class MedicationSummary(Base):
    __tablename__ = "medication_summaries"
    
    user_id = Column(Integer, ForeignKey('users.id'), primary_key=True)
    medication_id = Column(Integer, ForeignKey('medications.id'), primary_key=True)
    index = Column(Integer, nullable=True)
    restructured = Column(String)
    summary = Column(String)
    fewshots = Column(String)
    failed = Column(String)
    last_updated = Column(DateTime, server_default=func.now(), onupdate=func.now())
    

# CORS 미들웨어를 추가하여 모든 도메인에서의 요청을 허용
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["*"],
)


class MedicationItem(BaseModel):
    medication_id: int

class UserMedicationsRequest(BaseModel):
    user_id: int
    medications: list[MedicationItem]

    class Config:
        from_attributes = True

# Add the ChatRequest model here
class ChatRequest(BaseModel):
    user_id: int
    message: str = None
    user_info: str = None
    medication_info: list[str] = None
    
    # 추가 필요한 검증
    class Config:
        min_length_message = 1
        max_length_message = 1000

@app.post("/user/medications")
async def receive_medications(
    user_medications: UserMedicationsRequest,  # Pydantic 모델 사용
    db: AsyncSession = Depends(get_db)
):
    try:
        # 사용자 확인
        user_query = await db.execute(
            select(User).where(User.id == user_medications.user_id)
        )
        user = user_query.scalar_one_or_none()
        if not user:
            raise HTTPException(status_code=404, detail="User not found")

        # 기존 사용자-약물 관계 삭제
        await db.execute(
            delete(UserMedication).where(UserMedication.user_id == user_medications.user_id)
        )

        # 새로운 사용자-약물 관계 추가
        for med in user_medications.medications:
            new_user_med = UserMedication(
                user_id=user_medications.user_id,
                medication_id=med.medication_id
            )
            db.add(new_user_med)
        
        await db.commit()

        # 약물 정보 조회 및 구조화
        medication_details = {}
        for med in user_medications.medications:
            med_query = await db.execute(
                select(Medication).where(Medication.id == med.medication_id)
            )
            medication = med_query.scalar_one_or_none()
            if medication:
                # JSON 문자열을 파이썬 딕셔너리로 변환
                details_dict = json.loads(medication.details)
                dur_dict = json.loads(medication.dur_info)
                
                # 약물 정보를 구조화된 형태로 저장
                medication_details[medication.id] = str({
                    "약물정보": {
                        "약물명": medication.name,
                        **details_dict
                    },
                    "DUR정보": dur_dict
                })

        # Summarizer 처리 및 DB 저장
        summarizer = Summarizer()
        med_info_list = list(medication_details.values())
        summary_results = await summarizer.process_medication_infos(
            contents=med_info_list,
            user_id=user_medications.user_id,
            medication_ids=[med.medication_id for med in user_medications.medications]
        )

        # 요약 결과 저장
        async with AsyncSession(engine) as summary_session:
            try:
                for med_id, result in zip(medication_details.keys(), summary_results):
                    # 기존 요약 확인
                    summary_query = await summary_session.execute(
                        select(MedicationSummary).where(
                            and_(
                                MedicationSummary.user_id == user_medications.user_id,
                                MedicationSummary.medication_id == med_id
                            )
                        )
                    )
                    existing_summary = summary_query.scalar_one_or_none()

                    summary_data = {
                        'index': result.index,
                        'restructured': result.restructured,
                        'summary': result.summary,
                        'fewshots': result.fewshots,
                        'failed': str(result.failed)
                    }

                    if existing_summary:
                        # 기존 요약 업데이트
                        for key, value in summary_data.items():
                            setattr(existing_summary, key, value)
                        print(f"Updated summary for user {user_medications.user_id}, medication {med_id}")
                    else:
                        # 새 요약 추가
                        new_summary = MedicationSummary(
                            user_id=user_medications.user_id,
                            medication_id=med_id,
                            **summary_data
                        )
                        summary_session.add(new_summary)
                        print(f"Added new summary for user {user_medications.user_id}, medication {med_id}")

                await summary_session.commit()
                print(f"Successfully saved all summaries for user {user_medications.user_id}")

            except Exception as e:
                await summary_session.rollback()
                print(f"Error saving summaries to database: {str(e)}")
                raise

        return {
            "message": "Medications processed successfully",
            "user_id": user_medications.user_id,
            "medication_details": medication_details,
            "summaries": {
                "user_id": user_medications.user_id,
                "medications": [
                    {
                        "medication_id": med_id,
                        "index": result.index,
                        "restructured": result.restructured,
                        "summary": result.summary,
                        "fewshots": result.fewshots,
                        "failed": result.failed
                    } for med_id, result in zip(medication_details.keys(), summary_results)
                ]
            }
        }

    except Exception as e:
        await db.rollback()
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/user/{user_id}/medication-summaries")
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

@app.post('/user/medications/chat_message')
async def chat_message(
    request: ChatRequest,
    db: AsyncSession = Depends(get_db)
):
    try:
        chatbot = await chatbot_manager.get_chatbot(request.user_id, db)
        response = await chatbot.respond(request.message)
        return {"response": response}
    except ChatbotSessionError as e:
        raise HTTPException(status_code=400, detail=str(e))
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post('/user/medications/chat_start')
async def chat_start(
    request: ChatRequest,
    db: AsyncSession = Depends(get_db)
):
    try:
        chatbot = await chatbot_manager.get_chatbot(request.user_id, db)
        initial_response = await chatbot.start_chat()
        return {"message": initial_response, "user_id": request.user_id}
    except Exception as e:
        # ChatbotManager 내부에서 Redis 관련 오류를 포함한 모든 오류 처리
        raise HTTPException(status_code=500, detail=str(e))

@app.post('/user/medications/chat_reset')
async def chat_reset(
    request: ChatRequest,
    db: AsyncSession = Depends(get_db)
):
    try:
        # 기존 세션 및 대화 기록 삭제
        session_key = f"chatbot:session:{request.user_id}"
        chat_key = f"chat:history:{request.user_id}"
        
        # Redis에서 세션 및 대화 기록 삭제
        await redis.delete(session_key, chat_key)
        
        # ChatbotManager에서 인스턴스 제거
        if request.user_id in chatbot_manager.instances:
            del chatbot_manager.instances[request.user_id]
        
        # 새로운 세션 시작
        chatbot = await chatbot_manager.get_chatbot(request.user_id, db)
        initial_response = await chatbot.start_chat()
        
        return {
            "message": "Chat session reset successfully",
            "initial_response": initial_response
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.on_event("startup")
async def startup_event():
    # Redis 연결 확인
    try:
        await redis.ping()
    except Exception as e:
        raise Exception(f"Redis connection failed: {str(e)}")

@app.on_event("shutdown")
async def shutdown_event():
    # 리소스 정리
    for instance in chatbot_manager.instances.values():
        await instance.cleanup()
    await redis.close()

def main():
    uvicorn.run(app, host="0.0.0.0", port=8000)


if __name__ == "__main__":
    main()