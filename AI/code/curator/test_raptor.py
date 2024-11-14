import os
from dotenv import load_dotenv
import json
from raptor import RaptorTree

def test_document_processing():
    """문서 처리 테스트"""
    load_dotenv()
    
    raptor = RaptorTree(openai_api_key=os.getenv("OPENAI_API_KEY"))

    # 테스트용 문서 로드
    with open("./data/test_data/test_documents.json", "r", encoding='utf-8') as f:
        test_documents = json.load(f)

    print("\n=== 문서 처리 테스트 시작 ===\n")

    # 각 문서별 처리 테스트
    for i, doc in enumerate(test_documents, 1):
        print(f"\n테스트 케이스 {i}/{len(test_documents)}")
        print("="*50)
        print(f"문서 제목: {doc['metadata']['title']}")
        print("-"*50)
        
        try:
            # 문서 요약 및 질문 생성 테스트
            doc_analysis = raptor._generate_document_summary(doc)
            
            print("\n[요약]")
            print(doc_analysis["summary"])
            
            print("\n[생성된 질문들]")
            for q in doc_analysis["questions"]:
                print(f"\n카테고리: {q['category']}")
                print(f"질문: {q['question']}")
                print(f"검색 키워드: {', '.join(q['search_keywords'])}")
            
            print("\n[분류된 주제]")
            print(doc_analysis["metadata"]["topic"])
            
        except Exception as e:
            print(f"처리 중 오류 발생: {e}")
            print(f"상세 오류 정보: {str(e)}")

def test_search():
    load_dotenv()
    
    raptor = RaptorTree(openai_api_key=os.getenv("OPENAI_API_KEY"))

    # 테스트용 문서로 트리 구축
    with open("./data/test_data/test_documents.json", "r", encoding='utf-8') as f:
        test_documents = json.load(f)
    
    raptor.build_from_documents(test_documents)

    print("\n=== 검색 테스트 시작 ===\n")

    # 다양한 검색어로 테스트
    test_queries = [
        "비만치료제",
        "알츠하이머",
        "의료정책",
        "AI 의료",
        "전공의"
    ]

    for query in test_queries:
        print(f"\n검색어: {query}")
        print("-"*50)
        
        results = raptor.search(query, max_results=3)
        
        if not results:
            print(f"'{query}'에 대한 검색 결과가 없습니다.")
            continue
            
        for i, result in enumerate(results, 1):
            print(f"\n결과 {i}:")
            print(f"제목: {result.get('title', 'Unknown Title')}")
            print(f"검색 유형: {result.get('search_type', 'Unknown')}")
            if result.get('summary'):
                print(f"요약: {result['summary'][:200]}...")
            print(f"관련도 점수: {result.get('final_score', 0):.3f}")

if __name__ == "__main__":
    print("=== RAPTOR 테스트 시작 ===")
    
    try:
        print("\n1. 문서 처리 테스트")
        test_document_processing()
        
        print("\n2. 검색 기능 테스트")
        test_search()
        
    except Exception as e:
        print(f"\n테스트 중 오류 발생: {e}")
    
    print("\n=== 테스트 완료 ===")
