from typing import Optional
from langchain_openai import ChatOpenAI
from langchain_core.output_parsers import JsonOutputParser
from langchain_core.runnables import RunnableConfig, RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser
from .models import Topic
from .prompt_manager import PromptManager

class LLMProcessor:
    def __init__(self, llm: ChatOpenAI, prompt_manager: PromptManager):
        self.llm = llm
        self.prompt_manager = prompt_manager
        self.parser = JsonOutputParser(pydantic_object=Topic)

    async def restruct(self, context: str, config: Optional[RunnableConfig] = None) -> str:
        prompt = await self.prompt_manager.get_prompt_template('restruct')
        try:
            chain = (
                RunnablePassthrough() 
                | prompt 
                | self.llm 
                | StrOutputParser()
            ).with_config(
                run_name="Restructure Text",
                tags=["restructure"]
            )
            
            return await chain.ainvoke(
                context, 
                config=config
            )
        except Exception as e:
            raise Exception(f"Error during reconstruction: {str(e)}")

    async def summarize(self, context: str, config: Optional[RunnableConfig] = None) -> str:
        prompt = await self.prompt_manager.get_prompt_template('summarize')
        try:
            chain = (
                RunnablePassthrough() 
                | prompt 
                | self.llm 
                | StrOutputParser()
            ).with_config(
                run_name="Generate Summary",
                tags=["summarize"]
            )
            
            return await chain.ainvoke(
                context, 
                config=config
            )
        except Exception as e:
            raise Exception(f"Error during summarization: {str(e)}")

    async def verify(self, pair: tuple, config: Optional[RunnableConfig] = None) -> dict:
        prompt = await self.prompt_manager.get_prompt_template('verify')
        original, summary = pair
        try:
            chain = (
                RunnablePassthrough() 
                | prompt 
                | self.llm 
                | self.parser
            ).with_config(
                run_name="Verify Summary",
                tags=["verify"]
            )
            
            return await chain.ainvoke(
                {
                    'original_content': original,
                    'summary': summary
                }, 
                config=config
            )
        except Exception as e:
            raise ValueError(f"Verification failed: {str(e)}")

    async def regenerate(self, pair: tuple[str, str], config: Optional[RunnableConfig] = None) -> str:
        prompt = await self.prompt_manager.get_prompt_template('regenerate')
        original, fewshots = pair
        try:
            chain = (
                RunnablePassthrough() 
                | prompt 
                | self.llm 
                | StrOutputParser()
            ).with_config(
                run_name="Regenerate Summary",
                tags=["regenerate"]
            )
            
            return await chain.ainvoke(
                {
                    'original': original,
                    'fewshots': fewshots
                }, 
                config=config
            )
        except Exception as e:
            raise ValueError(f"Regeneration failed: {str(e)}")