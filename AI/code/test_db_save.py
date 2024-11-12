import asyncio
import json
from datetime import datetime
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from chatbot.database import engine, AsyncSessionLocal
from chatbot.database.models import MedicationSummary, User, Medication

async def test_save_summary():
    async with AsyncSessionLocal() as db:
        try:
            async with db.begin():
                # 테스트용 요약 데이터
                test_summary = MedicationSummary(
                    user_id=1,
                    medication_id=1,
                    restructured="테스트 재구성 데이터",
                    summary="테스트 요약 데이터",
                    fewshots="테스트 예시 데이터",
                    failed=""
                )
                
                # DB에 추가
                db.add(test_summary)
                print("Summary added to session")
                
            print("Changes committed")

            # 저장된 데이터 확인
            async with db.begin():
                result = await db.execute(
                    select(MedicationSummary).where(
                        MedicationSummary.user_id == 1,
                        MedicationSummary.medication_id == 1
                    )
                )
                saved_summary = result.scalar_one_or_none()
                
                if saved_summary:
                    print("\nSaved summary data:")
                    print(f"User ID: {saved_summary.user_id}")
                    print(f"Medication ID: {saved_summary.medication_id}")
                    print(f"Restructured: {saved_summary.restructured}")
                    print(f"Summary: {saved_summary.summary}")
                    print(f"Created at: {saved_summary.created_at}")
                else:
                    print("No summary found in database")

        except Exception as e:
            print(f"Error occurred: {str(e)}")
            raise

async def check_existing_data():
    async with AsyncSessionLocal() as db:
        async with db.begin():
            # 사용자 확인
            user_result = await db.execute(select(User).where(User.id == 1))
            user = user_result.scalar_one_or_none()
            print("\nExisting user:", user.name if user else "Not found")

            # 약물 확인
            med_result = await db.execute(select(Medication).where(Medication.id == 1))
            med = med_result.scalar_one_or_none()
            print("Existing medication:", med.name if med else "Not found")

            # 기존 요약 확인 - first() 사용
            summary_result = await db.execute(
                select(MedicationSummary)
                .where(
                    MedicationSummary.user_id == 1,
                    MedicationSummary.medication_id == 1
                )
                .order_by(MedicationSummary.created_at.desc())
            )
            summary = summary_result.first()
            print("Existing summary:", "Found" if summary else "Not found")
            if summary:
                print(f"Summary ID: {summary[0].id}")
                print(f"Created at: {summary[0].created_at}")

async def main():
    print("Checking existing data...")
    await check_existing_data()
    
    print("\nTesting summary save...")
    await test_save_summary()

if __name__ == "__main__":
    asyncio.run(main())