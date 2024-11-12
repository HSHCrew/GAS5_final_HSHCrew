import asyncio
from sqlalchemy import select, delete
from chatbot.database import AsyncSessionLocal
from chatbot.database.models import MedicationSummary

async def check_summaries():
    async with AsyncSessionLocal() as db:
        async with db.begin():
            # 모든 요약 데이터 조회
            result = await db.execute(
                select(MedicationSummary).order_by(
                    MedicationSummary.user_id,
                    MedicationSummary.medication_id,
                    MedicationSummary.created_at
                )
            )
            summaries = result.all()
            
            print("\n현재 저장된 요약 데이터:")
            print("-" * 50)
            for summary in summaries:
                summary = summary[0]  # Result 객체에서 실제 모델 인스턴스 추출
                print(f"ID: {summary.id}")
                print(f"User ID: {summary.user_id}")
                print(f"Medication ID: {summary.medication_id}")
                print(f"Created at: {summary.created_at}")
                print(f"Last updated: {summary.last_updated}")
                print("-" * 50)

async def clean_summaries():
    async with AsyncSessionLocal() as db:
        async with db.begin():
            # 모든 요약 데이터 삭제
            await db.execute(delete(MedicationSummary))
            print("\n모든 요약 데이터가 삭제되었습니다.")

async def main():
    print("현재 데이터베이스 상태 확인...")
    await check_summaries()
    
    response = input("\n모든 요약 데이터를 삭제하시겠습니까? (y/n): ")
    if response.lower() == 'y':
        await clean_summaries()
        print("\n데이터 삭제 후 상태 확인...")
        await check_summaries()

if __name__ == "__main__":
    asyncio.run(main()) 