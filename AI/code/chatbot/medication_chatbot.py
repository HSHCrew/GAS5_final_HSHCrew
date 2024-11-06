from langchain_openai import ChatOpenAI
from dotenv import load_dotenv
import json
import os
import asyncio
from langchain_core.prompts import load_prompt
from langchain_core.prompts import PromptTemplate
from langchain.prompts.chat import (
    ChatPromptTemplate,
    HumanMessagePromptTemplate,
    SystemMessagePromptTemplate,
    MessagesPlaceholder
)
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.chat_history import BaseChatMessageHistory
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_core.output_parsers import StrOutputParser
from langchain.callbacks.streaming_stdout import StreamingStdOutCallbackHandler
from langchain_teddynote import logging
from pydantic import BaseModel, Field
import traceback
from redis import asyncio as aioredis
import pickle
from datetime import datetime, timedelta
from .exceptions import SessionError, MessageError
from .models import ChatMessage
from .config import ChatbotSettings


class MedicationChatbot:
    def __init__(
        self, 
        user_id: int, 
        redis_client: aioredis.Redis, 
        user_info: str = None, 
        medication_info: list[str] = None,
        settings: ChatbotSettings = None
    ):
        load_dotenv()
        logging.langsmith("gas5-fp-chatbot")
        
        # 설정 초기화
        self.settings = settings or ChatbotSettings()
        
        self.user_id = user_id
        self.user_info = user_info
        if medication_info is None:
            medication_info = []
        elif isinstance(medication_info, str):
            try:
                medication_info = json.loads(medication_info)
            except json.JSONDecodeError:
                medication_info = [medication_info]
        self.medication_info = medication_info
        self.redis = redis_client
        print(f"Initialized chatbot for user {user_id} with {len(self.medication_info)} medications")
        self.message_ttl = timedelta(days=7)  # 메시지 보관 기간
        self.llm = ChatOpenAI(
            temperature=0,
            model_name='gpt-4o',
            callbacks=[StreamingStdOutCallbackHandler()],
        )
        self.current_dir = os.path.dirname(__file__)
        self.prompt_path = os.path.join(self.current_dir, 'prompts')

    async def get_chat_key(self, user_id: int) -> str:
        return f"chat:history:{user_id}"

    async def get_session_history(self, session_id: int) -> ChatMessageHistory:
        chat_key = await self.get_chat_key(session_id)
        history = ChatMessageHistory()
        
        # 파이프라인으로 메시지 일괄 처리
        async with self.redis.pipeline(transaction=True) as pipe:
            await pipe.lrange(chat_key, 0, -1)
            await pipe.expire(chat_key, timedelta(days=self.settings.MESSAGE_TTL))
            messages, _ = await pipe.execute()
            
        for msg_bytes in messages:
            try:
                msg = ChatMessage.parse_raw(msg_bytes)
                if msg.content:
                    if msg.role == "human":
                        history.add_user_message(msg.content)
                    else:
                        history.add_ai_message(msg.content)
            except Exception as e:
                print(f"Error processing message: {e}")
                continue
        
        return history

    async def _update_session_timestamp(self) -> None:
        """세션 마지막 접근 시간 업데이트"""
        session_key = f"chatbot:session:{self.user_id}"
        await self.redis.hset(
            session_key,
            mapping={
                "last_accessed": datetime.utcnow().isoformat(),
                "medication_count": len(self.medication_info)
            }
        )
        await self.redis.expire(session_key, self.settings.SESSION_TTL)

    async def _validate_session(self) -> bool:
        """세션 유효성 검증"""
        session_key = f"chatbot:session:{self.user_id}"
        chat_key = await self.get_chat_key(self.user_id)
        
        async with self.redis.pipeline(transaction=True) as pipe:
            await pipe.exists(session_key)
            await pipe.exists(chat_key)
            session_exists, chat_exists = await pipe.execute()
            
        if not session_exists:
            # 세션이 없으면 새로 시작
            await self.start_chat()
            return True
        
        if not chat_exists:
            # 채팅 기록이 없으면 초기화
            await self.reset_chat()
            return True
            
        await self._update_session_timestamp()
        return True

    async def save_message(self, role: str, content: str) -> None:
        if not content:  # content가 None이거나 비어있으면 저장하지 않음
            return
        
        try:
            message = ChatMessage(
                role=role,
                content=content,
                timestamp=datetime.utcnow()
            )
            
            chat_key = await self.get_chat_key(self.user_id)
            
            async with self.redis.pipeline(transaction=True) as pipe:
                # JSON으로 직렬화
                await pipe.rpush(chat_key, message.json())  # pickle.dumps 대신 json() 사용
                await pipe.ltrim(chat_key, -self.settings.MAX_HISTORY_LENGTH, -1)
                await pipe.expire(chat_key, timedelta(days=self.settings.MESSAGE_TTL))
                await pipe.execute()
                
        except Exception as e:
            raise MessageError(f"Failed to save message: {str(e)}")

    async def start_chat(self):
        """대화 시작 메시지를 반환합니다."""
        try:
            # 세션 초기화
            session_key = f"chatbot:session:{self.user_id}"
            await self.redis.hset(
                session_key,
                mapping={
                    "created_at": datetime.utcnow().isoformat(),
                    "last_accessed": datetime.utcnow().isoformat(),
                    "medication_count": len(self.medication_info)
                }
            )
            await self.redis.expire(session_key, self.settings.session_ttl_seconds)
            
            initial_message = (
                f"안녕하세요! 복약 상담 챗봇입니다.\n"
                f"현재 {len(self.medication_info)}개의 약물 정보가 등록되어 있습니다."
            )
            await self.save_message("assistant", initial_message)
            return initial_message
        except Exception as e:
            raise SessionError(f"Failed to start chat: {str(e)}")

    async def get_conversation_chain(self):
        try:
            prompt_path = os.path.join(self.prompt_path, 'system_template.yaml')
            if not os.path.exists(prompt_path):
                raise FileNotFoundError(f"System prompt template not found at {prompt_path}")
                
            system_prompt = load_prompt(prompt_path, encoding='utf-8')
            system_message = SystemMessagePromptTemplate.from_template(system_prompt.template)
            
            chat_prompt = ChatPromptTemplate.from_messages([
                system_message, 
                MessagesPlaceholder(variable_name="chat_history"),
                ("human", "#Question:\n{question}"),
            ])
            
            chain = chat_prompt | self.llm | StrOutputParser()
            return chain
        
        except Exception as e:
            raise Exception(f'Error loading conversation chain: {str(e)}\n{traceback.format_exc()}')

    async def conversation_with_history(self):
        try:
            chain = await self.get_conversation_chain()
            # 미리 await을 사용하여 session history 결과를 얻음
            session_history = await self.get_session_history(self.user_id)

            # chain_with_history에 session_history를 넘김
            chain_with_history = RunnableWithMessageHistory(
                runnable=chain,
                get_session_history=lambda _: session_history,  # 동기 함수처럼 사용
                input_messages_key="question",
                history_messages_key="chat_history",
            )
            return chain_with_history

        except Exception as e:
            raise Exception(f'Error during conversation with history: {str(e)}\n{traceback.format_exc()}')

    async def respond(self, message: str):
        try:
            # 사용자 메시지 저장
            await self.save_message("human", message)
            
            chain = await self.conversation_with_history()
            response = await chain.ainvoke(
                {
                    "question": message,
                    "medication_info": self.medication_info,
                    "user_info": self.user_info
                },
                config={"configurable": {"session_id": self.user_id}},
            )
            
            # AI 응답 저장
            await self.save_message("assistant", response)
            
            return response
        except Exception as e:
            raise Exception(f'Error during responding: {str(e)}')

    async def reset_chat(self):
        """채팅 기록을 초기화하고 시작 메시지를 반환합니다."""
        chat_key = await self.get_chat_key(self.user_id)
        await self.redis.delete(chat_key)
        return await self.start_chat()

    async def cleanup(self) -> None:
        """리소스 정리"""
        try:
            chat_key = await self.get_chat_key(self.user_id)
            session_key = f"chatbot:session:{self.user_id}"
            
            # Redis에서 세션 및 채팅 기록 삭제
            await asyncio.gather(
                self.redis.delete(chat_key),
                self.redis.delete(session_key)
            )
        except Exception as e:
            print(f"Cleanup error for user {self.user_id}: {str(e)}")