import aiohttp
import asyncio
from bs4 import BeautifulSoup
import pandas as pd
from datetime import datetime
import time
import random
import json
from langchain.tools import tool
from typing import Optional, Dict

USER_AGENTS = [
    # Windows Chrome
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36',
    
    # Windows Firefox
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/119.0',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/118.0',
    
    # Windows Edge
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36 Edg/119.0.0.0',
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36 Edg/118.0.0.0',
    
    # macOS Chrome
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36',
    
    # macOS Safari
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Safari/605.1.15',
    
    # Linux Chrome
    'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36',
    'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36',
    
    # Mobile Chrome (Android)
    'Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36',
    'Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Mobile Safari/537.36',
    
    # Mobile Safari (iOS)
    'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1',
    'Mozilla/5.0 (iPhone; CPU iPhone OS 16_6_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1'
]

def get_random_headers():
    """랜덤한 User-Agent와 함께 헤더 생성"""
    return {
        'User-Agent': random.choice(USER_AGENTS),
        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7',
        'Accept-Language': 'en-US,en;q=0.9,ko;q=0.8',
        'Accept-Encoding': 'gzip, deflate, br',
        'Connection': 'keep-alive',
        'Upgrade-Insecure-Requests': '1',
        'Cache-Control': 'max-age=0',
        'Sec-Ch-Ua': '"Google Chrome";v="119", "Chromium";v="119", "Not?A_Brand";v="24"',
        'Sec-Ch-Ua-Mobile': '?0',
        'Sec-Ch-Ua-Platform': '"Windows"',
        'Sec-Fetch-Dest': 'document',
        'Sec-Fetch-Mode': 'navigate',
        'Sec-Fetch-Site': 'none',
        'Sec-Fetch-User': '?1',
        'Referer': 'https://medicalxpress.com/'
    }

@tool("scrape_medicalxpress_article", return_direct=True)
async def scrape_medicalxpress_article(url: str) -> Optional[Dict]:
    """
    Scrapes article content from MedicalXpress.com.
    
    Args:
        url (str): Full URL of the MedicalXpress article starting with 'https://medicalxpress.com/news/'
        
    Returns:
        dict: Article data containing title, author, date, description, content, topics, and image URL
        
    Raises:
        ValueError: If URL format is invalid
        RuntimeError: If scraping encounters an error
    """
    try:
        # URL 유효성 검사
        if not url.startswith('https://medicalxpress.com/news/'):
            raise ValueError("Invalid URL. URL must start with 'https://medicalxpress.com/news/'")
        
        # 요청 전 잠시 대기 (1-3초 랜덤)
        await asyncio.sleep(random.uniform(1, 3))
        
        async with aiohttp.ClientSession() as session:
            headers = get_random_headers()
            async with session.get(url, headers=headers, timeout=10) as response:
                if response.status != 200:
                    raise RuntimeError(f"HTTP Error: {response.status}")
                
                html = await response.text()
                soup = BeautifulSoup(html, 'html.parser')
                
                # 주요 정보 추출
                article_data = {
                    'title': soup.find('meta', property='og:title')['content'],
                    'author': soup.select_one('.article-byline.text-low').text.strip().replace('by ', '') if soup.select_one('.article-byline.text-low') else 'Unknown',
                    'date': soup.select_one('.article__info-item.mr-auto p.text-uppercase.text-low').text.strip() if soup.select_one('.article__info-item.mr-auto p.text-uppercase.text-low') else None,
                    'description': soup.find('meta', property='og:description')['content'],
                    'content': soup.find('div', class_='article-main').get_text(strip=True) if soup.find('div', class_='article-main') else None,
                    'topics': [topic.text for topic in soup.find_all('meta', property='article:section')],
                    'image_url': soup.find('meta', property='og:image')['content'] if soup.find('meta', property='og:image') else None,
                    'has_editors_notes': bool(soup.select_one('a[data-toggle="factcheck"]')),  # 에디터 노트 아이콘 존재 여부
                }
                
                return article_data
                
    except aiohttp.ClientError as e:
        raise RuntimeError(f"Request Error: {str(e)}")
    except Exception as e:
        raise RuntimeError(f"Error scraping article: {str(e)}")
    
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