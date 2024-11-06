from typing import Optional, Tuple
from langchain_openai import ChatOpenAI
from langchain_core.runnables import RunnableConfig
from langchain_core.runnables import RunnablePassthrough
from .config import SummarizerSettings
from .models import Topic, ProcessResult
from .prompt_manager import PromptManager
from langchain_core.output_parsers import StrOutputParser, JsonOutputParser
from uuid import uuid4
from langchain_core.callbacks import CallbackManager

class LLMProcessor:
    def __init__(
        self, 
        llm: ChatOpenAI,
        prompt_manager: PromptManager,
        callback_manager: CallbackManager,
        settings: Optional[SummarizerSettings] = None
    ):
        self.llm = llm
        self.prompt_manager = prompt_manager
        self.callback_manager = callback_manager
        self.settings = settings or SummarizerSettings()
        self.parser = JsonOutputParser(pydantic_object=Topic)

    async def _create_chain(
        self, 
        prompt_type: str, 
        output_parser=None, 
        parent_run_id: Optional[str] = None
    ):
        """프롬프트 타입에 따른 체인 생성"""
        prompt = await self.prompt_manager.get_prompt_template(prompt_type)
        chain = (
            RunnablePassthrough() 
            | prompt 
            | self.llm 
            | (output_parser or StrOutputParser())
        )
        
        # 새로운 run_id 생성
        run_id = str(uuid4())
        
        return chain.with_config(
            run_name=f"Process {prompt_type}",
            tags=[prompt_type],
            callbacks=self.callback_manager.handlers,
            run_id=run_id,  # run_id 명시적 지정
            parent_run_id=parent_run_id
        )

    async def process_content(
        self, 
        content: str,
        config: Optional[RunnableConfig] = None
    ) -> ProcessResult:
        """단일 컨텐츠 처리"""
        result = ProcessResult()
        
        try:
            # 처리 과정 레벨 run 생성
            process_run_id = str(uuid4())
            
            # config 설정
            if config is None:
                config = RunnableConfig(
                    callbacks=self.callback_manager.handlers,
                    run_id=process_run_id
                )
            else:
                config["run_id"] = process_run_id
                config["callbacks"] = self.callback_manager.handlers
            
            # 각 단계별 처리
            result.restructured = await self._process_step(
                'restruct',
                content,
                config,
                parent_run_id=process_run_id
            )

            result.summary = await self._process_step(
                'summarize',
                result.restructured,
                config,
                parent_run_id=process_run_id
            )

            verification = await self._verify_summary(
                result.restructured,
                result.summary,
                config,
                parent_run_id=process_run_id
            )

            if verification.score < self.settings.VERIFICATION_THRESHOLD:
                result = await self._regenerate_summary(
                    result,
                    config,
                    parent_run_id=process_run_id
                )
            
            return result

        except Exception as e:
            result.failed = str(e)
            return result

    async def _process_step(
        self,
        step_type: str,
        content: str,
        config: Optional[RunnableConfig],
        parent_run_id: Optional[str] = None
    ) -> str:
        """각 처리 단계 실행"""
        chain = await self._create_chain(
            step_type, 
            parent_run_id=parent_run_id
        )
        return await chain.ainvoke(content, config=config)

    async def _verify_summary(
        self,
        original: str,
        summary: str,
        config: Optional[RunnableConfig],
        parent_run_id: Optional[str] = None
    ) -> Topic:
        """요약본 검증"""
        try:
            chain = await self._create_chain(
                'verify', 
                self.parser,
                parent_run_id=parent_run_id
            )
            result = await chain.ainvoke(
                {
                    'original_content': original,
                    'summary': summary
                },
                config=config
            )
            
            # dict를 Topic 모델로 변환
            if isinstance(result, dict):
                return Topic(
                    score=result.get('score', 0),
                    feedback=result.get('feedback', 'No feedback provided')
                )
            return result
            
        except Exception as e:
            print(f"Verification error: {str(e)}")
            # 에러 발생 시 기본 Topic 객체 반환
            return Topic(
                score=0,
                feedback=f"Verification failed: {str(e)}"
            )

    async def _regenerate_summary(
        self,
        result: ProcessResult,
        config: Optional[RunnableConfig],
        parent_run_id: Optional[str] = None
    ) -> ProcessResult:
        """요약본 재생성"""
        for attempt in range(self.settings.MAX_RETRY_ATTEMPTS):
            verification = await self._verify_summary(
                result.restructured,
                result.summary,
                config,
                parent_run_id=parent_run_id
            )
            
            if verification.score >= self.settings.VERIFICATION_THRESHOLD:
                print("Score meets threshold, stopping regeneration")
                break
                
            print(f"Score below threshold, regenerating...")
            
            result.fewshots += (
                f"Previous Summary (Attempt {attempt + 1}):\n"
                f"{result.summary}\n"
                f"Feedback:\n"
                f"{verification.feedback}\n"
                f"---\n"
            )
            
            print(f"Current fewshots:\n{result.fewshots}")
            chain = await self._create_chain(
                'regenerate',
                parent_run_id=parent_run_id
            )
            result.summary = await chain.ainvoke(
                {
                    'original': result.restructured,
                    'fewshots': result.fewshots
                },
                config=config
            )
        
        return result