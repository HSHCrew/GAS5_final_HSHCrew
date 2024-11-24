from langchain_openai import ChatOpenAI
from langchain.prompts import ChatPromptTemplate
from typing import List, Dict
import json
import os
from dotenv import load_dotenv
from datetime import datetime
import asyncio
from langchain_teddynote import logging

load_dotenv()
logging.langsmith(project_name="gas5fp_curator_translation")

chat = ChatOpenAI(
    model="gpt-4o",
    temperature=0.3,
    openai_api_key=os.getenv('OPENAI_API_KEY')
)

TRANSLATION_PROMPT_TEMPLATE = """### Task
You are a medical content translator and editor specializing in making complex medical information accessible to Korean readers.

### Original Content
{content}

### Instructions
1. Translation Guidelines:
   - Translate the content into natural, fluent Korean
   - Maintain medical accuracy while simplifying complex terms
   - Add brief explanations for medical terminology in parentheses when needed
   - Keep the original structure and flow

2. Readability Guidelines:
   - Use clear, everyday language when possible
   - Break down complex medical concepts
   - Maintain a friendly, informative tone
   - Ensure the content is engaging for general readers

3. Format Requirements:
   - Keep the same section structure
   - Use appropriate Korean formatting conventions
   - Include both Korean and English terms for key medical concepts where helpful

### Output Format
Title: [한국어 제목]

개요:
[주제 소개 및 중요성]

[주요 내용]
- 섹션별 번역된 내용
- 의학 용어 설명 포함
- 독자 친화적 설명

결론:
[요약 및 시사점]"""

VERIFICATION_PROMPT_TEMPLATE = """### Task
You are a medical content reviewer specializing in Korean medical content. Verify the quality and accuracy of the translated medical newsletter.

### Original Content (English)
{original_content}

### Translated Content (Korean)
{translated_content}

### Verification Steps
1. Translation Accuracy
   - Are medical terms correctly translated?
   - Is the meaning preserved accurately?
   - Are explanations culturally appropriate?

2. Readability & Accessibility
   - Is the Korean natural and fluent?
   - Are medical concepts explained clearly?
   - Is the content engaging for Korean readers?

3. Technical Accuracy
   - Are medical facts preserved in translation?
   - Are parenthetical explanations accurate?
   - Are Korean medical terms consistent?

### Return Format
```
{{"score": <integer_between_0_and_100>, "feedback": "<specific_feedback_for_improvements>"}}
```
"""

REGENERATE_PROMPT_TEMPLATE = """### Task
You are a medical content translator tasked with improving the Korean translation based on reviewer feedback.

### Original Content (English)
{original_content}

### Current Translation
{current_translation}

### Reviewer Feedback
{feedback}

### Previous Attempts
{previous_attempts}

### Instructions
1. Address all points in the reviewer feedback
2. Maintain accurate medical terminology
3. Keep natural Korean flow
4. Ensure accessibility for general readers

### Output
Provide an improved Korean translation incorporating the feedback."""


async def translate_curation(curation_path: str, max_retries: int = 3, retry_delay: int = 2) -> Dict:
    """큐레이션을 한국어로 번역하고 검증/재생성"""
    try:
        with open(curation_path, 'r', encoding='utf-8') as f:
            curation = json.load(f)
            
        async def attempt_verification(original: str, translation: str) -> Dict:
            """번역 검증"""
            prompt = ChatPromptTemplate.from_template(VERIFICATION_PROMPT_TEMPLATE)
            messages = prompt.format_messages(
                original_content=original,
                translated_content=translation
            )
            verification = await chat.ainvoke(messages)
            
            try:
                content = verification.content
                if '```' in content:
                    start = content.find('```') + 3
                    end = content.find('```', start)
                    if 'json' in content[start:start+10]:
                        start = content.find('\n', start) + 1
                    content = content[start:end].strip()
                
                return json.loads(content)
            except Exception as e:
                print(f"Verification parsing error: {str(e)}")
                return {'score': 0, 'feedback': str(e)}

        async def attempt_regenerate(original: str, current: str, feedback: str, previous_attempts: List[Dict]) -> str:
            """번역 재생성"""
            previous_attempts_text = "\n\n".join([
                f"Attempt {i+1}:\n{attempt['translation']}\nFeedback: {attempt['feedback']}"
                for i, attempt in enumerate(previous_attempts)
            ])
            
            prompt = ChatPromptTemplate.from_template(REGENERATE_PROMPT_TEMPLATE)
            messages = prompt.format_messages(
                original_content=original,
                current_translation=current,
                feedback=feedback,
                previous_attempts=previous_attempts_text
            )
            response = await chat.ainvoke(messages)
            return response.content

        # 초기 번역 및 검증-재생성 루프
        translation_content = None
        verification_history = []
        
        for attempt in range(max_retries):
            if attempt > 0:
                print(f"Retrying translation (attempt {attempt + 1}/{max_retries})")
                await asyncio.sleep(retry_delay * attempt)
            
            if not translation_content:
                prompt = ChatPromptTemplate.from_template(TRANSLATION_PROMPT_TEMPLATE)
                messages = prompt.format_messages(content=curation['content'])
                response = await chat.ainvoke(messages)
                translation_content = response.content
            
            # 검증
            verification = await attempt_verification(curation['content'], translation_content)
            verification_history.append({
                'translation': translation_content,
                'feedback': verification['feedback'],
                'score': verification['score']
            })
            
            # 점수가 충분히 높으면 종료
            if verification['score'] >= 85:
                break
            
            # 재생성 시도
            if attempt < max_retries - 1:
                translation_content = await attempt_regenerate(
                    curation['content'],
                    translation_content,
                    verification['feedback'],
                    verification_history
                )

        # 결과 저장
        curation['korean_content'] = translation_content
        curation['translation_verification_history'] = verification_history
        
        output_dir = os.path.dirname(curation_path)
        filename = os.path.basename(curation_path)
        output_path = os.path.join(output_dir, f"translated_{filename}")
        
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(curation, f, ensure_ascii=False, indent=4)
        
        print(f"Successfully translated and saved to: {output_path}")
        return curation
            
    except Exception as e:
        print(f"Error translating curation: {str(e)}")
        return None

async def main():
    """큐레이션 디렉토리의 모든 파일을 번역"""
    curation_dir = "./data/curations"
    today = datetime.now().strftime('%Y%m%d')
    
    tasks = []
    for filename in os.listdir(curation_dir):
        if not filename.endswith(f'{today}.json') or filename.startswith('translated_'):
            continue
            
        file_path = os.path.join(curation_dir, filename)
        tasks.append(translate_curation(file_path))
    
    if tasks:
        results = await asyncio.gather(*tasks)
        print(f"\nCompleted translation for {len(results)} curations")
    else:
        print("No curations found for translation")

if __name__ == "__main__":
    asyncio.run(main()) 