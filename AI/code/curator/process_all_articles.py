import os
import json
import asyncio
from generate_key_questions import generate_key_questions
from summarize_article import process_articles

async def process_all_files():
    """medi_press_terms 디렉토리의 모든 JSON 파일에 대해 key questions 생성"""
    
    # medi_press_terms 디렉토리 경로
    dir_path = "./data/medi_press_terms"
    
    # 디렉토리 내 모든 JSON 파일 처리
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
                
                # key questions 생성
                tool = generate_key_questions
                result_articles = await tool.ainvoke({"articles": articles})
                
                # 기사 요약
                tool = process_articles
                result_articles = await tool.ainvoke({"articles": articles})
                
                # 결과 저장
                if result_articles:
                    with open(file_path, 'w', encoding='utf-8') as f:
                        json.dump(result_articles, f, ensure_ascii=False, indent=4)
                    print(f"Successfully processed {len(result_articles)} articles in {filename}")
                else:
                    print(f"Failed to generate questions for {filename}")
                    
                # # 파일 간 대기
                # await asyncio.sleep(2)
                
            except Exception as e:
                print(f"Error processing {filename}: {str(e)}")
                continue

if __name__ == "__main__":
    asyncio.run(process_all_files()) 