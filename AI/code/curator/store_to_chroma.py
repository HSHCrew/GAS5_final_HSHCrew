import chromadb
from chromadb.utils import embedding_functions
from typing import List, Dict, Optional
import json
import os
from datetime import datetime
from dotenv import load_dotenv
from langchain.text_splitter import RecursiveCharacterTextSplitter
import tiktoken

def get_token_splitter(chunk_size: int = 600) -> RecursiveCharacterTextSplitter:
    """토큰 기반 텍스트 스플리터 생성"""
    return RecursiveCharacterTextSplitter.from_tiktoken_encoder(
        model_name="text-embedding-3-large",
        chunk_size=chunk_size,
        chunk_overlap=50,
        separators=["\n\n", "\n", ". ", ", ", " ", ""]
    )

def store_articles_to_chroma(articles: List[Dict], collection_name: Optional[str] = None):
    """기사들을 ChromaDB에 저장"""
    load_dotenv()
    # ChromaDB 클라이언트 초기화
    client = chromadb.PersistentClient(path="./data/chromadb")
    
    # OpenAI 임베딩 함수 설정
    embedding_function = embedding_functions.OpenAIEmbeddingFunction(
        api_key=os.getenv("OPENAI_API_KEY"),
        model_name="text-embedding-3-large"
    )
    
    # 컬렉션 이름 설정
    if not collection_name:
        collection_name = f"articles_{datetime.now().strftime('%Y%m%d')}_token600"
    
    # 컬렉션 생성 또는 가져오기
    try:
        collection = client.get_collection(
            name=collection_name,
            embedding_function=embedding_function
        )
    except:
        collection = client.create_collection(
            name=collection_name,
            embedding_function=embedding_function
        )
    
    # 텍스트 스플리터 초기화
    text_splitter = get_token_splitter()
    
    for article in articles:
        try:
            metadata = {
                "search_term": article.get("search_term", "") or "",
                "link": article.get("link", "") or "",
                "generated_summary": article.get("generated_summary", "") or "",
                "topic": article.get("topic", '') or '',
                "key_questions": str(article.get("key_questions", [])) if isinstance(article.get("key_questions", []), list) else article.get("key_questions", '') or '',
                "date": article.get("date", "") or '',
                "relevance_score": float(article.get("relevance_score", 0)),
                "editors_notes": str(article.get("editors_notes", {})) if isinstance(article.get("editors_notes", {}), dict) else article.get("editors_notes", '') or ''
            }
            
            # 1. 요약문 처리
            summary = article.get("generated_summary")
            if summary:
                summary_chunks = text_splitter.split_text(summary)
                summary_ids = [f"{article['link']}_summary_{i}" for i in range(len(summary_chunks))]
                summary_metadatas = [metadata for _ in summary_chunks]
                
                collection.add(
                    documents=summary_chunks,
                    ids=summary_ids,
                    metadatas=summary_metadatas
                )
            
            # full_text = f"{article['title']}\n\n{article['content']}"
            # content_chunks = text_splitter.split_text(full_text)
            # content_ids = [f"{article['link']}_{i}" for i in range(len(content_chunks))]
            # metadatas = [metadata for _ in content_chunks]
            
            # collection.add(
            #     documents=content_chunks,
            #     ids=content_ids,
            #     metadatas=metadatas
            # )
            
            # print(f"Successfully stored article: {article['title']} ({len(content_chunks)} chunks)")
            
        except Exception as e:
            print(f"Error storing article {article.get('title', 'Unknown')}: {str(e)}")
            continue
    
    return collection_name

if __name__ == "__main__":
    # medi_press_terms 디렉토리의 모든 JSON 파일 처리
    dir_path = "./data/medi_press_terms"
    
    for filename in os.listdir(dir_path):
        if filename.endswith('.json'):
            file_path = os.path.join(dir_path, filename)
            
            try:
                print(f"\nProcessing {filename}...")
                
                # JSON 파일 로드
                with open(file_path, 'r', encoding='utf-8') as f:
                    articles = json.load(f)
                
                if not articles:
                    print(f"Skipping {filename} - no articles found")
                    continue
                
                # ChromaDB에 저장
                collection_name = store_articles_to_chroma(articles)
                print(f"Articles stored in collection: {collection_name}")
                
            except Exception as e:
                print(f"Error processing {filename}: {str(e)}")
                continue 