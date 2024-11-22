import asyncio
from redis import asyncio as aioredis
from dotenv import load_dotenv
import os

async def test_redis():
    load_dotenv()
    
    try:
        # Redis 연결
        redis = await aioredis.from_url(
            os.getenv("REDIS_URL"),
            encoding="utf-8",
            decode_responses=False
        )
        
        # 테스트 데이터 쓰기
        await redis.set('test_key', 'Hello Redis!')
        
        # 데이터 읽기
        value = await redis.get('test_key')
        print(f"Retrieved value: {value}")
        
        await redis.aclose()
        
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    asyncio.run(test_redis())