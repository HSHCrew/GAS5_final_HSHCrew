from langchain_openai import ChatOpenAI
from dotenv import load_dotenv
import os
from langchain_core.prompts import load_prompt
from langchain.prompts.chat import (
    ChatPromptTemplate,
    SystemMessagePromptTemplate,
    MessagesPlaceholder,
)
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_core.output_parsers import StrOutputParser
from langchain.callbacks.streaming_stdout import StreamingStdOutCallbackHandler
from langchain_teddynote import logging
from pydantic import BaseModel, Field
import traceback
from redis import asyncio as aioredis
import pickle
from datetime import datetime, timedelta
from .exceptions import ChatbotSessionError, MessageError
from .models import ChatMessage, IntentClassification
from .config import ChatbotSettings
from .services import ChatService, SessionService
from langchain_core.output_parsers import JsonOutputParser
from .prompt_manager import PromptManager

class MedicationChatbot:
    def __init__(
        self, 
        user_id: int, 
        chat_service: ChatService,
        session_service: SessionService,
        settings: ChatbotSettings,
        # prompt_manager: PromptManager
    ):
        load_dotenv()
        logging.langsmith("gas5-fp-chatbot")
        
        self.user_id = user_id
        self.chat_service = chat_service
        self.session_service = session_service
        self.settings = settings
        # self.prompt_manager = prompt_manager
        self.llm = ChatOpenAI(
            model_name=self.settings.MODEL_NAME,
            temperature=self.settings.TEMPERATURE,
            callbacks=[StreamingStdOutCallbackHandler()]
        )
        self.prompt_path = os.path.join(os.path.dirname(__file__), 'prompts')

    async def get_session_history(self) -> ChatMessageHistory:
        """대화 기록 조회"""
        history = ChatMessageHistory()
        messages = await self.chat_service.get_chat_history(self.user_id)
        
        for msg in messages:
            if msg.content:
                if msg.role == "human":
                    history.add_user_message(msg.content)
                else:
                    history.add_ai_message(msg.content)
        
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

    async def start_chat(self) -> str:
        """대화 시작 메시지 반환"""
        try:
            # 세션 정보 확인
            session = await self.session_service.get_session(self.user_id)
            medication_count = len(session.medication_info) if session and session.medication_info else 0
            
            # 시작 메시지 생성
            start_message = f"안녕하세요! 복약 상담 챗봇입니다.\n현재 {medication_count}개의 약물 정보가 등록되어 있습니다."
            
            # 시작 메시지 저장
            await self.chat_service.save_message(
                self.user_id,
                ChatMessage(role="assistant", content=start_message)
            )
            
            return {
                "message": start_message,
                "user_id": self.user_id
            }
            
        except Exception as e:
            print(f"Error in start_chat: {str(e)}")
            raise

    async def is_expired(self) -> bool:
        """세션 만료 여부를 확인합니다."""
        try:
            session = await self.session_service.get_session(self.user_id)
            if not session:
                return True
                
            # 마지막 접근 시간이 TTL을 초과했는지 확인
            last_accessed = session.last_accessed
            if isinstance(last_accessed, str):
                last_accessed = datetime.fromisoformat(last_accessed)
                
            time_diff = datetime.utcnow() - last_accessed
            return time_diff.total_seconds() > self.settings.session_ttl_seconds
            
        except Exception:
            return True

    async def get_conversation_chain(self):
        try:
            prompt_path = os.path.join(self.prompt_path, 'system_template.yaml')
            if not os.path.exists(prompt_path):
                raise FileNotFoundError(f"System prompt template not found at {prompt_path}")
                
            system_prompt = load_prompt(prompt_path, encoding='utf-8')
            system_message = SystemMessagePromptTemplate.from_template(system_prompt.template)
            
            chat_prompt = ChatPromptTemplate.from_messages([
                system_message,
                ("system", "User Info: {user_info}"),
                ("system", "Medication Info: {medication_info}"),
                MessagesPlaceholder(variable_name="chat_history"),
                ("human", "{question}")
            ])
            
            chain = chat_prompt | self.llm | StrOutputParser()
            return chain
        
        except Exception as e:
            raise ChatbotSessionError(f'Error loading conversation chain: {str(e)}')
        
        
    async def classify_intent(self, message: str) -> IntentClassification:
        """사용자 메시지의 의도 분류"""
        try:
            prompt = await self.prompt_manager.get_prompt_template('classify_intent')
            chain = prompt | self.llm | JsonOutputParser(pydantic_object=IntentClassification)
            
            return await chain.ainvoke({"message": message})
        except Exception as e:
            raise ChatbotSessionError(f"Intent classification failed: {str(e)}")


    async def respond(self, message: str) -> str:
        try:
            # 사용자 메시지 저장
            await self.chat_service.save_message(
                self.user_id, 
                ChatMessage(role="human", content=message)
            )
            
            # 세션 정보 가져오기
            session = await self.session_service.get_session(self.user_id)
            if not session:
                raise ChatbotSessionError("Session not found. Please start a new chat.")
            
            # 대화 기록 가져오기
            session_history = await self.get_session_history()
            
            # Chain 실행
            chain = await self.get_conversation_chain()
            chain_with_history = RunnableWithMessageHistory(
                runnable=chain,
                get_session_history=lambda _: session_history,
                input_messages_key="question",
                history_messages_key="chat_history",
            )
            
            # medication_info를 문자열로 결합
            medication_info_str = ""
            if session and session.medication_info:
                medication_info_str = "\n\n".join([
                    f"약물 {idx+1}:\n{info}" 
                    for idx, info in enumerate(session.medication_info)
                    if info  # None이 아닌 경우만 포함
                ])
            
            # 사용자 정보 구성
            user_info_str = f"사용자 ID: {self.user_id}"
            if session and session.user_info:
                user_info_str += f"\n{session.user_info}"
            
            # 프롬프트에 전달할 데이터 구성
            prompt_data = {
                "question": message,
                "medication_info": medication_info_str or "등록된 약물 정보가 없습니다.",
                "user_info": user_info_str
            }
            
            print(f"\nPrompt Data:")
            print(f"User Info: {prompt_data['user_info']}")
            print(f"Medication Info: {prompt_data['medication_info'][:200]}...")
            
            response = await chain_with_history.ainvoke(
                prompt_data,
                config={"configurable": {"session_id": self.user_id}},
            )
            
            # 응답 저장
            await self.chat_service.save_message(
                self.user_id,
                ChatMessage(role="assistant", content=response)
            )
            
            # 세션 마지막 접근 시간 업데이트
            session.last_accessed = datetime.utcnow()
            await self.session_service.create_session(
                user_id=self.user_id,
                medication_info=session.medication_info,
                user_info=session.user_info
            )
            
            return response
        except Exception as e:
            traceback.print_exc()  # 상세한 에러 로그 출력
            raise Exception(f'Error during responding: {str(e)}')

    async def reset_chat(self):
        """채팅 기록을 초기화하고 시작 메시지를 반환합니다."""
        chat_key = await self.get_chat_key(self.user_id)
        await self.redis.delete(chat_key)
        return await self.start_chat()

    async def cleanup(self) -> None:
        """채팅 세션 정리"""
        try:
            # 세션 삭제
            session_key = f"chatbot:session:{self.user_id}"
            await self.session_service.redis.delete(session_key)
            
            # 채팅 기록 삭제
            chat_key = await self.chat_service.get_chat_key(self.user_id)
            await self.chat_service.redis.delete(chat_key)
            
            print(f"Cleaned up session and chat history for user {self.user_id}")
        except Exception as e:
            print(f"Error during cleanup for user {self.user_id}: {str(e)}")
            raise