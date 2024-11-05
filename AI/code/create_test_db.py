import asyncio
import json
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import sessionmaker
from main import Base, User, Medication, UserMedication, MedicationSummary

# SQLite 데이터베이스 URL
DATABASE_URL = "sqlite+aiosqlite:///./test.db"

# 테스트용 샘플 데이터
sample_users = [
    {"id": 1, "name": "김환자"},
    {"id": 2, "name": "이환자"}
]

sample_medications = [
    {
        "id": 1,
        "name": "타이레놀",
        "details": json.dumps({
            "성분": "아세트아미노펜",
            "용법용량": "1회 1~2정씩 1일 3-4회 필요시 복용",
            "효능효과": "감기로 인한 발열 및 동통(통증), 두통, 신경통, 근육통, 월경통, 염좌통(삔 통증)"
        }),
        "dur_info": json.dumps({
            "주의사항": "간장애 또는 그 병력이 있는 환자",
            "이상반응": "쇼크, 아나필락시스 유사증상",
            "상호작용": "다른 해열진통제, 감기약과 병용 투여하지 않음"
        })
    },
    {
        "id": 2,
        "name": "판콜에스",
        "details": json.dumps({
            "성분": "아세트아미노펜, 클로르페니라민말레산염",
            "용법용량": "1회 2정씩 1일 3회 식후 복용",
            "효능효과": "감기의 제증상(콧물, 코막힘, 재채기, 인후통, 기침, 가래, 오한, 발열, 두통, 관절통, 근육통)의 완화"
        }),
        "dur_info": json.dumps({
            "주의사항": "다른 감기약, 해열진통제와 함께 복용하지 않음",
            "이상반응": "졸음, 어지러움, 구역, 구토",
            "상호작용": "MAO 억제제, 알코올"
        })
    }
]

async def create_test_database():
    # 데이터베이스 엔진 생성
    engine = create_async_engine(DATABASE_URL, echo=True)
    
    # 기존 테이블 삭제 후 새로 생성
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.drop_all)
        await conn.run_sync(Base.metadata.create_all)
    
    # 세션 생성
    async_session = sessionmaker(engine, class_=AsyncSession)
    
    async with async_session() as session:
        try:
            # 사용자 데이터 삽입
            for user_data in sample_users:
                user = User(**user_data)
                session.add(user)
            
            # 약물 데이터 삽입
            for med_data in sample_medications:
                medication = Medication(**med_data)
                session.add(medication)
            
            await session.commit()
            print("테스트 데이터베이스가 성공적으로 생성되었습니다.")
            
        except Exception as e:
            await session.rollback()
            print(f"오류 발생: {str(e)}")
            raise
        finally:
            await session.close()

if __name__ == "__main__":
    asyncio.run(create_test_database())