from langchain_openai import ChatOpenAI
from dotenv import load_dotenv
import json
import os
import asyncio
from langchain_core.prompts import load_prompt
from langchain_core.prompts import PromptTemplate
from langchain.prompts.chat import (
    # ChatPromptTemplate,
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


class MedicationChatbot:
    def __init__(self, user):
        load_dotenv()
        logging.langsmith("gas5-fp-chatbot")
        self.user_id = user.user_id
        self.user_info = user.user_info
        self.medication_info = str(user.medication_info)
        self.current_dir = os.path.dirname(__file__)
        self.llm = ChatOpenAI(
            temperature=0,
            model_name='gpt-4o',
            callbacks=[StreamingStdOutCallbackHandler()],
        )
        self.conversation_history = {}  # 대화 이력 초기화
        self.prompt_path = os.path.join(self.current_dir, 'prompts')
        
    def get_session_history(self, session_ids):
        print(f"[대화 세션ID]: {session_ids}")
        if session_ids not in self.conversation_history:  # 세션 ID가 store에 없는 경우
            # 새로운 ChatMessageHistory 객체를 생성하여 store에 저장
            self.conversation_history[session_ids] = ChatMessageHistory()
        return self.conversation_history[session_ids]  # 해당 세션 ID에 대한 세션 기록 반환
    
    async def start_chat(self):
        '''초기화 혹은 대화 시작 로직'''
        return f'챗봇 시작. 사용자 : {self.user_id}'
    
    # async def get_conversation_chain(self):
    #     system_prompt = load_prompt(os.path.join(self.prompt_path, 'system_template.yaml'), encoding='utf-8')
    #     # fewshot_example = 
    #     # example_prompt = ChatPromptTemplate.from_messages(
    #     #     [
    #     #         ("human", "{question}"),
    #     #         ("ai", "{answer}"),
    #     #     ]
    #     # )
    #     # few_shot_prompt = FewShotChatMessagePromptTemplate(
    #     #     example_prompt=example_prompt,
    #     #     examples=few_shot_examples,
    #     # )
    #     messages = [
    #         SystemMessagePromptTemplate.from_template(system_prompt),
    #         # few_shot_prompt,
    #     ]
        
    #     chat_prompt = ChatPromptTemplate.from_messages([
    #             messages,
    #             MessagesPlaceholder(variable_name="chat_history"),
    #             ("human", "#Question:\n{question}"),  # 사용자 입력을 변수로 사용
    #         ])
    #     chain = chat_prompt | self.llm | StrOutputParser()
    #     return chain
    
    async def get_conversation_chain(self):
        try:
            # 시스템 프롬프트를 로드합니다.
            system_prompt = load_prompt(os.path.join(self.prompt_path, 'system_template.yaml'), encoding='utf-8')
            print()
            print()
            print()
            # 시스템 메시지 템플릿을 생성합니다.
            messages = [
                SystemMessagePromptTemplate.from_template(system_prompt),
            ]
            
            # 대화 프롬프트 템플릿을 생성합니다.
            chat_prompt = PromptTemplate.from_messages([
                messages,
                MessagesPlaceholder(variable_name="chat_history"),
                ("human", "#Question:\n{question}"),  # 사용자 입력을 변수로 사용
            ])
            
            chain = chat_prompt | self.llm | StrOutputParser()
            return chain
        except Exception as e:
            raise Exception(f'Error loading conversation chain: {str(e)}')

    async def conversation_with_history(self):
        try:
            chain = await self.get_conversation_chain()
            print()
            print()
            print()
            chain_with_history = RunnableWithMessageHistory(
                chain,
                self.get_session_history,  # 세션 기록을 가져오는 함수
                input_messages_key="question",  # 사용자의 질문이 템플릿 변수에 들어갈 key
                history_messages_key="chat_history",  # 기록 메시지의 키
            )
            return chain_with_history
        except Exception as e:
            raise Exception(f'Error during conversation with history: {str(e)}')

    async def respond(self, message: str):
        try:
            chain = await self.conversation_with_history()  # 비동기 호출로 대화 체인 가져오기
            print()
            print()
            print()
            response = await chain.invoke(
                {
                    "question": message,
                    "medication_info": self.medication_info,
                    'user_info': self.user_info
                },
                config={"configurable": {"session_id": self.user_id}},
            )
            return response
        except Exception as e:
            raise Exception(f'Error during responding: {str(e)}')



    # 약물정보 요약본 요청
    def ask_question(self, user_id, user_question):
        conn = self.db_manager.connect()
        cursor = conn.cursor()
        cursor.execute("SELECT summary FROM summaries WHERE user_id = ?", (user_id,))
        result = cursor.fetchall()
        # print('debugging: ask_question: result:',result)
        summary = result[-1] if result else None
        conn.close()
        
        if summary:
            # print('debugging: ask_question:',summary)
            return self.chatbot_manager.get_user_question_response(user_id, user_question, summary)
        else:
            return "요약된 약물 정보가 없습니다. 약물 정보를 먼저 저장하세요."
