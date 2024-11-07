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
        session_key = f"chatbot:session:{user_id}"
        session_data = await self.redis.hgetall(session_key)
        
        if not session_data:
            return None
            
        try:
            # Redis에서 가져온 데이터에 user_id가 없으면 추가
            if 'user_id' not in session_data:
                session_data['user_id'] = str(user_id)
            
            # medication_info가 문자열이면 JSON으로 파싱
            if 'medication_info' in session_data and isinstance(session_data['medication_info'], str):
                try:
                    session_data['medication_info'] = json.loads(session_data['medication_info'])
                except json.JSONDecodeError:
                    session_data['medication_info'] = []
            
            return ChatSession.from_redis_hash(session_data)
        except Exception as e:
            raise ChatbotSessionError(f"Failed to load session: {str(e)}")

    async def create_session(self, user_id: int, medication_info: List[str], user_info: str = "") -> ChatSession:
        """새로운 세션 생성"""
        try:
            session = ChatSession(
                user_id=user_id,
                medication_info=medication_info,
                user_info=user_info,
                created_at=datetime.utcnow(),
                last_accessed=datetime.utcnow()
            )
            
            session_key = f"chatbot:session:{user_id}"
            session_data = session.to_redis_hash()
            
            # Redis hash로 변환하여 저장
            await self.redis.hset(
                session_key,
                mapping=session_data
            )
            await self.redis.expire(session_key, self.settings.session_ttl_seconds)
            
            return session
        except Exception as e:
            raise ChatbotSessionError(f"Failed to create session: {str(e)}")