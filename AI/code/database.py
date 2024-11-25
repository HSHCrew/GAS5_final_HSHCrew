from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession
from sqlalchemy.orm import declarative_base, sessionmaker
from config import DatabaseSettings

# 데이터베이스 설정 가져오기
db_settings = DatabaseSettings()

# MySQL 엔진 생성 - dialect를 명시적으로 지정
engine = create_async_engine(
    db_settings.DATABASE_URL,
    echo=True,
    pool_pre_ping=True,
    pool_size=5,
    max_overflow=10,
    future=True
)

# 세션 설정
AsyncSessionLocal = sessionmaker(
    engine,
    class_=AsyncSession,
    expire_on_commit=False
)

Base = declarative_base()

async def get_db():
    async with AsyncSessionLocal() as session:
        try:
            yield session
        finally:
            await session.close() 