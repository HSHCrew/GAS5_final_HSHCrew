from typing import Dict
import time
from redis import asyncio as aioredis
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from .medication_chatbot import MedicationChatbot
from .config import ChatbotSettings
from .services import ChatService, SessionService
from .database.models import MedicationSummary

class ChatbotManager:
    def __init__(self, redis_client: aioredis.Redis):
        self.redis = redis_client
        self.instances: Dict[int, MedicationChatbot] = {}
        self.settings = ChatbotSettings()
        self.last_cleanup = time.time()
        self.chat_service = ChatService(redis_client, self.settings)
        self.session_service = SessionService(redis_client, self.settings)

    async def _cleanup_expired(self):
        """만료된 챗봇 인스턴스 정리"""
        current_time = time.time()
        if current_time - self.last_cleanup < self.settings.cleanup_interval:
            return

        expired_users = []
        for user_id, chatbot in self.instances.items():
            if await chatbot.is_expired():
                expired_users.append(user_id)
                await chatbot.cleanup()

        for user_id in expired_users:
            del self.instances[user_id]

        self.last_cleanup = current_time

    async def get_chatbot(self, user_id: int, db: AsyncSession) -> MedicationChatbot:
        if user_id in self.instances:
            chatbot = self.instances[user_id]
            # 기존 인스턴스의 세션 정보 업데이트
            session = await self.session_service.get_session(user_id)
            if session:
                return chatbot

        await self._cleanup_expired()
        
        # 사용자의 약물 요약 정보 조회
        query = select(MedicationSummary).where(MedicationSummary.user_id == user_id)
        result = await db.execute(query)
        summaries = result.scalars().all()
        
        # restructured 정보 추출
        medication_info = [summary.restructured for summary in summaries] if summaries else []
        
        # 챗봇 인스턴스 생성
        chatbot = MedicationChatbot(
            user_id=user_id,
            chat_service=self.chat_service,
            session_service=self.session_service,
            settings=self.settings
        )
        
        # 세션 생성 또는 업데이트
        await self.session_service.create_session(
            user_id=user_id,
            medication_info=medication_info
        )
        
        self.instances[user_id] = chatbot
        return chatbot