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
from datetime import datetime, timedelta, UTC
from .exceptions import ChatbotSessionError, MessageError
from .models import ChatMessage, IntentClassification, ChatSession
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
        prompt_manager: PromptManager
    ):
        load_dotenv()
        logging.langsmith("gas5-fp-chatbot")
        
        self.user_id = user_id
        self.chat_service = chat_service
        self.session_service = session_service
        self.settings = settings
        self.prompt_manager = prompt_manager
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
        await self.session_service.redis.hset(
            session_key,
            mapping={
                "last_accessed": datetime.now(UTC).isoformat(),
                "medication_count": len(self.medication_info)
            }
        )
        await self.session_service.redis.expire(session_key, self.settings.SESSION_TTL)

    async def _validate_session(self) -> bool:
        """세션 유효성 검증"""
        session_key = f"chatbot:session:{self.user_id}"
        chat_key = await self.get_chat_key(self.user_id)
        
        async with self.session_service.redis.pipeline(transaction=True) as pipe:
            await pipe.exists(session_key)
            await pipe.exists(chat_key)
            session_exists, chat_exists = await pipe.execute()
            
        if not session_exists:
            # 세션이 없으면 새로 시작
            await self.start_chat()
            return True
        
        if not chat_exists:
            print(f"[DEBUG] Chat key {chat_key} does not exist.")
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
                timestamp=datetime.now(UTC)
            )
            
            chat_key = await self.get_chat_key(self.user_id)
            
            async with self.chat_service.redis.pipeline(transaction=True) as pipe:
                # JSON으로 직렬화
                await pipe.rpush(chat_key, message.model_dump_json())
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
                
            time_diff = datetime.now(UTC) - last_accessed
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
            print("[DEBUG] Loading classify_intent prompt template")
            prompt = await self.prompt_manager.get_prompt_template('classify_intent')
            print("[DEBUG] Creating classification chain")
            
            # JsonOutputParser를 직접 생성하고 결과를 IntentClassification으로 변환
            output_parser = JsonOutputParser()
            chain = prompt | self.llm | output_parser
            
            print(f"[DEBUG] Classifying message: {message}")
            result = await chain.ainvoke({"message": message})
            print(f"[DEBUG] Raw classification result: {result}")
            
            # dict를 IntentClassification 객체로 변환
            return IntentClassification(
                intent=result["intent"],
                confidence=result["confidence"],
                explanation=result["explanation"]
            )
            
        except Exception as e:
            print(f"[ERROR] Intent classification error: {str(e)}")
            # 폴백 로직
            harmful_keywords = ["system", "prompt", "assistant", "model", "instruction"]
            if any(keyword in message.lower() for keyword in harmful_keywords):
                return IntentClassification(
                    intent="harmful",
                    confidence=1.0,
                    explanation="시스템 관련 키워드가 감지되었습니다."
                )
            
            medical_keywords = ["약", "복용", "효과", "부작용", "주의", "보관", "용법"]
            if any(keyword in message for keyword in medical_keywords):
                return IntentClassification(
                    intent="medical_or_daily",
                    confidence=0.8,
                    explanation="의약품 관련 키워드가 감지되었습니다."
                )
            
            return IntentClassification(
                intent="medical_or_daily",
                confidence=0.5,
                explanation="의도를 명확히 파악할 수 없어 의료 상담으로 처리합니다."
            )

    async def respond(self, message: str) -> str:
        try:
            # 세션 정보 가져오기
            session = await self.session_service.get_session(self.user_id)
            if not session:
                raise ChatbotSessionError("Session not found. Please start a new chat.")
            
            # 의도 분류 
            intent_result = await self.classify_intent(message)
            intent = intent_result.intent  # IntentClassification 객체에서 intent 추출
            
            # 의도에 따른 응답 생성
            if intent == "harmful":
                response = "잘못된 질문입니다."
            elif intent == "clarification":
                response = await self._generate_medical_response(
                    f"이전 질문에 대해 좀 더 자세히 설명해주세요.: {message}", 
                    session
                )
            else:   
                response = await self._generate_medical_response(message, session)
            
            # 대화 기록 저장 (사용자 메시지와 응답을 한 번에 저장)
            async with self.chat_service.redis.pipeline(transaction=True) as pipe:
                # 사용자 메시지 저장
                user_message = ChatMessage(role="human", content=message)
                await self.chat_service.save_message(self.user_id, user_message)
                
                # 챗봇 응답 저장
                bot_message = ChatMessage(role="assistant", content=response)
                await self.chat_service.save_message(self.user_id, bot_message)
            
            # 세션 업데이트
            session.last_accessed = datetime.now(UTC)
            await self.session_service.create_session(
                user_id=self.user_id,
                medication_info=session.medication_info,
                user_info=session.user_info
            )
            
            return response
            
        except Exception as e:
            traceback.print_exc()
            raise Exception(f'Error during responding: {str(e)}')

    async def _generate_medical_response(self, message: str, session: ChatSession) -> str:
        """의료 관련 응답 생성"""
        # 대화 기록 가져오기
        session_history = await self.get_session_history()
        
        # Chain 실행
        chain = await self.get_conversation_chain()
        chain_with_history = RunnableWithMessageHistory(
            runnable=chain,
            get_session_history=lambda _: session_history,
            input_messages_key="question",
            history_messages_key="chat_history",
            session_key=f"chatbot:session:{self.user_id}"
        )
            
        # medication_info 문자열 구성
        medication_info_str = "\n\n".join([
            f"약물 {idx+1}:\n{info}" 
            for idx, info in enumerate(session.medication_info)
            if info
        ]) or "등록된 약물 정보가 없습니다."
        
        # 프롬프트 데이터 구성
        prompt_data = {
            "question": message,
            "medication_info": medication_info_str,
            "user_info": f"사용자 ID: {self.user_id}\n{session.user_info}"
        }
        
        return await chain_with_history.ainvoke(
            prompt_data,
            config={"configurable": {"session_id": self.user_id}},
        )