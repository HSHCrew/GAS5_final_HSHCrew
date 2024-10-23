import os
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from sqlalchemy import create_engine, Column, Integer, String, ForeignKey
from sqlalchemy.orm import sessionmaker, declarative_base
from sqlalchemy.exc import NoResultFound
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.ext.declarative import as_declarative
from sqlalchemy.orm import sessionmaker
from dotenv import load_dotenv
import uvicorn
from summarizer import Summarizer

load_dotenv()

app = FastAPI()

# 데이터베이스 연결 정보
DATABASE_URL = os.getenv("DATABASE_URL")  # 환경 변수에서 데이터베이스 URL 가져오기
engine = create_async_engine(DATABASE_URL, echo=True)
SessionLocal = sessionmaker(bind=engine, class_=AsyncSession)
Base = declarative_base()

# SQLAlchemy 모델 정의
@as_declarative()
class Base:
    pass

class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100))

class Medication(Base):
    __tablename__ = "medications"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(100))
    details = Column(String)
    dur_info = Column(String)

class UserMedication(Base):
    __tablename__ = "user_medications"
    user_id = Column(Integer, ForeignKey('users.id'), primary_key=True)
    medication_id = Column(Integer, ForeignKey('medications.id'), primary_key=True)

class MedicationDetails(BaseModel):
    medication_id: int

class UserMedications(BaseModel):
    user_id: int
    medications: list[MedicationDetails]

# CORS 미들웨어를 추가하여 모든 도메인에서의 요청을 허용
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["*"],
)

@app.post("/user/medications")
async def receive_medications(user_medications: UserMedications):
    async with SessionLocal() as db:
        # 사용자 확인
        user_query = await db.execute(select(User).where(User.id == user_medications.user_id))
        user = user_query.scalars().first()
        if not user:
            raise HTTPException(status_code=404, detail="User not found")

        # 약물 정보 저장 및 상세 정보 검색
        medication_details = []
        
        for med in user_medications.medications:
            medication_query = await db.execute(select(Medication).where(Medication.id == med.medication_id))
            medication = medication_query.scalars().first()
            if not medication:
                raise HTTPException(status_code=404, detail=f"Medication with ID {med.medication_id} not found")

            medication_details.append({
                "id": medication.id,
                "name": medication.name,
                "details": medication.details,
                "dur_info": medication.dur_info
            })

        # # 사용자 약물 정보 저장 (선택적)
        # for med in user_medications.medications:
        #     user_medication = UserMedication(user_id=user_medications.user_id, medication_id=med.medication_id)
        #     db.add(user_medication)

        await db.commit()

    return {
        "message": "Medications received successfully",
        "medication_details": medication_details
    }


summarizer = Summarizer()


def main():
    uvicorn.run(app, host="0.0.0.0", port=8000)


if __name__ == "__main__":
    main()