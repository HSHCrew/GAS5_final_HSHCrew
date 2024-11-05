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

class Summarizer:
    def __init__(self):
        load_dotenv()
        logging.langsmith("gas5-fp")
        
        self.tracer = LangChainTracer()
        self.callback_manager = CallbackManager([self.tracer])
        
        self.llm = ChatOpenAI(
            temperature=0,
            model_name="gpt-4o-mini",
            api_key=os.getenv("OPENAI_API_KEY"),
            callback_manager=self.callback_manager
        )
        
        self.prompt_manager = PromptManager(os.path.dirname(__file__))
        self.processor = LLMProcessor(self.llm, self.prompt_manager)

    async def process_single(self, context: str, index: Optional[int] = None, 
                           parent_run: Optional[AsyncCallbackHandler] = None,
                           parent_run_id: Optional[str] = None) -> ProcessResult:
        result = ProcessResult(index=index)
        fewshots = ""
        run_id = str(uuid4())
        
        config = RunnableConfig(
            callbacks=[parent_run, self.tracer] if parent_run else self.callback_manager.handlers,
            run_name=f"Document {index}",
            tags=["document_processing"],
            run_id=run_id
        )
        
        try:
            if parent_run:
                await parent_run.on_chain_start(
                    {"name": f"process_document_{index}"},
                    {"input": context},
                    run_id=run_id,
                    parent_run_id=parent_run_id
                )
            
            result.restructured = await self.processor.restruct(context, config)
            result.summary = await self.processor.summarize(result.restructured, config)
            verification = await self.processor.verify(
                (result.restructured, result.summary),
                config
            )
            
            for attempt in range(2):
                if verification['score'] < 80:
                    fewshots += (
                        f"Previous Summary (Attempt {attempt + 1}):\n"
                        f"{result.summary}\n"
                        f"Feedback:\n"
                        f"{verification['feedback']}\n"
                        f"---\n"
                    )
                    result.summary = await self.processor.regenerate(
                        (result.restructured, fewshots),
                        config
                    )
                    verification = await self.processor.verify(
                        (result.restructured, result.summary),
                        config
                    )
                else:
                    break
            
            result.fewshots = fewshots
            
            if parent_run:
                await parent_run.on_chain_end(
                    {"output": result.summary},
                    run_id=run_id
                )
            return result
            
        except Exception as e:
            result.failed = str(e)
            if parent_run:
                await parent_run.on_chain_error(e, run_id=run_id)
            return result
        
    async def mono_processes(self, contexts: List[str]) -> List[ProcessResult]:
        """Process multiple contexts as a single chain"""
        run_collector = AsyncRunCollector()
        batch_run_id = str(uuid4())
        
        await run_collector.on_chain_start(
            {"name": "batch_summarization"},
            {"input": f"Processing {len(contexts)} documents"},
            run_id=batch_run_id
        )
        
        try:
            config = RunnableConfig(
                callbacks=[run_collector, self.tracer],
                tags=["batch_summarization"],
                run_name="Batch Document Processing",
                run_id=batch_run_id
            )
            
            tasks = [
                self.process_single(
                    context, 
                    index, 
                    run_collector, 
                    batch_run_id
                ) 
                for index, context in enumerate(contexts)
            ]
            results = await asyncio.gather(*tasks)
            
            await run_collector.on_chain_end(
                {"output": f"Processed {len(results)} documents"},
                run_id=batch_run_id
            )
            return results
            
        except Exception as e:
            await run_collector.on_chain_error(e, run_id=batch_run_id)
            raise e