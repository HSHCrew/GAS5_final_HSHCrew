from typing import Optional, List
from sqlalchemy.ext.asyncio import AsyncSession
from .models import ChatMessage, ChatSession, MedicationInfo
from .exceptions import ChatbotSessionError, MessageError
from redis import asyncio as aioredis
from datetime import datetime
from .config import ChatbotSettings
import json

class ChatService:
    def __init__(self, redis_client: aioredis.Redis, settings: ChatbotSettings):
        self.redis = redis_client
        self.settings = settings

    async def get_chat_key(self, user_id: int) -> str:
        return f"chat:history:{user_id}"

    async def save_message(self, user_id: int, message: ChatMessage) -> None:
        if not message.content:
            return
            
        try:
            chat_key = await self.get_chat_key(user_id)
            
            async with self.redis.pipeline(transaction=True) as pipe:
                await pipe.rpush(chat_key, message.json())
                await pipe.ltrim(chat_key, -self.settings.MAX_HISTORY_LENGTH, -1)
                await pipe.expire(chat_key, self.settings.message_ttl_seconds)
                await pipe.execute()
                
        except Exception as e:
            raise MessageError(f"Failed to save message: {str(e)}")

    async def get_chat_history(self, user_id: int) -> List[ChatMessage]:
        chat_key = await self.get_chat_key(user_id)
        messages = await self.redis.lrange(chat_key, 0, -1)
        return [ChatMessage.parse_raw(msg) for msg in messages]

class SessionService:
    def __init__(self, redis_client: aioredis.Redis, settings: ChatbotSettings):
        self.redis = redis_client
        self.settings = settings

    async def get_session(self, user_id: int) -> Optional[ChatSession]:
        """사용자 세션 정보 조회"""
        try:
            print(f"\n[DEBUG] Getting session for user {user_id}")
            session_key = f"chatbot:session:{user_id}"
            session_data = await self.redis.hgetall(session_key)
            
            print(f"[DEBUG] Raw session data from Redis:")
            print(session_data)
            
            if not session_data:
                print("[DEBUG] No session data found")
                return None
                
            session = ChatSession.from_redis_hash(session_data)
            print(f"[DEBUG] Session loaded with {len(session.medication_info)} medication info entries")
            
            return session
            
        except Exception as e:
            print(f"[ERROR] Failed to load session: {str(e)}")
            raise ChatbotSessionError(f"Failed to load session: {str(e)}")

    async def create_session(self, user_id: int, medication_info: List[str], user_info: str = "") -> ChatSession:
        """새로운 세션 생성"""
        try:
            print(f"\n[DEBUG] Creating session for user {user_id}")
            print(f"Medication info count: {len(medication_info)}")
            
            # medication_info가 문자열 리스트인지 확인
            if medication_info and isinstance(medication_info[0], str):
                print("[DEBUG] Medication info is valid string list")
            else:
                print(f"[WARNING] Unexpected medication_info type: {type(medication_info)}")
                if medication_info:
                    print(f"First item type: {type(medication_info[0])}")
            
            session = ChatSession(
                user_id=user_id,
                medication_info=medication_info,
                user_info=user_info,
                created_at=datetime.utcnow(),
                last_accessed=datetime.utcnow()
            )
            
            session_key = f"chatbot:session:{user_id}"
            session_data = session.to_redis_hash()
            
            # Redis 저장 전 데이터 확인
            print("\n[DEBUG] Session data before Redis save:")
            for key, value in session_data.items():
                print(f"{key}: {str(value)[:10]}...")
            
            await self.redis.hset(session_key, mapping=session_data)
            await self.redis.expire(session_key, self.settings.session_ttl_seconds)
            
            # 저장된 데이터 확인
            saved_data = await self.redis.hgetall(session_key)
            print("\n[DEBUG] Saved Redis data:")
            print(saved_data)
            
            return session
            
        except Exception as e:
            print(f"[ERROR] Failed to create session: {str(e)}")
            raise ChatbotSessionError(f"Failed to create session: {str(e)}")