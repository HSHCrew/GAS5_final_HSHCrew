import aiohttp
import asyncio
from bs4 import BeautifulSoup
import pandas as pd
from datetime import datetime
import time
import random
import json
from langchain.tools import tool
from typing import Optional, Dict, Tuple
from playwright.async_api import async_playwright

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

@tool("scrape_medicalxpress_article", return_direct=True)
async def scrape_medicalxpress_article(url: str, max_retries: int = 3) -> Optional[Dict]:
    """
    Scrapes article content from MedicalXpress.com including editors' notes.
    
    Args:
        url (str): Full URL of the MedicalXpress article starting with 'https://medicalxpress.com/news/'
        
    Returns:
        dict: Article data containing title, author, date, description, content, topics, image URL, and editors' notes
    """
    for attempt in range(max_retries):
        try:
            if attempt > 0:
                wait_time = (2 ** attempt) + random.uniform(1, 3)
                print(f"Retry attempt {attempt + 1}/{max_retries} for {url}, waiting {wait_time:.1f} seconds...")
                await asyncio.sleep(wait_time)
            
            # URL 유효성 검사
            if not url.startswith('https://medicalxpress.com/news/'):
                raise ValueError("Invalid URL. URL must start with 'https://medicalxpress.com/news/'")
            
            # 요청 전 잠시 대기 (1-3초 랜덤)
            await asyncio.sleep(random.uniform(1, 3))
            
            # 브라우저 타입과 User Agent 선택
            browser_type, user_agent = get_random_browser_config()
            
            async with async_playwright() as p:
                # 선택된 브라우저 타입으로 브라우저 실행
                browser = await getattr(p, browser_type).launch(headless=True)
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
                
                # 페이지 로드 타임아웃 증가
                await page.goto(url, timeout=30000)
                
                # 에디터 노트 버튼 클릭 (있는 경우)
                editors_notes_button = page.locator('a[data-toggle="factcheck"]')
                if await editors_notes_button.count() > 0:
                    await editors_notes_button.click()
                    # 팝오버가 나타날 때까지 대기
                    await page.wait_for_selector('.popover-body', timeout=5000)
                    
                    # 에디터 노트 텍스트 추출 및 정리
                    raw_notes = await page.locator('.popover-body').text_content()
                    
                    # 텍스트 정리
                    notes_text = raw_notes.strip()
                    # 여러 줄의 공백을 하나의 공백으로 변경
                    notes_text = ' '.join(line.strip() for line in notes_text.splitlines() if line.strip())
                    
                    # 속성 목록 추출
                    attributes = []
                    if 'fact-checked' in notes_text:
                        attributes.append('fact-checked')
                    if 'reputable news agency' in notes_text:
                        attributes.append('reputable news agency')
                    if 'proofread' in notes_text:
                        attributes.append('proofread')
                    
                    editors_notes = {
                        'text': notes_text,
                        'attributes': attributes
                    }
                else:
                    editors_notes = None
                
                # HTML 파싱
                html = await page.content()
                soup = BeautifulSoup(html, 'html.parser')
                
                # 주요 정보 추출 (안전하게 처리)
                title_meta = soup.find('meta', property='og:title')
                description_meta = soup.find('meta', property='og:description')
                image_meta = soup.find('meta', property='og:image')
                
                if not title_meta or not description_meta:
                    print(f"Required meta tags not found for {url}")
                    return None
                
                article_data = {
                    'title': title_meta['content'],
                    'author': soup.select_one('.article-byline.text-low').text.strip().replace('by ', '') if soup.select_one('.article-byline.text-low') else 'Unknown',
                    'date': soup.select_one('.article__info-item.mr-auto p.text-uppercase.text-low').text.strip() if soup.select_one('.article__info-item.mr-auto p.text-uppercase.text-low') else None,
                    'description': description_meta['content'],
                    'content': soup.find('div', class_='article-main').get_text(strip=True) if soup.find('div', class_='article-main') else None,
                    'topics': [topic.text for topic in soup.find_all('meta', property='article:section')],
                    'image_url': image_meta['content'] if image_meta else None,
                    'editors_notes': editors_notes
                }
                
                # 필수 필드가 없는 경우 None 반환
                if not article_data['content']:
                    print(f"Article content not found for {url}")
                    return None
                
                await browser.close()
                return article_data
                
        except Exception as e:
            if attempt < max_retries - 1:
                print(f"Error processing article {url}: {str(e)}, retrying...")
                continue
            print(f"Error processing article {url}: {str(e)} after {max_retries} attempts")
            return None

# 사용 예시
if __name__ == "__main__":
    # 테스트 URL
    test_url = "https://medicalxpress.com/news/2024-11-heartburn-heart-condition-expert.html"
    
    async def main():
        try:
            # Tool 실행
            tool = scrape_medicalxpress_article
            result = await tool.ainvoke({"url": test_url})
            
            if result:
                # JSON 파일로 저장
                filename = f"medicalxpress_article_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
                with open(filename, 'w', encoding='utf-8') as f:
                    json.dump(result, ensure_ascii=False, indent=2, fp=f)
                print(f"Article data saved to {filename}")
            else:
                print("Failed to scrape article data")
                
        except Exception as e:
            print(f"Error: {str(e)}")
    
    # 비동기 실행
    asyncio.run(main())