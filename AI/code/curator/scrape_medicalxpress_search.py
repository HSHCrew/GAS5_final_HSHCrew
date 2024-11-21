import pandas as pd
from typing import List, Dict, Optional
from datetime import datetime
from langchain.tools import tool
import json
import asyncio
import random
from typing import Tuple
from scrape_medicalxpress_content import scrape_medicalxpress_article
from playwright.async_api import async_playwright, Playwright


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

async def handle_press_and_hold(page):
    """
    Handles the 'Press and Hold' button if it appears
    """
    try:
        # 버튼이 나타날 때까지 대기 (최대 1초)
        button = await page.wait_for_selector('#holdButton', timeout=1000)
        if button:
            print("Found Press and Hold button, handling...")
            box = await button.bounding_box()
            if box:
                # 마우스 이동 및 클릭 유지
                await page.mouse.move(box['x'] + box['width']/2, box['y'] + box['height']/2)
                await page.mouse.down()
                # 6초 대기 (안전하게 5초보다 조금 더 길게)
                await asyncio.sleep(6)
                await page.mouse.up()
                print("Press and Hold completed")
                await page.wait_for_selector('#holdButton', state='hidden', timeout=5000)
    except Exception as e:
        if 'TimeoutError' in str(e):
            print("No Press and Hold button found, continuing...")
        else:
            print(f"Warning: Press and Hold handling failed: {str(e)}")

async def scrape_medicalxpress_search_raw(search_term: str, max_retries: int = 3) -> Optional[pd.DataFrame]:
    """
    Raw scraping function for MedicalXpress search results using Playwright.
    """
    for attempt in range(max_retries):
        try:
            if attempt > 0:
                wait_time = (2 ** attempt) + random.uniform(2, 5)
                print(f"Retry attempt {attempt + 1}/{max_retries}, waiting {wait_time:.1f} seconds...")
                await asyncio.sleep(wait_time)
            
            browser_type, user_agent = get_random_browser_config()
            
            playwright = await async_playwright().start()
            try:
                browser = await getattr(playwright, browser_type).launch(headless=True)
                context = await browser.new_context(
                    user_agent=user_agent,
                    viewport={'width': 1920, 'height': 1080}
                )
                
                # 추가 브라우저 설정
                await context.add_init_script("""
                    Object.defineProperty(navigator, 'webdriver', {
                        get: () => undefined
                    });
                """)
                
                page = await context.new_page()
                
                # 검색 URL로 이동
                url = f"https://medicalxpress.com/search/page1.html?search={search_term}&s=1"
                await page.goto(url, timeout=30000)
                
                # Press and Hold 버튼 처리
                await handle_press_and_hold(page)
                
                # 페이지 로딩 대기
                await page.wait_for_load_state('networkidle')
                
                # 검색 결과 추출
                search_results = []
                articles = await page.query_selector_all('article.sorted-article')
                
                for article in articles:
                    try:
                        # 기사 제목과 링크
                        title_elem = await article.query_selector('h2 a')
                        title = await title_elem.text_content() if title_elem else None
                        link = await title_elem.get_attribute('href') if title_elem else None
                        
                        # 기사 요약
                        summary_elem = await article.query_selector('p.mb-4')
                        summary = await summary_elem.text_content() if summary_elem else None
                        
                        # 카테고리/토픽
                        topic_elem = await article.query_selector('div.sorted-article-topic')
                        topic = await topic_elem.text_content() if topic_elem else None
                        
                        # 날짜 정보
                        date_elem = await article.query_selector('p.text-uppercase.text-low')
                        relative_date = await date_elem.text_content() if date_elem else None
                        absolute_date = convert_relative_date(relative_date) if relative_date else None
                        
                        # 이미지 URL
                        img_elem = await article.query_selector('img')
                        img_url = await img_elem.get_attribute('src') if img_elem else None
                        
                        if title and link:
                            search_results.append({
                                'search_term': search_term,
                                'title': title.strip(),
                                'link': f"https://medicalxpress.com{link}" if link.startswith('/') else link,
                                'summary': summary.strip() if summary else None,
                                'topic': topic.strip() if topic else None,
                                'date': absolute_date,
                                'image_url': img_url
                            })
                    except Exception as e:
                        print(f"Error extracting article data: {str(e)}")
                        continue
                
                if not search_results:
                    if attempt < max_retries - 1:
                        print("No search results found, retrying...")
                        continue
                    return None
                
                return pd.DataFrame(search_results)
                
            finally:
                await playwright.stop()
                
        except Exception as e:
            if attempt < max_retries - 1:
                print(f"Error fetching search results: {str(e)}, retrying...")
                continue
            print(f"Error fetching search results after {max_retries} attempts: {str(e)}")
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

@tool("scrape_medicalxpress_articles", return_direct=True)
async def scrape_medicalxpress_articles(search_term: str, min_relevance: float = 0.1, max_articles: int = None) -> List[Dict]:
    """
    Searches for articles on MedicalXpress.com and scrapes their full content in parallel.
    
    Args:
        search_term (str): The search term to query (e.g., "diabetes")
        min_relevance (float): Minimum relevance score (0-1) for including articles (default: 0.1)
        max_articles (int): Maximum number of articles to scrape (default: None)
        
    Returns:
        List[Dict]: List of articles with full content and metadata
    """
    # 먼저 검색 결과 가져오기
    search_tool = scrape_medicalxpress_search
    search_results = await search_tool.ainvoke({
        "search_term": search_term,
        "min_relevance": min_relevance
    })
    
    if not search_results:
        return []
    
    # 상위 N개의 기사만 선택
    if max_articles is None or max_articles > len(search_results) or max_articles <= 0:
        max_articles = len(search_results)
    selected_articles = search_results[:max_articles]
    
    async def scrape_article_with_metadata(article: Dict) -> Optional[Dict]:
        """단일 기사의 내용을 스크래핑하고 메타데이터와 병합"""
        try:
            content_tool = scrape_medicalxpress_article
            content = await content_tool.ainvoke({"url": article['link']})
            if content:
                return {
                    **article,  # 검색 결과 메타데이터 (relevance_score 포함)
                    **content  # 기사 전체 내용
                }
        except Exception as e:
            print(f"Error scraping article {article['link']}: {str(e)}")
            return None
    
    # 모든 기사를 동시에 스크래핑
    tasks = [scrape_article_with_metadata(article) for article in selected_articles]
    results = await asyncio.gather(*tasks)
    
    # None 값 제거하고 성공적으로 스크래핑된 기사만 반환
    return [result for result in results if result is not None]

# 사용 예시
if __name__ == "__main__":
    async def main():
        try:
            # Tool 실행
            tool = scrape_medicalxpress_articles
            results = await tool.ainvoke({
                "search_term": "diabetes", 
                "min_relevance": 0.1,
                "max_articles": 0  # 전체 기사 가져오기
            })
            
            if results:
                # JSON 파일로 저장
                filename = f"medicalxpress_full_articles_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
                with open(filename, 'w', encoding='utf-8') as f:
                    json.dump(results, ensure_ascii=False, indent=2, fp=f)
                print(f"Full articles saved to {filename}")
                print(f"Successfully scraped {len(results)} articles")
            else:
                print("No articles found")
                
        except Exception as e:
            print(f"Error: {str(e)}")
    
    # 비동기 실행
    asyncio.run(main())