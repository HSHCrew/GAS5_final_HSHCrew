import os
import json
import asyncio
from datetime import datetime
from generate_key_questions import generate_key_questions

async def generate_daily_questions():
    """각 검색어별 당일 수집 자료들의 요약을 하나의 문서로 취합하여 핵심 질문 생성"""
    
    # medi_press_terms 디렉토리 경로
    dir_path = "./data/medi_press_terms"
    output_path = "./data/daily_questions"
    
    # 출력 디렉토리가 없으면 생성
    os.makedirs(output_path, exist_ok=True)
    
    # 오늘 날짜
    today = datetime.now().strftime('%Y%m%d')
    
    # 검색어별 요약 취합 및 질문 생성
    for filename in os.listdir(dir_path):
        if filename.endswith('.json'):
            try:
                # 검색어 추출
                search_term = filename.split('_')[3]  # medi_press_full_{search_term}_...
                print(f"\nProcessing summaries for {search_term}...")
                
                # JSON 파일 로드
                file_path = os.path.join(dir_path, filename)
                with open(file_path, 'r', encoding='utf-8') as f:
                    articles = json.load(f)
                
                # 요약 취합하여 하나의 문서로 만들기
                combined_content = []
                for article in articles:
                    if article.get('generated_summary'):
                        combined_content.append(
                            f"Title: {article['title']}\n"
                            f"Summary: {article['generated_summary']}\n"
                        )
                
                if not combined_content:
                    print(f"No summaries found for {search_term}")
                    continue
                
                # 하나의 문서로 결합
                combined_article = [{
                    'title': f"Daily Summaries for {search_term}",
                    'content': "\n\n".join(combined_content)
                }]
                
                # 핵심 질문 생성
                tool = generate_key_questions
                result = await tool.ainvoke({"articles": combined_article})
                
                if result and result[0].get('key_questions'):
                    output_data = {
                        'search_term': search_term,
                        'day_key_questions': result[0]['key_questions'],
                        'created_at': datetime.now().isoformat()
                    }
                    
                    output_filename = f"daily_questions_{search_term}_{today}.json"
                    output_file = os.path.join(output_path, output_filename)
                    
                    with open(output_file, 'w', encoding='utf-8') as f:
                        json.dump(output_data, f, ensure_ascii=False, indent=4)
                        
                    print(f"Generated daily questions for {search_term}")
                else:
                    print(f"Failed to generate questions for {search_term}")
                
                # await asyncio.sleep(2)
                
            except Exception as e:
                print(f"Error processing {filename}: {str(e)}")
                continue

if __name__ == "__main__":
    asyncio.run(generate_daily_questions()) 