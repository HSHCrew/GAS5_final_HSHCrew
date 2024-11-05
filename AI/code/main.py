import os
from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from sqlalchemy import Column, Integer, String, ForeignKey, select, delete
from sqlalchemy.orm import sessionmaker, declarative_base
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.ext.declarative import as_declarative
import asyncio
from dotenv import load_dotenv
import uvicorn
import json
from summarizer.Summarizer import Summarizer
from chatbot.medication_chatbot import MedicationChatbot
from sqlalchemy import DateTime, func
from sqlalchemy import and_

load_dotenv()

app = FastAPI()

# 데이터베이스 연결 정보
DATABASE_URL = os.getenv("DATABASE_URL")  # 환경 변수에서 데이터베이스 URL 가져오기
# 테스트를 위한 데이터베이스 설정
DATABASE_URL = "sqlite+aiosqlite:///./test.db"  # 테스트용 SQLite 데이터베이스
engine = create_async_engine(DATABASE_URL, echo=True)
SessionLocal = sessionmaker(bind=engine, class_=AsyncSession)
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
        summary_results = await summarizer.mono_processes(med_info_list)

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


class temp_UserMedications(BaseModel):
    user_id: int
    user_info : str
    medication_info: list[str]
    
class temp_Message(BaseModel):
    user_id: int
    message: str

chatbot_instances = {}

@app.post('/user/medications/chat_start')
async def chat_start(user: temp_UserMedications):
    try:
        chatbot = MedicationChatbot(user)
        chatbot_instances[user.user_id] = chatbot 
        initial_response = await chatbot.start_chat()
        return {"message": initial_response, "user_id": user.user_id}
    except Exception as e:
        raise HTTPException(status_code=400, detail=str(e))

@app.post('/user/medications/chat_message')
async def chat_message(request: temp_Message):
    user_id = request.user_id
    message = request.message

    if user_id not in chatbot_instances:
        raise HTTPException(status_code=404, detail="챗봇 인스턴스가 존재하지 않습니다. 먼저 /chat_start를 호출하세요.")

    chatbot = chatbot_instances[user_id]  # 사용자 ID로 챗봇 인스턴스를 불러옴
    response = await chatbot.respond(message)  # 비동기 호출
    
    return {"response": response}
    


def main():
    uvicorn.run(app, host="0.0.0.0", port=8000)


if __name__ == "__main__":
    asyncio.run(main())