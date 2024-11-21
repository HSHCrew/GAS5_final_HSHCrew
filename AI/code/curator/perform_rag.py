from langchain_openai import ChatOpenAI
from langchain.prompts import ChatPromptTemplate
from langchain_community.vectorstores import Chroma
from langchain_openai import OpenAIEmbeddings
from langchain.tools import tool
from typing import List, Dict
import json
import os
from dotenv import load_dotenv
import asyncio
from datetime import datetime
from langchain_teddynote import logging

load_dotenv()
logging.langsmith(project_name="gas5fp_curator_rag")

# ChromaDB 및 OpenAI 초기화
embeddings = OpenAIEmbeddings(
    model="text-embedding-3-large",
    openai_api_key=os.getenv('OPENAI_API_KEY')
)
vectorstore = Chroma(
    persist_directory="./data/chromadb",
    collection_name="articles_20241121_token600",
    embedding_function=embeddings
)

# ChatGPT 모델 초기화
chat = ChatOpenAI(
    model="gpt-4o",
    temperature=0.2,
    openai_api_key=os.getenv('OPENAI_API_KEY')
)

RAG_PROMPT_TEMPLATE = """### task
You are a medical news analyst responsible for creating easily understandable and reader-friendly curated content. 
Based on the provided context, please answer the following question in a comprehensive and accurate manner.

### question
Question: {question}

### context
{context}

### answer  
Please provide a detailed answer that:
1. Directly addresses the question
2. Uses specific information from the context
3. Maintains scientific accuracy
4. Acknowledges any limitations in the available information

Answer:"""

async def perform_rag_for_question(question: str, k: int = 3) -> Dict:
    """단일 질문에 대한 RAG 수행"""
    try:
        # 관련 문서 검색
        docs = vectorstore.similarity_search(question, k=k)
        context = "\n\n".join([doc.page_content for doc in docs])
        
        # 프롬프트 생성
        prompt = ChatPromptTemplate.from_template(RAG_PROMPT_TEMPLATE)
        messages = prompt.format_messages(
            question=question,
            context=context
        )
        
        # ChatGPT로 답변 생성
        response = await chat.ainvoke(messages)
        
        return {
            "question": question,
            "answer": response.content,
            "source_docs": [doc.metadata for doc in docs]
        }
        
    except Exception as e:
        print(f"Error performing RAG for question: {question}")
        print(f"Error details: {str(e)}")
        return None

async def calculate_similarities(questions: List[str], search_term: str) -> List[float]:
    """질문들과 검색어 간의 유사도를 비동기로 계산"""
    try:
        # 임베딩 생성을 비동기로 처리
        question_embeddings_task = asyncio.create_task(
            embeddings.aembed_documents([q for q in questions])
        )
        search_term_embedding_task = asyncio.create_task(
            embeddings.aembed_query(search_term)
        )
        
        # 두 임베딩 작업이 모두 완료될 때까지 대기
        question_embeddings, search_term_embedding = await asyncio.gather(
            question_embeddings_task,
            search_term_embedding_task
        )
        
        # 코사인 유사도 계산 (넘파이 연산은 충분히 빠르므로 동기로 처리)
        from numpy import dot
        from numpy.linalg import norm
        
        similarities = [
            dot(search_term_embedding, q_emb) / (norm(search_term_embedding) * norm(q_emb))
            for q_emb in question_embeddings
        ]
        
        return similarities
        
    except Exception as e:
        print(f"Error calculating similarities: {str(e)}")
        return [0.0] * len(questions)  # 에러 시 모든 유사도를 0으로 반환

async def process_article_questions(article_path: str, 
                                 min_relevance: float = 0.15,  # 최소 관련도 점수
                                 max_questions: int = 15,      # 최대 질문 수
                                 min_questions: int = 5        # 최소 질문 수
                                 ) -> List[Dict]:
    """단일 아티클의 key_questions에 대한 RAG 수행"""
    try:
        with open(article_path, 'r', encoding='utf-8') as f:
            articles = json.load(f)
        
        # 검색어 추출
        search_term = os.path.basename(article_path).split('_full_')[1].split('_')[0]
        
        # 질문과 메타데이터 수집
        all_questions = []
        questions_only = []
        for article in articles:
            if 'key_questions' not in article:
                continue
            for question in article['key_questions']:
                questions_only.append(question)
                all_questions.append({
                    'question': question,
                    'article_title': article['title'],
                    'article_link': article['link']
                })
        
        if not all_questions:
            return []
        
        # 유사도 계산을 비동기로 수행
        similarities = await calculate_similarities(questions_only, search_term)
        
        # 질문에 유사도 점수 추가
        for q, sim in zip(all_questions, similarities):
            q['relevance_score'] = float(sim)
        
        # 관련도 점수로 정렬
        sorted_questions = sorted(
            all_questions,
            key=lambda x: x['relevance_score'],
            reverse=True
        )
        
        # 임계값을 넘는 질문 선택
        filtered_questions = [q for q in sorted_questions if q['relevance_score'] >= min_relevance]
        
        # 질문 수 조정
        if len(filtered_questions) < min_questions:
            # 임계값을 넘지 않더라도 최소 질문 수는 확보
            selected_questions = sorted_questions[:min_questions]
        else:
            # 임계값을 넘는 질문 중 최대 개수까지만 선택
            selected_questions = filtered_questions[:max_questions]
        
        print(f"Selected {len(selected_questions)} questions for {search_term}")
        
        # 선택된 질문들에 대해 RAG 수행
        tasks = [
            perform_rag_for_question(q['question'])
            for q in selected_questions
        ]
        
        # 병렬 실행
        rag_results = await asyncio.gather(*tasks)
        
        # 결과를 아티클별로 그룹화
        article_results = {}
        for q, result in zip(selected_questions, rag_results):
            if result:
                title = q['article_title']
                if title not in article_results:
                    article_results[title] = {
                        "article_title": title,
                        "article_link": q['article_link'],
                        "rag_results": []
                    }
                result['relevance_score'] = q['relevance_score']
                article_results[title]["rag_results"].append(result)
        
        return list(article_results.values())
        
    except Exception as e:
        print(f"Error processing article file: {article_path}")
        print(f"Error details: {str(e)}")
        return []

async def process_daily_questions(daily_questions_path: str) -> List[Dict]:
    """daily_questions 폴더의 질문들에 대한 RAG 수행"""
    try:
        all_results = []
        tasks = []
        
        for filename in os.listdir(daily_questions_path):
            if not filename.endswith('.json'):
                continue
                
            file_path = os.path.join(daily_questions_path, filename)
            
            with open(file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
                
            search_term = data['search_term']
            questions = data['day_key_questions']
            
            # 유사도 계산을 비동기로 수행
            similarities = await calculate_similarities(questions, search_term)
            
            # 각 질문에 대한 RAG 태스크 생성
            term_tasks = []
            for question, similarity in zip(questions, similarities):
                rag_task = perform_rag_for_question(question)
                term_tasks.append((search_term, rag_task, similarity))
            tasks.extend(term_tasks)
        
        # 모든 RAG 태스크 병렬 실행
        if tasks:
            results = await asyncio.gather(*[task for _, task, _ in tasks])
            
            # 결과를 search_term별로 그룹화하고 유사도 점수 추가
            term_results = {}
            for (search_term, _, similarity), result in zip(tasks, results):
                if result:  # None이 아닌 결과만 처리
                    if search_term not in term_results:
                        term_results[search_term] = []
                    result['relevance_score'] = float(similarity)
                    term_results[search_term].append(result)
            
            # 최종 결과 형식으로 변환
            all_results = [
                {
                    "search_term": term,
                    "rag_results": results,
                    "created_at": datetime.now().isoformat()
                }
                for term, results in term_results.items()
                if results  # 결과가 있는 경우만 포함
            ]
        
        return all_results
        
    except Exception as e:
        print(f"Error processing daily questions")
        print(f"Error details: {str(e)}")
        return []

async def main():
    # 결과 저장 디렉토리
    output_dir = "./data/rag_results"
    os.makedirs(output_dir, exist_ok=True)
    
    # # 1. 아티클 key_questions 처리
    # articles_dir = "./data/medi_press_terms"
    # for filename in os.listdir(articles_dir):
    #     if not filename.endswith('.json'):
    #         continue
            
    #     file_path = os.path.join(articles_dir, filename)
    #     search_term = filename.split('_full_')[1].split('_')[0]  # 검색어 추출
    #     results = await process_article_questions(file_path)
        
    #     if results:
    #         # term별 파일명 생성
    #         output_path = os.path.join(
    #             output_dir, 
    #             f"article_top_q_rag_{search_term}_{datetime.now().strftime('%Y%m%d')}.json"
    #         )
    #         with open(output_path, 'w', encoding='utf-8') as f:
    #             json.dump(results, f, ensure_ascii=False, indent=4)
    #         print(f"Saved article RAG results for {search_term}")
    
    # 2. daily_questions 처리
    daily_questions_dir = "./data/daily_questions"
    results = await process_daily_questions(daily_questions_dir)
    
    if results:
        # term별로 결과 분리하여 저장
        for term_result in results:
            search_term = term_result["search_term"]
            output_path = os.path.join(
                output_dir,
                f"daily_rag_{search_term}_{datetime.now().strftime('%Y%m%d')}.json"
            )
            with open(output_path, 'w', encoding='utf-8') as f:
                json.dump([term_result], f, ensure_ascii=False, indent=4)
            print(f"Saved daily RAG results for {search_term}")

if __name__ == "__main__":
    asyncio.run(main()) 