from langchain_openai import ChatOpenAI
from langchain.prompts import ChatPromptTemplate
from langchain.tools import tool
from typing import List, Dict
import json
import os
from dotenv import load_dotenv
from langchain_teddynote import logging
import asyncio

load_dotenv()

logging.langsmith(project_name="gas5fp_curator")

# ChatGPT 모델 초기화
generate_chat = ChatOpenAI(
    model="gpt-4o",
    temperature=0.3,
    openai_api_key=os.getenv('OPENAI_API_KEY')
)

verify_chat = ChatOpenAI(
    model="gpt-4o",
    temperature=0.0,
    openai_api_key=os.getenv('OPENAI_API_KEY')
)

SUMMARIZE_PROMPT_TEMPLATE = """### task
  You are a medical news analyst tasked with creating clear, accessible summaries of medical research articles.
  check follwing key_questions when reading the article.

  ### key_questions
  {key_questions}
  
  ### original article
  Original Article:
  {article_content}

  ### writing guidelines
  Follow these guidelines to create an informative summary:

  1. **Key Research Findings**
     - Main discoveries or advancements
     - Significance of the findings
     - Potential impact on medical practice

  2. **Methodology Highlights**
     - Key research methods used
     - Study scope and participants
     - Important technical details (in simple terms)

  3. **Clinical Implications**
     - Practical applications
     - Benefits for patients
     - Potential limitations or challenges

  ### writing guidelines
  - Use clear, non-technical language
  - Maintain scientific accuracy
  - Focus on practical implications
  - Keep sentences concise
  - Avoid unnecessary jargon

  ### return format
  Return a clear, narrative summary which efficiently reflects the original article.
  maximum output token count: 800
  """
  
VERIFICATION_PROMPT_TEMPLATE = """### task
You are a medical content reviewer tasked with verifying the accuracy and completeness of article summaries.

### original-summary pair
- Original Article:
{original_content}

- Summary to Review:
{summary}

### verification steps
1. Consistency Check
    - Are key points accurately reflected?
    - Do any details contradict the original?
    - Is the scientific meaning preserved?

2. Completeness Check
    - Are all essential findings included?
    - Is the research context maintained?
    - Are important limitations mentioned?

3. Clarity Assessment
    - Is the language appropriate for the target audience?
    - Are complex concepts explained clearly?
    - Is the information structure logical?

4. Overall Evaluation
    - Score the summary from 0 to 100
    - Provide specific feedback for improvements

### return format
```
{{"score": <integer_between_0_and_100>, "feedback": "<detailed_feedback>"}}
```
### few-shot examples
Here are some examples:

- example 1
- Original Content: The global economy saw a significant recovery in 2023, with most regions experiencing increased growth rates compared to previous years. Major sectors such as technology, healthcare, and energy led the recovery, while some industries, including tourism and hospitality, continued to struggle. Inflation remained a concern in several major economies, though it showed signs of stabilizing in the latter half of the year.
- Summary: The global economy improved in 2023, with strong growth in technology, healthcare, and energy sectors. Inflation, while still a problem, began to stabilize.
- Response:
    ```
    {{"score": 75, "feedback": "Include the information about the tourism and hospitality sectors still struggling to provide a more complete summary."}}
    ```
    
- example 2
- Original Content: Researchers have discovered a new species of deep-sea fish in the Pacific Ocean. This species, which dwells at depths of over 1,500 meters, has adapted to the extreme conditions with specialized features such as bioluminescence and a unique bone structure. The discovery offers new insights into deep-sea biodiversity and the evolutionary processes that enable life in such harsh environments.
- Summary: A new deep-sea fish species has been found in the Pacific Ocean. It has special features to survive deep underwater, and this discovery helps scientists learn more about deep-sea life.
- Response:
    ```
    {{"score": 85, "feedback": "Valid"}}
    ```

- example 3
- Original Content: The company announced that its revenue grew by 15% in the third quarter of 2024, driven by increased demand for its cloud computing services and digital transformation solutions. However, the company also noted that supply chain disruptions affected its hardware division, leading to lower-than-expected sales in that sector.
- Summary: The company saw a 15% revenue increase in Q3 2024, mainly due to high demand for its cloud services, though its hardware sales were impacted by supply chain issues.
- Response:
    ```
    {{"score": 95, "feedback": "Valid"}}
    ```
"""

REGENERATE_PROMPT_TEMPLATE = """### task
You are a medical content specialist tasked with improving article summaries based on feedback.

### original article
Original Article:
  {original}

### previous attempts and feedback
Previous Attempts and Feedback:
{fewshots}

### improvement process
1. Review previous feedback carefully
2. Address all identified issues
3. Maintain accuracy while improving clarity
4. Ensure completeness of key information
5. Optimize for readability

### create a new summary
Create a new summary that:
- Incorporates all feedback
- Maintains scientific accuracy
- Uses clear, accessible language
- Covers all essential points
- Flows naturally and logically

### return format
Return only the improved summary without any formatting markers or labels.
"""

@tool("process_articles", return_direct=True)
async def process_articles(articles: List[Dict]) -> List[Dict]:
    """
    Processes multiple articles by generating summaries and verifying them.
    Each article is processed sequentially (summary -> verification),
    but multiple articles are processed in parallel.
    
    Args:
        articles (List[Dict]): List of dictionaries containing article content and metadata
        
    Returns:
        List[Dict]: List of articles with added summaries and verifications
    """
    async def process_single_article(article: Dict, max_retries: int = 3, retry_delay: float = 1.0) -> Dict:
        """
        단일 기사에 대한 요약 생성 및 검증을 순차적으로 수행
        
        Args:
            article (Dict): 처리할 기사 데이터
            max_retries (int): 최대 재시도 횟수
            retry_delay (float): 재시도 간 대기 시간(초)
        """
        async def attempt_summary(article: Dict) -> Dict:
            """요약 생성 시도"""
            prompt = ChatPromptTemplate.from_template(SUMMARIZE_PROMPT_TEMPLATE)
            messages = prompt.format_messages(
                article_content=f"title: {article['title']}\ncontent: {article['content']}\n",
                key_questions=article.get('key_questions', '')
            )
            response = await generate_chat.ainvoke(messages)
            return response.content

        async def attempt_verification(article: Dict, summary: str) -> Dict:
            """검증 시도"""
            try:
                prompt = ChatPromptTemplate.from_template(VERIFICATION_PROMPT_TEMPLATE)
                messages = prompt.format_messages(
                    original_content=f"title: {article['title']}\ncontent: {article['content']}\n",
                    summary=summary
                )
                verification = await verify_chat.ainvoke(messages)
                
                # # 디버깅을 위해 원본 응답 출력
                # print(f"\nVerification response for '{article['title']}':")
                # print(verification.content)
                
                # 응답에서 JSON 부분 추출
                content = verification.content.strip()
                
                # 코드 블록 제거
                if '```' in content:
                    # 첫 번째 코드 블록 찾기
                    start = content.find('```') + 3
                    end = content.find('```', start)
                    if 'json' in content[start:start+10]:  # json 표시가 있으면 제거
                        start = content.find('\n', start) + 1
                    content = content[start:end].strip()
                
                # JSON 파싱
                verification_dict = json.loads(content)
                return {
                    'score': verification_dict.get('score', 0),
                    'feedback': verification_dict.get('feedback', 'No feedback provided')
                }
                
            except json.JSONDecodeError as e:
                print(f"JSON parsing error for '{article['title']}': {str(e)}")
                print(f"Attempted to parse: {content}")
                return {
                    'score': 0,
                    'feedback': f"Failed to parse verification response: {verification.content}"
                }
            except Exception as e:
                print(f"Verification error for '{article['title']}': {str(e)}")
                print(f"Full error: {e.__class__.__name__}: {str(e)}")
                return {
                    'score': 0,
                    'feedback': f"Verification failed: {str(e)}"
                }
                
        async def attempt_regenerate(article: Dict, fewshots: list[dict[str]]) -> str:
            """재생성 시도"""
            prompt = ChatPromptTemplate.from_template(REGENERATE_PROMPT_TEMPLATE)
            messages = prompt.format_messages(
                original=f"title: {article['title']}\ncontent: {article['content']}\n",
                fewshots=fewshots
            )
            response = await generate_chat.ainvoke(messages)
            return response.content

        if not article.get('content'):
            print(f"Skipping article '{article.get('title', 'Unknown')}' due to missing content")
            article['processing_error'] = "Missing content"
            return article

        article_with_summary = article.copy()
        
        # 요약 생성 시도
        for attempt in range(max_retries):
            try:
                if attempt > 0:
                    print(f"Retrying summary generation for '{article['title']}' (attempt {attempt + 1}/{max_retries})")
                    await asyncio.sleep(retry_delay * attempt)  # 지수 백오프
                
                summary = await attempt_summary(article)
                article_with_summary['generated_summary'] = summary
                break
            except Exception as e:
                if attempt == max_retries - 1:
                    print(f"Failed to generate summary for '{article['title']}' after {max_retries} attempts: {str(e)}")
                    article_with_summary['processing_error'] = f"Summary generation failed: {str(e)}"
                    return article_with_summary
        
        # 검증 및 재생성
        for attempt in range(max_retries):
            try:
                if attempt > 0:
                    print(f"Retrying verification for '{article['title']}' (attempt {attempt + 1}/{max_retries})")
                    await asyncio.sleep(retry_delay * attempt)  # 지수 백오프
                fewshots = []
                
                verification = await attempt_verification(article, article_with_summary['generated_summary'])
                article_with_summary['verification'] = verification
                
                for i in range(2):
                    if verification['score'] < 80:
                        fewshots.append({
                            "original_summary": article_with_summary['generated_summary'],
                            "feedback": verification['feedback']
                        }) 
                        article_with_summary['fewshots'] = fewshots
                        summary = await attempt_regenerate(article_with_summary, fewshots)
                        article_with_summary['generated_summary'] = summary
                        
                        verification = await attempt_verification(article, article_with_summary['generated_summary'])
                        article_with_summary['verification'] = verification
                    else:
                        break
                break
            
            except Exception as e:
                if attempt == max_retries - 1:
                    print(f"Failed to verify summary for '{article['title']}' after {max_retries} attempts: {str(e)}")
                    article_with_summary['processing_error'] = f"Verification failed: {str(e)}"
                    return article_with_summary
        
        # 성공적으로 처리된 경우
        article_with_summary['processing_status'] = 'success'
        return article_with_summary
    
    try:
        # 여러 기사를 병렬로 처리 (각 기사 내부는 순차 처리)
        result_articles = await asyncio.gather(*[process_single_article(article) for article in articles])
        return result_articles
        
    except Exception as e:
        print(f"Error processing articles: {str(e)}")
        return articles

# 사용 예시
if __name__ == "__main__":
    async def main():
        try:
            # 테스트용 기사 데이터 로드
            path = "./data/medi_xpress/medicalxpress_full_articles_20241119_000025.json"
            with open(path, 'r', encoding='utf-8') as f:
                articles = json.load(f)[:2]
            
            # 도구 실행
            tool = process_articles
            result_articles = await tool.ainvoke({"articles": articles})
            
            if result_articles:
                with open(path, 'w', encoding='utf-8') as f:
                    json.dump(result_articles, f, ensure_ascii=False, indent=4)
                print(f"Successfully processed {len(result_articles)} articles")
            else:
                print("Failed to process articles")
                
        except Exception as e:
            print(f"Error: {str(e)}")
    
    # 비동기 실행
    asyncio.run(main())
