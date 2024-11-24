from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import declarative_base, sessionmaker
from sqlalchemy.ext.asyncio import AsyncEngine

# SQLite 데이터베이스 URL (비동기 버전)
SQLALCHEMY_DATABASE_URL = "sqlite+aiosqlite:///./test.db"

# 비동기 엔진 생성
engine = create_async_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args={"check_same_thread": False}  # SQLite를 위한 설정
)

# 비동기 세션 팩토리 생성
AsyncSessionLocal = sessionmaker(
    engine, class_=AsyncSession, expire_on_commit=False
)

# Base 클래스 생성
Base = declarative_base()

# 비동기 데이터베이스 세션 의존성
async def get_db():
    async with AsyncSessionLocal() as session:
        try:
            yield session
        finally:
            await session.close() 