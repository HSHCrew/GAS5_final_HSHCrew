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

        try:
            # 만료된 세션 확인 및 정리
            expired_users = []
            for user_id, chatbot in self.instances.items():
                if await chatbot.is_expired():
                    expired_users.append(user_id)
                    await chatbot.cleanup()

            # 인스턴스 제거
            for user_id in expired_users:
                del self.instances[user_id]

            # 용량 제한 확인
            if len(self.instances) > self.settings.max_instances:
                oldest_keys = sorted(self.instances.keys())[:-self.settings.max_instances]
                for key in oldest_keys:
                    await self.instances[key].cleanup()
                    del self.instances[key]

            self.last_cleanup = current_time
        except Exception as e:
            print(f"Cleanup error: {str(e)}")

    async def get_chatbot(self, user_id: int, db: AsyncSession) -> MedicationChatbot:
        try:
            # 기존 인스턴스 확인
            if user_id in self.instances:
                chatbot = self.instances[user_id]
                session = await self.session_service.get_session(user_id)
                if session and session.medication_info:
                    print(f"Reusing existing chatbot instance for user {user_id}")
                    return chatbot

            await self._cleanup_expired()
            
            # DB 조회 디버깅
            print(f"\n[DEBUG] Querying medication summaries for user {user_id}")
            query = select(MedicationSummary).where(MedicationSummary.user_id == user_id)
            result = await db.execute(query)
            summaries = result.scalars().all()
            print(f"[DEBUG] Found {len(summaries)} summaries in DB")
            
            # 각 요약 정보 출력
            for idx, summary in enumerate(summaries):
                print(f"\n[DEBUG] Summary {idx + 1}:")
                print(f"ID: {summary.id}")
                print(f"User ID: {summary.user_id}")
                print(f"Medication ID: {summary.medication_id}")
                print(f"Restructured: {summary.restructured[:10] if summary.restructured else 'None'}...")
            
            # restructured 정보 추출
            medication_info = [
                summary.restructured 
                for summary in summaries 
                if summary.restructured
            ]
            print(f"\n[DEBUG] Extracted {len(medication_info)} valid medication info entries")
            
            # 챗봇 인스턴스 생성
            chatbot = MedicationChatbot(
                user_id=user_id,
                chat_service=self.chat_service,
                session_service=self.session_service,
                settings=self.settings
            )
            
            # 세션 생성 전 데이터 확인
            print("\n[DEBUG] Creating session with following data:")
            print(f"User ID: {user_id}")
            print(f"Medication Info Count: {len(medication_info)}")
            for idx, info in enumerate(medication_info):
                print(f"Info {idx + 1} preview: {info[:10]}...")
            
            # 세션 생성
            session = await self.session_service.create_session(
                user_id=user_id,
                medication_info=medication_info,
                user_info=""
            )
            
            # 세션 생성 확인
            created_session = await self.session_service.get_session(user_id)
            print("\n[DEBUG] Verifying created session:")
            print(f"Session exists: {created_session is not None}")
            if created_session:
                print(f"Medication info count in session: {len(created_session.medication_info)}")
            
            # 초기 대화 시작
            await chatbot.start_chat()
            
            self.instances[user_id] = chatbot
            return chatbot
            
        except Exception as e:
            print(f"[ERROR] Error in get_chatbot: {str(e)}")
            raise
