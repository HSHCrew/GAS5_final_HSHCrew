from dotenv import load_dotenv
import os
import asyncio
from typing import Dict, List, Tuple, Optional
from dataclasses import dataclass
from langchain_openai import ChatOpenAI
from langchain_core.prompts import load_prompt, PromptTemplate
from langchain_core.output_parsers import JsonOutputParser
from langchain_teddynote import logging
from pydantic import BaseModel, Field

@dataclass
class ProcessResult:
    '''Process 결과 데이터 구조'''
    index: Optional[int] = None
    restructured: str = ""
    summary: str = ""
    fewshots: str = ""
    failed: str | int = 0

class Topic(BaseModel):
    '''verify 결과 데이터 구조'''
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

     async def get_prompt_template(self, usage: str) -> PromptTemplate:
        '''목적에 따른 프롬프트 호출.'''
        try:
            prompt_path = os.path.join(self.current_dir, 'prompts', f'{usage}.yaml')
            return load_prompt(prompt_path, encoding='utf-8')
        except FileNotFoundError:
            raise ValueError(f"Prompt template '{usage}' not found")
        except Exception as e:
            raise ValueError(f"Error loading prompt template: {str(e)}")

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

    async def verify(self, pair: tuple) -> dict:
        '''원본과의 대조 검증.'''
        parser = JsonOutputParser(pydantic_object=Topic)
        prompt = await self.get_prompt_template('verify')
        original, summary = pair
        
        try:
            chain = prompt | self.llm
            result = await chain.ainvoke({
                'original_content': original,
                'summary': summary
            })
            return await self.parser.aparse(result.content)
        except Exception as e:
            raise ValueError(f"Verification failed: {str(e)}")

    async def regenerate(self, pair: Tuple[str, str]) -> str:
        """피드백과 이전 시도를 기반으로 요약 재생성

        Args:
            pair: (원본 텍스트, 몇 가지 예시)를 포함하는 튜플
            original_text: 요약할 원본 내용
            fewshot_examples: 이전 요약과 그에 대한 피드백을 포함하는 문자열
        
        Returns:
            str: 피드백을 반영한 개선된 요약
        """
        prompt = await self.get_prompt_template('regenerate')
        original, fewshots = pair
        
        try:
            chain = prompt | self.llm
            result = await chain.ainvoke({
                'original': original,
                'fewshots': fewshots
            })
            return result.content
        except Exception as e:
            raise ValueError(f"Regeneration failed: {str(e)}")

    async def process_single(self, context: str, index: Optional[int] = None) -> ProcessResult:
        """Process single context with verification and regeneration"""
        result = ProcessResult(index=index)
        fewshots = ""
        
        try:
            result.restructured = await self.restruct(context)
            result.summary = await self.summarize(result.restructured)
            verification = await self.verify((result.restructured, result.summary))
            
            for attempt in range(2):  # 반복횟수 설정
                if verification['score'] < 80:
                    # 전달할 fewshot 포맷.
                    fewshots += (
                        f"Previous Summary (Attempt {attempt + 1}):\n"
                        f"{result.summary}\n"
                        f"Feedback:\n"
                        f"{verification['feedback']}\n"
                        f"---\n"
                    )
                    result.summary = await self.regenerate((result.restructured, fewshots))
                    verification = await self.verify((result.restructured, result.summary))
                else:
                    break
            
            result.fewshots = fewshots
            return result
            
        except Exception as e:
            result.failed = str(e)
            return result

    async def mono_processes(self, contexts: list[str]) -> list[dict]:
        '''각각의 컨텍스트에 대해 비동기적으로 process를 수행'''
        tasks = [self.process_single(context, index) for index, context in enumerate(contexts)]  # 각 context에 대해 process 메서드 호출
        results = await asyncio.gather(*tasks)  # 모든 작업을 비동기적으로 실행
        return results
    
    

# 테스트용 코드
async def main():
    summarizer = Summarizer()
    import sample.sample1 as sample  # contexts를 가져옵니다.
    # 전체 프로세스 수행
    results = await summarizer.process(sample.context2)
    print(f"Original: {results['restructed']}\nSummary: {results['summary']}\n")

if __name__ == "__main__":
    asyncio.run(main())
