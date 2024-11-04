from dotenv import load_dotenv
import os
import asyncio
from typing import List, Optional
from langchain_openai import ChatOpenAI
from langchain_teddynote import logging
from .models import ProcessResult
from .prompt_manager import PromptManager
from .llm_processor import LLMProcessor

class Summarizer:
    def __init__(self):
        load_dotenv()
        logging.langsmith("gas5-fp")
        
        self.llm = ChatOpenAI(
            temperature=0,
            model_name="gpt-4o-mini",
            api_key=os.getenv("OPENAI_API_KEY")
        )
        
        self.prompt_manager = PromptManager(os.path.dirname(__file__))
        self.processor = LLMProcessor(self.llm, self.prompt_manager)

    async def process_single(self, context: str, index: Optional[int] = None) -> ProcessResult:
        result = ProcessResult(index=index)
        fewshots = ""
        
        try:
            result.restructured = await self.processor.restruct(context)
            result.summary = await self.processor.summarize(result.restructured)
            verification = await self.processor.verify((result.restructured, result.summary))
            
            for attempt in range(2):
                if verification['score'] < 80:
                    fewshots += (
                        f"Previous Summary (Attempt {attempt + 1}):\n"
                        f"{result.summary}\n"
                        f"Feedback:\n"
                        f"{verification['feedback']}\n"
                        f"---\n"
                    )
                    result.summary = await self.processor.regenerate((result.restructured, fewshots))
                    verification = await self.processor.verify((result.restructured, result.summary))
                else:
                    break
            
            result.fewshots = fewshots
            return result
            
        except Exception as e:
            result.failed = str(e)
            return result

    async def mono_processes(self, contexts: List[str]) -> List[ProcessResult]:
        tasks = [self.process_single(context, index) for index, context in enumerate(contexts)]
        return await asyncio.gather(*tasks) 