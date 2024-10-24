from dotenv import load_dotenv
import os
import asyncio
from langchain.llms import OpenAI
from langchain_openai import ChatOpenAI
from langchain_core.prompts import load_prompt
from langchain_core.prompts import PromptTemplate

class Summarizer:
    def __init__(self):
        load_dotenv()
        self.llm = ChatOpenAI(
            temperature=0,
            model_name="gpt-4o",  # 모델명
        )

    def get_prompt_template(self, usage: str) -> PromptTemplate:
        '''usage; 용도에 따라 다른 프롬프트 사용'''
        try:
            prompt = load_prompt(f'prompts/{usage}.yaml', encoding='utf-8')
            return prompt
        except FileNotFoundError:
            raise Exception(f"Prompt template for '{usage}' not found.")
        except Exception as e:
            raise Exception(f"Error loading prompt template: {str(e)}")

    async def restruct(self, context: str) -> str:
        '''AI가 이해하기 쉬운 형태로 재구조화 (불필요한 정보 중복 제거 포함)'''
        prompt = self.get_prompt_template('restruct')
        chain = prompt | self.llm
        try:
            result = await chain.invoke(context)
            return result.content
        except Exception as e:
            raise Exception(f"Error during reconstruction: {str(e)}")

    async def summarize(self, context: str) -> list:
        '''텍스트 요약.'''
        prompt = self.get_prompt_template('summarize')
        chain = prompt | self.llm
        try:
            result = await chain.invoke(context)
            return [context, result.content]
        except Exception as e:
            raise Exception(f"Error during summarization: {str(e)}")

    async def verify(self, pair: list) -> str:
        '''원본과의 대조 검증.'''
        prompt = self.get_prompt_template('verify')
        chain = prompt | self.llm
        try:
            result = await chain.invoke({'original_content': pair[0], 'summary': pair[1]})
            return result.content
        except Exception as e:
            raise Exception(f"Error during verification: {str(e)}")
        
    async def restruct(self, context: list[str]) -> list:
        pass
            
            
    # 대조->생성으로 진행하려면 얘네는 통으로 하는 것 보다는 둘을 하나로 합치는 게 나을듯?
    async def summarize(self, context: list[str]) -> list:
        pass        
    
    async def verify(self, context: list[list]) -> list:
        pass
    