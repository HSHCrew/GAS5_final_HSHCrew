from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from .models import MedicationSummary
from typing import List

class MedicationRepository:
    def __init__(self, db: AsyncSession):
        self.db = db

    async def get_medication_summaries(self, user_id: int) -> List[MedicationSummary]:
        query = select(MedicationSummary).where(
            MedicationSummary.user_id == user_id
        )
        result = await self.db.execute(query)
        return result.scalars().all()

    async def save_summary(self, summary: MedicationSummary) -> None:
        self.db.add(summary)
        await self.db.commit() 