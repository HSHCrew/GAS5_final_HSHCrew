from typing import Optional
from langchain_openai import ChatOpenAI
from langchain_core.output_parsers import JsonOutputParser
from .models import Topic
from .prompt_manager import PromptManager
from langchain_core.runnables import RunnableConfig

class LLMProcessor:
    def __init__(self, llm: ChatOpenAI, prompt_manager: PromptManager):
        self.llm = llm
        self.prompt_manager = prompt_manager
        self.parser = JsonOutputParser(pydantic_object=Topic)

    async def restruct(self, context: str, config: Optional[RunnableConfig] = None) -> str:
        prompt = await self.prompt_manager.get_prompt_template('restruct')
        try:
            chain = prompt | self.llm
            return (await chain.ainvoke(context, config=config)).content
        except Exception as e:
            raise Exception(f"Error during reconstruction: {str(e)}")

    async def summarize(self, context: str, config: Optional[RunnableConfig] = None) -> str:
        prompt = await self.prompt_manager.get_prompt_template('summarize')
        try:
            chain = prompt | self.llm
            return (await chain.ainvoke(context, config=config)).content
        except Exception as e:
            raise Exception(f"Error during summarization: {str(e)}")

    async def verify(self, pair: tuple, config: Optional[RunnableConfig] = None) -> dict:
        prompt = await self.prompt_manager.get_prompt_template('verify')
        original, summary = pair
        try:
            chain = prompt | self.llm
            result = await chain.ainvoke({
                'original_content': original,
                'summary': summary
            }, config=config)
            return await self.parser.aparse(result.content)
        except Exception as e:
            raise ValueError(f"Verification failed: {str(e)}")

    async def regenerate(self, pair: tuple[str, str], config: Optional[RunnableConfig] = None) -> str:
        prompt = await self.prompt_manager.get_prompt_template('regenerate')
        original, fewshots = pair
        try:
            chain = prompt | self.llm
            result = await chain.ainvoke({
                'original': original,
                'fewshots': fewshots
            }, config=config)
            return result.content
        except Exception as e:
            raise ValueError(f"Regeneration failed: {str(e)}")