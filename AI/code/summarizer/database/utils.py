from contextlib import asynccontextmanager
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from sqlalchemy.orm import sessionmaker
from .models import Base
from ..config import DatabaseSettings

async def init_db():
    db_settings = DatabaseSettings()
    engine = create_async_engine(db_settings.DATABASE_URL)
    
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    
    return engine

async_session = sessionmaker(
    class_=AsyncSession,
    expire_on_commit=False
)

@asynccontextmanager
async def get_session():
    engine = await init_db()
    async_session.configure(bind=engine)
    
    async with async_session() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
        finally:
            await session.close() 