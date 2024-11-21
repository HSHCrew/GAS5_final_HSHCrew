from langchain_openai import ChatOpenAI
from langchain.prompts import ChatPromptTemplate
from langchain.tools import tool
from typing import List, Dict
import json
import os
from dotenv import load_dotenv
from datetime import datetime
from langchain_teddynote import logging

load_dotenv()
logging.langsmith(project_name="gas5fp_curator_curation")

chat = ChatOpenAI(
    model="gpt-4o",
    temperature=0.3,
    openai_api_key=os.getenv('OPENAI_API_KEY')
)

CURATION_PROMPT_TEMPLATE = """### Task
You are a medical newsletter writer. Create a comprehensive newsletter based on the provided Q&A pairs from medical news articles.

### Context
Search Term (Topic): {search_term}
Q&A Pairs:
{qa_pairs}

### Instructions
Create a newsletter that:
1. Has a clear and engaging title related to the search term
2. Starts with a brief overview of the topic
3. Organizes information from Q&A pairs into coherent sections
4. Highlights key findings and implications
5. Maintains scientific accuracy while being accessible
6. Includes a brief conclusion or future outlook

### Format
Title: [Newsletter Title]

Overview:
[Brief introduction to the topic and why it's important]

[Main Content Sections]
- Use appropriate subheadings
- Integrate information from Q&A pairs
- Highlight key points
- Connect related findings

Conclusion:
[Summary and implications]

### Output
Please write the newsletter in a clear, engaging style suitable for informed readers interested in medical topics."""

VERIFICATION_PROMPT_TEMPLATE = """### Task
You are a medical content reviewer tasked with verifying the quality of medical newsletters.

### Content to Review
{content}

### Verification Steps
1. Scientific Accuracy
   - Are medical facts correctly presented?
   - Is the information up-to-date and well-supported?

2. Content Organization
   - Is the information logically structured?
   - Are sections well-connected and coherent?

3. Clarity and Accessibility
   - Is the language appropriate for the target audience?
   - Are complex concepts explained clearly?

4. Completeness
   - Are all key points from the Q&A pairs covered?
   - Is there a good balance of information?

5. Overall Impact
   - Is the newsletter engaging and informative?
   - Does it provide valuable insights for readers?

### Return Format
json
```
{{"score": <integer_between_0_and_100>,, "feedback": "<specific_feedback_for_improvements>"}}
```
"""

REGENERATE_PROMPT_TEMPLATE = """### Task
You are a medical newsletter writer tasked with improving the newsletter based on reviewer feedback.

### Original Newsletter
{original_content}

### Reviewer Feedback
{feedback}

### Instructions
1. Address all points in the reviewer feedback
2. Maintain the original structure and format
3. Ensure scientific accuracy and clarity
4. Keep the engaging style for medical professionals

### Previous Attempts and Feedback
{previous_attempts}

### Output
Please provide an improved version of the newsletter incorporating the feedback."""

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


@tool("generate_curation", return_direct=True)
async def generate_curation(rag_results_path: str, max_retries: int = 3, retry_delay: int = 2) -> Dict:
    """
    Generate a curated newsletter from RAG results
    
    Args:
        rag_results_path: Path to the RAG results JSON file
        
    Returns:
        Dict containing the generated curation
    """
    try:
        # RAG 결과 로드
        with open(rag_results_path, 'r', encoding='utf-8') as f:
            rag_results = json.load(f)
        
        # 검색어 추출
        search_term = os.path.basename(rag_results_path).split('_rag_')[1].split('_')[0]
        
        # Q&A 쌍 구성
        qa_pairs = []
        for result in rag_results:
            # 데일리 RAG 결과 처리
            if isinstance(result, dict) and 'rag_results' in result:
                for rag_result in result['rag_results']:
                    qa_pairs.append({
                        'question': rag_result['question'],
                        'answer': rag_result['answer'],
                        'relevance_score': rag_result.get('relevance_score', 0), 
                        'source_docs': rag_result.get('source_docs', [])
                    })
            # 아티클 RAG 결과 처리
            elif isinstance(result, dict) and 'article_title' in result:
                for rag_result in result.get('rag_results', []):
                    qa_pairs.append({
                        'question': rag_result['question'],
                        'answer': rag_result['answer'],
                        'relevance_score': rag_result.get('relevance_score', 0),
                        'source_docs': rag_result.get('source_docs', [])
                    })

        # Long-context reordering: 관련도가 높은 내용을 바깥쪽(처음과 끝)에 배치
        n = len(qa_pairs)
        half_n = n // 2
        
        # 관련도 점수로 정렬
        sorted_pairs = sorted(qa_pairs, key=lambda x: x['relevance_score'], reverse=True)
        
        # 높은 점수의 절반은 앞뒤로 번갈아가며 배치
        reordered_pairs = []
        for i in range(half_n):
            if i % 2 == 0:
                reordered_pairs.insert(0, sorted_pairs[i])  # 앞에 추가
            else:
                reordered_pairs.append(sorted_pairs[i])     # 뒤에 추가
                
        # 나머지 낮은 점수는 중간에 배치
        middle_start = len(reordered_pairs) // 2
        reordered_pairs[middle_start:middle_start] = sorted_pairs[half_n:]
        
        qa_pairs = reordered_pairs
        
        # Q&A 쌍을 문자열로 변환
        qa_text = "\n\n".join([
            f"Q: {qa['question']}\nA: {qa['answer']}"
            for qa in qa_pairs
        ])
        
        async def attempt_verification(content: str) -> Dict:
            """뉴스레터 검증"""
            prompt = ChatPromptTemplate.from_template(VERIFICATION_PROMPT_TEMPLATE)
            messages = prompt.format_messages(content=content)
            verification = await chat.ainvoke(messages)
            
            try:
                content = verification.content
                if '```' in content:
                    start = content.find('```') + 3
                    end = content.find('```', start)
                    if 'json' in content[start:start+10]:
                        start = content.find('\n', start) + 1
                    content = content[start:end].strip()
                
                verification_dict = json.loads(content)
                return {
                    'score': verification_dict.get('score', 0),
                    'feedback': verification_dict.get('feedback', 'No feedback provided')
                }
            except Exception as e:
                print(f"Verification error: {str(e)}")
                return {'score': 0, 'feedback': f"Verification failed: {str(e)}"}

        async def attempt_regenerate(original_content: str, feedback: str, previous_attempts: List[Dict]) -> str:
            """재생성 시도"""
            previous_attempts_text = "\n\n".join([
                f"Attempt {i+1}:\n{attempt['content']}\nFeedback: {attempt['feedback']}"
                for i, attempt in enumerate(previous_attempts)
            ])
            
            prompt = ChatPromptTemplate.from_template(REGENERATE_PROMPT_TEMPLATE)
            messages = prompt.format_messages(
                original_content=original_content,
                feedback=feedback,
                previous_attempts=previous_attempts_text
            )
            response = await chat.ainvoke(messages)
            return response.content

        # 초기 큐레이션 생성
        curation_content = None
        verification_history = []
        
        for attempt in range(max_retries):
            if attempt > 0:
                print(f"Retrying curation generation (attempt {attempt + 1}/{max_retries})")
                await asyncio.sleep(retry_delay * attempt)
            
            if not curation_content:
                # 첫 생성 또는 이전 시도 실패
                prompt = ChatPromptTemplate.from_template(CURATION_PROMPT_TEMPLATE)
                messages = prompt.format_messages(
                    search_term=search_term,
                    qa_pairs=qa_text
                )
                response = await chat.ainvoke(messages)
                curation_content = response.content
            
            # 검증
            verification = await attempt_verification(curation_content)
            verification_history.append({
                'content': curation_content,
                'feedback': verification['feedback'],
                'score': verification['score']
            })
            
            # 점수가 충분히 높으면 종료
            if verification['score'] >= 85:
                break
            
            # 재생성 시도
            if attempt < max_retries - 1:
                curation_content = await attempt_regenerate(
                    curation_content,
                    verification['feedback'],
                    verification_history
                )
        
        # 소스 문서 정보 수집
        source_docs = set()  # 중복 제거를 위한 집합
        for qa in sorted_pairs:
            if 'source_docs' in qa:
                for doc in qa['source_docs']:
                    source_docs.add((
                        doc['generated_summary'],
                        doc['link']
                    ))
        
        # 결과 저장
        curation = {
            "search_term": search_term,
            "content": curation_content,
            "generated_at": datetime.now().isoformat(),
            "source_qa_pairs": qa_pairs,
            "sources": [
                {
                    "summary": summary,
                    "link": link
                }
                for summary, link in source_docs
            ],
            "verification_history": verification_history
        }
        
        # 큐레이션 생성 완료 후 번역 및 윤문 수행
        curation = await translate_and_edit_curation(curation)
        
        # 파일로 저장
        output_dir = "./data/curations"
        os.makedirs(output_dir, exist_ok=True)
        
        output_path = os.path.join(
            output_dir,
            f"curation_{search_term}_{datetime.now().strftime('%Y%m%d')}.json"
        )
        
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(curation, f, ensure_ascii=False, indent=4)
            
        return curation
        
    except Exception as e:
        print(f"Error generating curation: {str(e)}")
        return None

async def process_keyword(keyword: str, files: Dict[str, str], rag_dir: str, today: str):
    """단일 키워드에 대한 큐레이션 생성"""
    try:
        print(f"\nProcessing {keyword}...")
        
        # 두 파일의 RAG 결과 결합
        combined_results = []
        
        # 아티클 질문 결과 로드
        article_path = os.path.join(rag_dir, files['article'])
        with open(article_path, 'r', encoding='utf-8') as f:
            article_results = json.load(f)
            combined_results.extend(article_results)
        
        # 데일리 질문 결과 로드
        daily_path = os.path.join(rag_dir, files['daily'])
        with open(daily_path, 'r', encoding='utf-8') as f:
            daily_results = json.load(f)
            combined_results.extend(daily_results)
        
        # 결합된 결과로 큐레이션 생성
        output_path = os.path.join(rag_dir, f"combined_rag_{keyword}_{today}.json")
        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(combined_results, f, ensure_ascii=False, indent=4)
        
        # 큐레이션 생성
        tool = generate_curation
        return await tool.ainvoke({"rag_results_path": output_path})
        
    except Exception as e:
        print(f"Error processing {keyword}: {str(e)}")
        return None

async def translate_and_edit_curation(curation: Dict, max_retries: int = 3, retry_delay: int = 2) -> Dict:
    """큐레이션을 한국어로 번역하고 윤문"""
    try:
        prompt = ChatPromptTemplate.from_template(TRANSLATION_PROMPT_TEMPLATE)
        messages = prompt.format_messages(content=curation['content'])
        
        for attempt in range(max_retries):
            if attempt > 0:
                print(f"Retrying translation (attempt {attempt + 1}/{max_retries})")
                await asyncio.sleep(retry_delay * attempt)
            
            response = await chat.ainvoke(messages)
            translated_content = response.content
            
            # 번역본 추가
            curation['korean_content'] = translated_content
            
            # 파일 업데이트
            output_dir = "./data/curations"
            output_path = os.path.join(
                output_dir,
                f"curation_{curation['search_term']}_{datetime.now().strftime('%Y%m%d')}.json"
            )
            
            with open(output_path, 'w', encoding='utf-8') as f:
                json.dump(curation, f, ensure_ascii=False, indent=4)
            
            return curation
            
    except Exception as e:
        print(f"Error translating curation: {str(e)}")
        return curation

async def main():
    """키워드별로 article과 daily RAG 결과를 결합하여 큐레이션 생성"""
    rag_dir = "./data/rag_results"
    today = datetime.now().strftime('%Y%m%d')
    
    # 키워드별 파일 매핑
    keyword_files = {}
    
    # RAG 결과 파일들을 키워드별로 그룹화
    for filename in os.listdir(rag_dir):
        if not filename.endswith(f'{today}.json'):
            continue
            
        if filename.startswith('article_top_q_rag_'):
            keyword = filename.split('_rag_')[1].split('_')[0]
            if keyword not in keyword_files:
                keyword_files[keyword] = {'article': None, 'daily': None}
            keyword_files[keyword]['article'] = filename
            
        elif filename.startswith('daily_rag_'):
            keyword = filename.split('_rag_')[1].split('_')[0]
            if keyword not in keyword_files:
                keyword_files[keyword] = {'article': None, 'daily': None}
            keyword_files[keyword]['daily'] = filename
    
    # 모든 키워드에 대한 태스크 생성
    tasks = []
    for keyword, files in keyword_files.items():
        if not files['article'] or not files['daily']:
            print(f"Skipping {keyword} - missing article or daily RAG results")
            continue
        
        tasks.append(process_keyword(keyword, files, rag_dir, today))
    
    # 모든 태스크 병렬 실행
    if tasks:
        results = await asyncio.gather(*tasks)
        print(f"\nCompleted curation generation for {len(results)} keywords")
    else:
        print("No valid keyword pairs found")

if __name__ == "__main__":
    import asyncio
    asyncio.run(main()) 