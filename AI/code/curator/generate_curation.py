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

@tool("generate_curation", return_direct=True)
async def generate_curation(rag_results_path: str) -> Dict:
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
                        'relevance_score': rag_result.get('relevance_score', 0)
                    })
            # 아티클 RAG 결과 처리
            elif isinstance(result, dict) and 'article_title' in result:
                for rag_result in result.get('rag_results', []):
                    qa_pairs.append({
                        'question': rag_result['question'],
                        'answer': rag_result['answer'],
                        'relevance_score': rag_result.get('relevance_score', 0)
                    })
        
        # 관련도 점수로 정렬
        qa_pairs = sorted(qa_pairs, key=lambda x: x['relevance_score'], reverse=True)
        
        # Q&A 쌍을 문자열로 변환
        qa_text = "\n\n".join([
            f"Q: {qa['question']}\nA: {qa['answer']}"
            for qa in qa_pairs
        ])
        
        # 프롬프트 생성
        prompt = ChatPromptTemplate.from_template(CURATION_PROMPT_TEMPLATE)
        messages = prompt.format_messages(
            search_term=search_term,
            qa_pairs=qa_text
        )
        
        # 큐레이션 생성
        response = await chat.ainvoke(messages)
        
        # 결과 저장
        curation = {
            "search_term": search_term,
            "content": response.content,
            "generated_at": datetime.now().isoformat(),
            "source_qa_pairs": qa_pairs
        }
        
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