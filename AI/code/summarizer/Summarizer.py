from dotenv import load_dotenv
import os
import asyncio
import aiohttp
from langchain_community.llms import OpenAI  # OpenAI import
from langchain_openai import ChatOpenAI
from langchain_core.prompts import load_prompt
from langchain_core.prompts import PromptTemplate
from langchain_core.messages import AIMessage  # AIMessage를 명확하게 import
from sample.sample2 import contexts
from langchain_teddynote import logging


load_dotenv()
# 프로젝트 이름을 입력합니다.
logging.langsmith("gas5-fp")

class Summarizer:
    def __init__(self):
        load_dotenv()
        self.api_key = os.getenv("OPENAI_API_KEY")  # 환경변수에서 API 키 로드
        self.llm = ChatOpenAI(
            temperature=0,
            model_name="gpt-4o-mini",  # 모델명
        )

    def get_prompt_template(self, usage: str) -> PromptTemplate:
        '''usage; 용도에 따라 다른 프롬프트 사용'''
        try:
            current_dir = os.path.dirname(__file__)
            prompt_path = os.path.join(current_dir, 'prompts', f'{usage}.yaml')
            prompt = load_prompt(prompt_path, encoding='utf-8')
            return prompt
        except FileNotFoundError:
            raise Exception(f"Prompt template for '{usage}' not found.")
        except Exception as e:
            raise Exception(f"Error loading prompt template: {str(e)}")

    async def fetch_openai_response(self, prompt: str) -> dict:
        '''OpenAI API에 비동기 요청을 보내고 응답을 반환.'''
        url = "https://api.openai.com/v1/chat/completions"
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json",
        }
        data = {
            "model": "gpt-4o",  # 사용할 모델
            "messages": [{"role": "user", "content": prompt}],
        }
        
        async with aiohttp.ClientSession() as session:
            async with session.post(url, headers=headers, json=data) as response:
                response.raise_for_status()  # HTTP 오류 발생 시 예외 발생
                return await response.json()

    async def restruct(self, contexts: list[str]) -> list[str]:
        '''AI가 이해하기 쉬운 형태로 재구성 (불필요한 정보 중복 제거 포함)'''
        results = []
        try:
            for context in contexts:
                print(f"Processing context: {context}")  # 현재 처리 중인 context 출력
                response = await self.fetch_openai_response(context)  # OpenAI API 호출
                print(f"Response: {response}")  # API 응답 출력
                
                if 'choices' in response and len(response['choices']) > 0:
                    result_content = response['choices'][0]['message']['content']
                    results.append(result_content)
                    
            return results
        except Exception as e:
            raise Exception(f"Error during reconstruction: {str(e)}")

    async def summarize(self, contexts: list[str]) -> list[tuple[str, str]]:
        '''텍스트 요약.'''
        results = []
        try:
            for context in contexts:
                response = await self.fetch_openai_response(context)
                if 'choices' in response and len(response['choices']) > 0:
                    result_content = response['choices'][0]['message']['content']
                    results.append((context, result_content))
            return results
        except Exception as e:
            raise Exception(f"Error during summarization: {str(e)}")

    async def verify(self, pairs: list[tuple[str, str]]) -> list[str]:
        '''원본과의 대조 검증.'''
        results = []
        try:
            for original, summary in pairs:
                response = await self.fetch_openai_response(f"Verify: {original} vs {summary}")
                if 'choices' in response and len(response['choices']) > 0:
                    result_content = response['choices'][0]['message']['content']
                    results.append(result_content)
            return results
        except Exception as e:
            raise Exception(f"Error during verification: {str(e)}")

    async def regenerate(self, context: str) -> str:
        '''요약을 다시 생성.'''
        try:
            response = await self.fetch_openai_response(context)
            if 'choices' in response and len(response['choices']) > 0:
                return response['choices'][0]['message']['content']
            return ""
        except Exception as e:
            raise Exception(f"Error during regeneration: {str(e)}")

    async def process(self, contexts: list[str]) -> list[tuple[str, str]]:
        '''전체 프로세스: 요약 -> 검증 -> 재생성'''
        restructured = await self.restruct(contexts)
        summaries = await self.summarize(restructured)
        verification_results = await self.verify(summaries)

        final_results = []
        for (original, summary), verification in zip(summaries, verification_results):
            if verification != "Valid":  # 검증 결과가 "Valid"가 아닐 경우 재생성
                regenerated_summary = await self.regenerate(original)
                final_results.append((original, regenerated_summary))
            else:
                final_results.append((original, summary))

        return final_results


async def main():
    summarizer = Summarizer()
    # 전체 프로세스 수행
    results = await summarizer.process(contexts)
    for original, summary in results:
        print(f"Original: {original}\nSummary: {summary}\n")

if __name__ == "__main__":
    asyncio.run(main())
