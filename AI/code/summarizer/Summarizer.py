from dotenv import load_dotenv
import os
import asyncio
from typing import List, Optional
from langchain_openai import ChatOpenAI
from langchain_teddynote import logging
from .models import ProcessResult
from .prompt_manager import PromptManager
from .llm_processor import LLMProcessor

from uuid import uuid4
from langchain_core.callbacks import CallbackManager
from langchain_core.tracers import ConsoleCallbackHandler, LangChainTracer
from langchain_core.runnables import RunnableConfig
from langchain_core.callbacks.base import AsyncCallbackHandler
from .callbacks import AsyncRunCollector
from .config import SummarizerSettings

class Summarizer:
    def __init__(self, project_name: str = "medication-summarizer"):
        load_dotenv()
        
        settings = SummarizerSettings()
        
        # LangSmith 설정
        try:
            logging.langsmith("gas5-fp")
        except Exception as e:
            print(f"Warning: LangSmith initialization failed: {e}")
        
        callbacks = [AsyncRunCollector()]
        if os.getenv("LANGCHAIN_API_KEY"): 
            try:
                self.tracer = LangChainTracer()
                callbacks.append(self.tracer)
            except Exception as e:
                print(f"Warning: LangChain tracer initialization failed: {e}")
        
        self.callback_manager = CallbackManager(callbacks)
        
        # LLM 설정
        self.llm = ChatOpenAI(
            temperature=settings.TEMPERATURE,
            model_name=settings.MODEL_NAME,
            callback_manager=self.callback_manager
        )
        
        # 프로세서 초기화
        self.prompt_manager = PromptManager(os.path.dirname(__file__))
        self.processor = LLMProcessor(
            llm=self.llm,
            prompt_manager=self.prompt_manager,
            callback_manager=self.callback_manager,
            settings=settings
        )

    async def process_medication_info(
        self,
        content: str,
        user_id: Optional[str] = None,
        medication_id: Optional[int] = None,
        index: Optional[int] = None,
        parent_run_id: Optional[str] = None
    ) -> ProcessResult:
        """단일 복약정보 처리"""
        config = RunnableConfig(
            callbacks=self.callback_manager.handlers,
            run_name=f"Process Medication Info - Med {medication_id}",
            tags=["medication_processing"],
            parent_run_id=parent_run_id
        )
        
        result = await self.processor.process_content(content, config)
        result.index = index
        return result

    async def process_medication_infos(
        self,
        contents: List[str],
        user_id: str,
        medication_ids: List[int]
    ) -> List[ProcessResult]:
        """여러 복약정보 처리"""
        if len(contents) != len(medication_ids):
            raise ValueError("Contents and medication_ids must have the same length")
        
        # 유저 레벨 run 생성
        user_run_id = str(uuid4())
        user_config = RunnableConfig(
            callbacks=self.callback_manager.handlers,
            run_name=f"User Medication Processing - User {user_id}",
            tags=["user_processing"],
            parent_run_id=None
        )
        
        # 비동기 작업 생성
        tasks = []
        for idx, (content, med_id) in enumerate(zip(contents, medication_ids)):
            # 약물정보 레벨 run 생성
            med_run_id = str(uuid4())
            med_config = RunnableConfig(
                callbacks=self.callback_manager.handlers,
                run_name=f"Medication {med_id} Processing",
                tags=["medication_processing"],
                parent_run_id=user_run_id
            )
            
            # 비동기 태스크 생성
            task = self.process_medication_info(
                content=content,
                user_id=user_id,
                medication_id=med_id,
                index=idx,
                parent_run_id=med_run_id
            )
            tasks.append(task)
        
        # 모든 태스크 동시 실행
        results = await asyncio.gather(*tasks)
        return results

    @classmethod
    def create(cls, 
              llm: Optional[ChatOpenAI] = None,
              settings: Optional[SummarizerSettings] = None,
              project_name: str = "medication-summarizer") -> "Summarizer":
        instance = cls(project_name)
        if llm:
            instance.llm = llm
        if settings:
            instance.settings = settings
        return instance