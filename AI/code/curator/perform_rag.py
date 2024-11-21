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

async def process_article_questions(article_path: str) -> List[Dict]:
    """단일 아티클의 key_questions에 대한 RAG 수행"""
    try:
        with open(article_path, 'r', encoding='utf-8') as f:
            articles = json.load(f)
            
        all_results = []
        tasks = []
        
        # 모든 아티클의 모든 질문에 대한 태스크 생성
        for article in articles:
            if 'key_questions' not in article:
                continue
                
            # 각 질문에 대한 RAG 태스크 생성
            article_tasks = [
                (article['title'], article['link'], perform_rag_for_question(question))
                for question in article['key_questions']
            ]
            tasks.extend(article_tasks)
        
        # 모든 RAG 태스크 병렬 실행
        if tasks:
            results = await asyncio.gather(*[task for _, _, task in tasks])
            
            # 결과를 아티클별로 그룹화
            article_results = {}
            for (title, link, _), result in zip(tasks, results):
                if result:  # None이 아닌 결과만 처리
                    if title not in article_results:
                        article_results[title] = {
                            "article_title": title,
                            "article_link": link,
                            "rag_results": []
                        }
                    article_results[title]["rag_results"].append(result)
            
            # 최종 결과 형식으로 변환
            all_results = [
                article_data
                for article_data in article_results.values()
                if article_data["rag_results"]  # 결과가 있는 경우만 포함
            ]
                
        return all_results
    
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
            search_term = filename.split('_questions_')[1].split('.json')[0]
            
            with open(file_path, 'r', encoding='utf-8') as f:
                questions = json.load(f)
            
            # 각 질문에 대한 RAG 태스크 생성
            term_tasks = [perform_rag_for_question(question) for question in questions]
            tasks.extend([(search_term, task) for task in term_tasks])
        
        # 모든 RAG 태스크 병렬 실행
        if tasks:
            results = await asyncio.gather(*[task for _, task in tasks])
            
            # 결과를 search_term별로 그룹화
            term_results = {}
            for (search_term, _), result in zip(tasks, results):
                if result:  # None이 아닌 결과만 처리
                    if search_term not in term_results:
                        term_results[search_term] = []
                    term_results[search_term].append(result)
            
            # 최종 결과 형식으로 변환
            all_results = [
                {"search_term": term, "rag_results": results}
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
    
    # 1. 아티클 key_questions 처리
    articles_dir = "./data/medi_press_terms"
    for filename in os.listdir(articles_dir):
        if not filename.endswith('.json'):
            continue
            
        file_path = os.path.join(articles_dir, filename)
        search_term = filename.split('_full_')[1].split('_')[0]  # 검색어 추출
        results = await process_article_questions(file_path)
        
        if results:
            # term별 파일명 생성
            output_path = os.path.join(
                output_dir, 
                f"article_rag_{search_term}_{datetime.now().strftime('%Y%m%d')}.json"
            )
            with open(output_path, 'w', encoding='utf-8') as f:
                json.dump(results, f, ensure_ascii=False, indent=4)
            print(f"Saved article RAG results for {search_term}")
    
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