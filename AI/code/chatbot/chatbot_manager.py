from typing import Dict
import time
from redis import asyncio as aioredis
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy import select
from .medication_chatbot import MedicationChatbot
from .config import ChatbotSettings
from .services import ChatService, SessionService
from .database.models import MedicationSummary
from .prompt_manager import PromptManager
import os

class ChatbotManager:
    def __init__(self, redis_client: aioredis.Redis):
        self.redis = redis_client
        self.instances: Dict[int, MedicationChatbot] = {}
        self.settings = ChatbotSettings()
        self.last_cleanup = time.time()
        self.chat_service = ChatService(redis_client, self.settings)
        self.session_service = SessionService(redis_client, self.settings)
        
        # chatbot/prompts 디렉토리를 base_dir로 설정
        base_dir = os.path.dirname(__file__)
        self.prompt_manager = PromptManager(base_dir)

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
                    await self.cleanup_chatbot(user_id)

            # 인스턴스 제거
            for user_id in expired_users:
                del self.instances[user_id]

            # 용량 제한 확인
            if len(self.instances) > self.settings.max_instances:
                oldest_keys = sorted(self.instances.keys())[:-self.settings.max_instances]
                for key in oldest_keys:
                    await self.cleanup_chatbot(key)
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
                else:
                    await self.cleanup_chatbot(user_id)

            await self._cleanup_expired()
            
            # DB에서 약물 정보 조회 - user_profile_id 사용
            query = select(MedicationSummary).where(
                MedicationSummary.user_profile_id == user_id
            )
            result = await db.execute(query)
            summaries = result.scalars().all()
            print(f"[DEBUG] Found {len(summaries)} summaries in DB")
            
            # restructured 정보 추출
            medication_info = [
                summary.restructured 
                for summary in summaries 
                if summary.restructured
            ]
            print(f"[DEBUG] Extracted {len(medication_info)} valid medication info entries")
            print(f"[DEBUG] Medication info: {medication_info}")
            
            # 챗봇 인스턴스 생성 (medication_info가 없어도 생성)
            chatbot = MedicationChatbot(
                user_id=user_id,
                chat_service=self.chat_service,
                session_service=self.session_service,
                settings=self.settings,
                prompt_manager=self.prompt_manager
            )
            
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
            
            # # 초기 대화 시작
            # await chatbot.start_chat()
            
            self.instances[user_id] = chatbot
            
            return chatbot

        except Exception as e:
            print(f"[ERROR] Error getting chatbot for user {user_id}: {str(e)}")
            raise

    async def reset_chat(self, user_id: int, db: AsyncSession) -> MedicationChatbot:
        """챗봇 세션 및 챗 기록 초기화"""
        try:
            print(f"\n[DEBUG] Starting chat reset for user {user_id}")
            
            # 삭제 대상 키
            session_key = f"chatbot:session:{user_id}"
            chat_key = await self.chat_service.get_chat_key(user_id)
            
            # 삭제 전 세션 확인
            before_delete = await self.redis.exists(session_key)
            print(f"[DEBUG] Session exists before deletion: {before_delete}")
            
            # Redis 파이프라인으로 세션과 채팅 기록 삭제
            async with self.redis.pipeline(transaction=True) as pipe:
                await pipe.delete(session_key)
                await pipe.delete(chat_key)
                await pipe.execute()
            
            # 삭제 후 세션 확인
            after_delete = await self.redis.exists(session_key)
            print(f"[DEBUG] Session exists after deletion: {after_delete}")
            
            # 인스턴스 제거
            if user_id in self.instances:
                del self.instances[user_id]
                print(f"[DEBUG] Removed chatbot instance for user {user_id}")
            
            # 새로운 챗봇 인스턴스 생성
            print(f"[DEBUG] Creating new chatbot instance for user {user_id}")
            new_chatbot = await self.get_chatbot(user_id, db)
            
            # 세션이 정상적으로 초기화되었는지 확인
            session = await self.session_service.get_session(user_id)
            if session:
                print(f"[DEBUG] New session created successfully")
            else:
                print(f"[WARNING] Failed to create new session")
            
            return new_chatbot

        except Exception as e:
            print(f"[ERROR] Failed to reset chat for user {user_id}: {str(e)}")
            raise

    async def cleanup_chatbot(self, user_id: int):
        """개별 챗봇 인스턴스 정리"""
        try:
            chatbot = self.instances.get(user_id)
            if chatbot:
                await self.session_service.cleanup(user_id)
                await self.chat_service.cleanup(user_id)
                print(f"Cleaned up chatbot instance for user {user_id}")
        except Exception as e:
            print(f"Error during chatbot cleanup for user {user_id}: {str(e)}")
