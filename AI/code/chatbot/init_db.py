import asyncio
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from database import engine, Base, AsyncSessionLocal
from db_models import User, VoiceTranscription

async def init_db():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.drop_all)
        await conn.run_sync(Base.metadata.create_all)
    
    async with AsyncSessionLocal() as session:
        test_user = User(
            id=1,
            name="Test User"
        )
        session.add(test_user)
        await session.commit()

if __name__ == "__main__":
    asyncio.run(init_db()) 