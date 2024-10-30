from dotenv import load_dotenv
import os
import asyncio
from langchain_openai import ChatOpenAI
from langchain_core.prompts import load_prompt
from langchain_core.prompts import PromptTemplate
from langchain_core.prompts.few_shot import FewShotPromptTemplate
from langchain_teddynote import logging
import sample.sample1 as sample  # contexts를 가져옵니다.
from pydantic import BaseModel, Field
from langchain_core.output_parsers import JsonOutputParser

class Topic(BaseModel):
    score: int = Field(description="Overall Assessment Score  [0,100]")
    feedback: str = Field(description="feedback detailing what needs to be improved or added to ensure the summary accurately reflects the original content")

class Summarizer:
    def __init__(self):
        load_dotenv()
        logging.langsmith("gas5-fp")
        self.api_key = os.getenv("OPENAI_API_KEY")
        self.llm = ChatOpenAI(
            temperature=0,
            model_name="gpt-4o-mini",
        )
        self.current_dir = os.path.dirname(__file__)

    def get_prompt_template(self, usage: str) -> PromptTemplate:
        '''usage; 용도에 따라 다른 프롬프트 사용'''
        try:
            prompt_path = os.path.join(self.current_dir, 'prompts', f'{usage}.yaml')
            prompt = load_prompt(prompt_path, encoding='utf-8')
            return prompt
        except FileNotFoundError:
            raise Exception(f"Prompt template for '{usage}' not found.")
        except Exception as e:
            raise Exception(f"Error loading prompt template: {str(e)}")

    async def restruct(self, context: str) -> str:
        '''AI가 이해하기 쉬운 형태로 재구성 (불필요한 정보 중복 제거 포함)'''
        prompt = self.get_prompt_template('restruct')
        try:
            chain = prompt | self.llm
            return (await chain.ainvoke(context)).content
        except Exception as e:
            raise Exception(f"Error during reconstruction: {str(e)}")

    async def summarize(self, context: str) -> str:
        '''텍스트 요약.'''
        prompt = self.get_prompt_template('summarize')
        chain = prompt | self.llm
        try:
            return (await chain.ainvoke(context)).content
        except Exception as e:
            raise Exception(f"Error during summarization: {str(e)}")

    async def verify(self, pair: tuple) -> list:
        '''원본과의 대조 검증.'''
        parser = JsonOutputParser(pydantic_object=Topic)
        prompt = self.get_prompt_template('verify')
        original, summary = pair
        chain = prompt | self.llm | parser
        try:
            result = await chain.ainvoke({'original_content': original, 'summary': summary})
            return [result.content, original, summary]
        except Exception as e:
            raise Exception(f"Error during verification: {str(e)}")

    async def regenerate(self, pair : tuple) -> list:
        '''요약을 다시 생성.'''
        prompt = self.get_prompt_template('regenerate')
        original, fewshots = pair
        chain = prompt | self.llm
        try:
            result = await chain.ainvoke({'original':original, 'fewshots':fewshots}) 
            return [result.content, fewshots]
        except Exception as e:
            raise Exception(f"Error during regenerate: {str(e)}")
        
    async def process(self, context: str) -> dict:
        '''전체 프로세스: 요약 -> 검증 -> 재생성'''
        fewshots, restructured, summary = str()
        verification_result = list()
        
        try:
            restructured = await self.restruct(context)
            summary = await self.summarize(restructured)
            verification_result = await self.verify((restructured, summary))
            
            for _ in range(2):
                if verification_result['score'] < 80:  # 검증 점수가 80 미만일 경우
                    fewshot = f"Summary:\n{summary}\nFeedback:\n{verification_result['Feedback']}\n\n"
                    fewshots += fewshot
                    summary = await self.regenerate((restructured, fewshots))
                    verification_result = await self.verify((restructured, summary))
                else:
                    break

            return {
                'restructured': restructured,
                'summary': summary,
                'fewshots': fewshots,
                'failed': 0
            }
        except Exception as e:
            raise Exception(f"Error during process: {str(e)}")
                  
            
    async def process(self, context: str, index: int) -> dict:
        '''전체 프로세스: 요약 -> 검증 -> 재생성 ; 여러 context가 리스트 입력된 경우 처리'''
        fewshots, restructured, summary = str()
        verification_result = list()
        
        try:
            restructured = await self.restruct(context)
            summary = await self.summarize(restructured)
            verification_result = await self.verify((restructured, summary))
            
            for _ in range(2):
                if verification_result['score'] < 80:  # 검증 점수가 80 미만일 경우
                    fewshot = f"Summary:\n{summary}\nFeedback:\n{verification_result['Feedback']}\n\n"
                    fewshots += fewshot
                    summary = await self.regenerate((restructured, fewshots))
                    verification_result = await self.verify((restructured, summary))
                else:
                    break

            return {
                'index': index,
                'restructured': restructured,
                'summary': summary,
                'fewshots': fewshots,
                'failed': 0
            }
        except Exception as e:
            print(f"Error during process: {index} - {str(e)}")
            return {
                'index': index,
                'restructured': restructured,
                'summary': summary,
                'fewshots': fewshots,
                'failed': str(e)
            }

    async def mono_processes(self, contexts: list[str]) -> list[dict]:
        '''각각의 컨텍스트에 대해 비동기적으로 process를 수행'''
        tasks = [self.process(context, index) for index, context in enumerate(contexts)]  # 각 context에 대해 process 메서드 호출
        results = await asyncio.gather(*tasks)  # 모든 작업을 비동기적으로 실행
        return results
    
    

# 테스트용 코드
async def main():
    summarizer = Summarizer()
    # 전체 프로세스 수행
    results = await summarizer.process(sample.context2)
    print(f"Original: {results['restructed']}\nSummary: {results['summary']}\n")

if __name__ == "__main__":
    asyncio.run(main())
