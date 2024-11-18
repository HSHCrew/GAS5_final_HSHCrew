import aiohttp
from bs4 import BeautifulSoup
import pandas as pd
from typing import List, Dict, Optional
from datetime import datetime
from langchain.tools import tool
import json
import asyncio
import random
from typing import Tuple


# User Agents를 브라우저 타입별로 구분
BROWSER_CONFIGS = {
    'chromium': [
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36',
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36',
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36',
        'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36',
        'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36'
    ],
    'firefox': [
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/119.0',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/118.0'
    ],
    'webkit': [
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15',
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Safari/605.1.15',
        'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1',
        'Mozilla/5.0 (iPhone; CPU iPhone OS 16_6_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1'
    ]
}

def get_random_browser_config() -> Tuple[str, str]:
    """
    Returns a random browser type and matching user agent
    
    Returns:
        Tuple[str, str]: (browser_type, user_agent)
    """
    browser_type = random.choice(list(BROWSER_CONFIGS.keys()))
    user_agent = random.choice(BROWSER_CONFIGS[browser_type])
    return browser_type, user_agent


def calculate_relevance_score(article_data: Dict, search_term: str) -> float:
    """
    Calculates relevance score of an article based on search term.
    
    Args:
        article_data (dict): Article metadata including title, summary, topic
        search_term (str): Original search term
        
    Returns:
        float: Relevance score between 0 and 1
    """
    score = 0.0
    search_terms = set(search_term.lower().split())
    
    # 제목에서의 검색어 출현 빈도 (가중치: 0.4)
    title_words = set(article_data['title'].lower().split())
    title_matches = len(search_terms.intersection(title_words))
    score += 0.4 * (title_matches / len(search_terms))
    
    # 요약에서의 검색어 출현 빈도 (가중치: 0.3)
    summary_words = set(article_data['summary'].lower().split())
    summary_matches = len(search_terms.intersection(summary_words))
    score += 0.3 * (summary_matches / len(search_terms))
    
    # 토픽 관련성 (가중치: 0.2)
    topic_words = set(article_data['topic'].lower().split())
    topic_matches = len(search_terms.intersection(topic_words))
    score += 0.2 * (topic_matches / len(search_terms))
    
    # 최신성 (가중치: 0.1)
    try:
        date = datetime.strptime(article_data['date'], '%B %d, %Y')
        days_old = (datetime.now() - date).days
        recency_score = max(0, 1 - (days_old / 365))  # 1년 이내 기사
        score += 0.1 * recency_score
    except:
        pass
    
    return min(1.0, score)

def convert_relative_date(relative_date: str) -> str:
    """
    Converts relative date to absolute date.
    
    Args:
        relative_date (str): Relative date string (e.g., "18 hours ago", "2 days ago")
        
    Returns:
        str: Absolute date in format "Month DD, YYYY"
    """
    now = datetime.now()
    
    try:
        if 'minutes ago' in relative_date or 'minute ago' in relative_date:
            minutes = int(relative_date.split()[0])
            date = now - pd.Timedelta(minutes=minutes)
        elif 'hours ago' in relative_date or 'hour ago' in relative_date:
            hours = int(relative_date.split()[0])
            date = now - pd.Timedelta(hours=hours)
        elif 'days ago' in relative_date or 'day ago' in relative_date:
            days = int(relative_date.split()[0])
            date = now - pd.Timedelta(days=days)
        elif 'weeks ago' in relative_date or 'week ago' in relative_date:
            weeks = int(relative_date.split()[0])
            date = now - pd.Timedelta(weeks=weeks)
        else:
            # 이미 절대 날짜 형식인 경우 그대로 반환
            return relative_date
        
        return date.strftime('%B %d, %Y')
    except:
        return relative_date

async def scrape_medicalxpress_search_raw(search_term: str) -> Optional[pd.DataFrame]:
    """
    Raw scraping function for MedicalXpress search results.
    """
    # 검색 URL 생성 (s=0 : relevancy, s=1 : date)
    url = f"https://medicalxpress.com/search/page1.html?search={search_term}&s=1"
    
    # HTTP 요청 헤더 설정
    headers = {
        'User-Agent': get_random_browser_config()[1]
    }
    
    try:
        async with aiohttp.ClientSession() as session:
            async with session.get(url, headers=headers) as response:
                if response.status != 200:
                    raise RuntimeError(f"HTTP Error: {response.status}")
                
                html = await response.text()
                soup = BeautifulSoup(html, 'html.parser')
                
                # 검색 결과 기사들 찾기
                articles = soup.find_all('article', class_='sorted-article')
                
                search_results = []
                for article in articles:
                    # 기사 제목과 링크
                    title_elem = article.find('h2').find('a')
                    title = title_elem.text.strip()
                    link = title_elem['href']
                    
                    # 기사 요약
                    summary = article.find('p', class_='mb-4').text.strip()
                    
                    # 카테고리/토픽
                    topic = article.find('div', class_='sorted-article-topic').text.strip()
                    
                    # 날짜 정보 추출 및 변환
                    relative_date = article.find('p', class_='text-uppercase text-low').text.strip()
                    absolute_date = convert_relative_date(relative_date)
                    
                    # 댓글 수와 공유 수
                    info_items = article.find_all('span', class_='article__info-item')
                    comments = info_items[1].find('span').text if len(info_items) > 1 else '0'
                    shares = info_items[2].find('span').text if len(info_items) > 2 else '0'
                    
                    # 이미지 URL (있는 경우)
                    img_elem = article.find('img')
                    img_url = img_elem['src'] if img_elem else None
                    
                    search_results.append({
                        'title': title,
                        'link': link, 
                        'summary': summary,
                        'topic': topic,
                        'date': absolute_date,  # 변환된 절대 날짜 사용
                        'comments': comments,
                        'shares': shares,
                        'image_url': img_url
                    })
                
                # 결과를 DataFrame으로 변환
                df = pd.DataFrame(search_results)
                return df
    
    except Exception as e:
        print(f"Error fetching data: {e}")
        return None

@tool("scrape_medicalxpress_search", return_direct=True)
async def scrape_medicalxpress_search(search_term: str, min_relevance: float = 0.3) -> List[Dict]:
    """
    Scrapes articles from MedicalXpress.com based on search term and filters by relevance.
    
    Args:
        search_term (str): The search term to query (e.g., "diabetes")
        min_relevance (float): Minimum relevance score (0-1) for including articles (default: 0.3)
        
    Returns:
        List[Dict]: List of relevant articles' urls with metadata and relevance scores
    """
    df = await scrape_medicalxpress_search_raw(search_term)
    if df is None or df.empty:
        return []
    
    # 각 기사의 관련도 점수 계산
    results = []
    for _, row in df.iterrows():
        article_data = row.to_dict()
        relevance = calculate_relevance_score(article_data, search_term)
        
        if relevance >= min_relevance:
            article_data['relevance_score'] = round(relevance, 3)
            results.append(article_data)
    
    # 관련도 점수로 정렬
    results.sort(key=lambda x: x['relevance_score'], reverse=True)
    
    return results

# 사용 예시
if __name__ == "__main__":
    async def main():
        try:
            # Tool 실행
            tool = scrape_medicalxpress_search
            results = await tool.ainvoke({"search_term": "diabetes", "min_relevance": 0.3})
            
            if results:
                # JSON 파일로 저장
                filename = f"medicalxpress_search_results_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
                with open(filename, 'w', encoding='utf-8') as f:
                    json.dump(results, ensure_ascii=False, indent=2, fp=f)
                print(f"Search results saved to {filename}")
            else:
                print("No relevant articles found")
                
        except Exception as e:
            print(f"Error: {str(e)}")
    
    # 비동기 실행
    asyncio.run(main())