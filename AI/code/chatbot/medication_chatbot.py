from langchain_openai import ChatOpenAI
from dotenv import load_dotenv
import os
from langchain_core.prompts import load_prompt
from langchain.prompts.chat import (
    ChatPromptTemplate,
    SystemMessagePromptTemplate,
    MessagesPlaceholder
)
from langchain_community.chat_message_histories import ChatMessageHistory
from langchain_core.runnables.history import RunnableWithMessageHistory
from langchain_core.output_parsers import StrOutputParser
from langchain.callbacks.streaming_stdout import StreamingStdOutCallbackHandler
from langchain_teddynote import logging
from .exceptions import ChatbotSessionError, MessageError
from .models import ChatMessage
from .config import ChatbotSettings
from .services import ChatService, SessionService
from datetime import datetime


class MedicationChatbot:
    def __init__(
        self,
        user_id: int,
        chat_service: ChatService,
        session_service: SessionService,
        settings: ChatbotSettings
    ):
        load_dotenv()
        logging.langsmith("gas5-fp-chatbot")
        
        self.user_id = user_id
        self.chat_service = chat_service
        self.session_service = session_service
        self.settings = settings
        
        self.llm = ChatOpenAI(
            model_name=self.settings.MODEL_NAME,
            temperature=self.settings.TEMPERATURE,
            callbacks=[StreamingStdOutCallbackHandler()]
        )
        self.prompt_path = os.path.join(os.path.dirname(__file__), 'prompts')

    async def get_session_history(self) -> ChatMessageHistory:
        """대화 기록 조회"""
        history = ChatMessageHistory()
        chat_history = await self.chat_service.get_chat_history(self.user_id)
        
        for msg in chat_history:
            if msg.content:
                if msg.role == "human":
                    history.add_user_message(msg.content)
                else:
                    history.add_ai_message(msg.content)
        
        return history

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
            medication_info_str = "\n".join(session.medication_info) if session.medication_info else ""
            
            response = await chain_with_history.ainvoke(
                {
                    "question": message,
                    "medication_info": medication_info_str,
                    "user_info": session.user_info or ""
                },
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
        except ChatbotSessionError as e:
            raise ChatbotSessionError(f"Session error: {str(e)}")
        except Exception as e:
            raise MessageError(f"Failed to process message: {str(e)}")

    async def start_chat(self) -> str:
        """채팅 세션 시작"""
        try:
            initial_message = (
                f"안녕하세요! 복약 상담 챗봇입니다.\n"
                f"어떤 도움이 필요하신가요?"
            )
            
            # 초기 메시지를 assistant 메시지로 저장
            await self.chat_service.save_message(
                self.user_id,
                ChatMessage(role="assistant", content=initial_message)
            )
            
            return initial_message
        except Exception as e:
            raise ChatbotSessionError(f"Failed to start chat: {str(e)}")

    async def is_expired(self) -> bool:
        """챗봇 세션 만료 여부 확인"""
        session = await self.session_service.get_session(self.user_id)
        return session is None

    async def cleanup(self):
        """리소스 정리"""
        try:
            chat_key = await self.chat_service.get_chat_key(self.user_id)
            session_key = f"chatbot:session:{self.user_id}"
            
            # Redis에서 세션 및 채팅 기록 삭제
            await self.chat_service.redis.delete(chat_key, session_key)
        except Exception as e:
            print(f"Cleanup error for user {self.user_id}: {str(e)}")