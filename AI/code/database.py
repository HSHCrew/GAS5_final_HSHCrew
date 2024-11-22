from motor.motor_asyncio import AsyncIOMotorClient
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import sessionmaker

# SQLite 설정
DATABASE_URL = "sqlite+aiosqlite:////test.db"
engine = create_async_engine(DATABASE_URL, echo=True)
AsyncSessionLocal = sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)

# MongoDB 설정
MONGODB_URL = "mongodb://localhost:27017"
mongodb_client = AsyncIOMotorClient(MONGODB_URL)
mongodb = mongodb_client.gas5fp  # 데이터베이스 이름
summary_collection = mongodb.summaries  # 컬렉션 이름

# 의존성 함수들
async def get_db():
    async with AsyncSessionLocal() as session:
        try:
            yield session
        finally:
            await session.close()

async def get_mongodb():
    try:
        yield mongodb
    finally:
        mongodb_client.close() 