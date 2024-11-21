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
from bs4 import BeautifulSoup
import re


# User Agents를 브라우저 타입별로 구분
BROWSER_CONFIGS = {
    'chromium': [
        # Chrome Windows
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36',
        # Chrome Mac
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36',
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36',
        # Chrome Linux
        'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36',
        'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36',
        # Chrome Mobile
        'Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Mobile Safari/537.36',
        'Mozilla/5.0 (iPhone; CPU iPhone OS 16_6_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/119.0.6045.109 Mobile/15E148 Safari/604.1',
    ],
    'firefox': [
        # Firefox Windows
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/119.0',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/118.0',
        # Firefox Mac
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/119.0',
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:109.0) Gecko/20100101 Firefox/118.0',
        # Firefox Linux
        'Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/119.0',
        'Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/118.0',
        # Firefox Mobile
        'Mozilla/5.0 (Android 13; Mobile; rv:109.0) Gecko/119.0 Firefox/119.0',
        'Mozilla/5.0 (iPhone; CPU iPhone OS 16_6_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) FxiOS/119.0 Mobile/15E148 Safari/605.1.15',
    ],
    'webkit': [
        # Safari Mac
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Safari/605.1.15',
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Safari/605.1.15',
        # Safari iOS
        'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1',
        'Mozilla/5.0 (iPad; CPU OS 17_0_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1',
        'Mozilla/5.0 (iPod touch; CPU iPhone OS 17_0_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1'
    ]
}

# User Agent 관리를 위한 클래스 추가
class UserAgentManager:
    def __init__(self):
        self.browser_configs = BROWSER_CONFIGS
        self.used_agents = set()
        self.lock = asyncio.Lock()
    
    async def get_browser_config(self) -> Tuple[str, str]:
        """
        Thread-safe하게 사용되지 않은 User Agent 반환
        """
        async with self.lock:
            available_browsers = list(self.browser_configs.keys())
            random.shuffle(available_browsers)
            
            for browser_type in available_browsers:
                available_agents = [
                    agent for agent in self.browser_configs[browser_type] 
                    if agent not in self.used_agents
                ]
                
                if available_agents:
                    user_agent = random.choice(available_agents)
                    self.used_agents.add(user_agent)
                    return browser_type, user_agent
            
            # 모든 User Agent가 사용 중인 경우, 사용된 목록 초기화
            self.used_agents.clear()
            browser_type = random.choice(available_browsers)
            user_agent = random.choice(self.browser_configs[browser_type])
            self.used_agents.add(user_agent)
            return browser_type, user_agent
    
    async def release_user_agent(self, user_agent: str):
        """
        사용이 끝난 User Agent 반환
        """
        async with self.lock:
            self.used_agents.discard(user_agent)

# 전역 UserAgentManager 인스턴스 생성
user_agent_manager = UserAgentManager()

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
    search_terms = set(search_term.lower().split('+'))
    
    # 제목에서의 검색어 출현 빈도 (가중치: 0.4)
    title_words = set(article_data['title'].lower().split())
    title_matches = len(search_terms.intersection(title_words))
    score += 0.4 * (title_matches / len(search_terms))
    
    # 요약에서의 검색어 출현 빈도 (가중치: 0.3)
    summary_words = set(article_data.get('summary', '').lower().split())
    summary_matches = len(search_terms.intersection(summary_words))
    score += 0.3 * (summary_matches / len(search_terms))
    
    # 토픽 관련성 (가중치: 0.2)
    topic_words = set(article_data.get('topic', '').lower().split())
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

async def check_for_verification_page(page) -> bool:
    """
    Verification 페이지인지 확인
    """
    try:
        # 페이지 내용 확인
        content = await page.content()
        if 'Please complete security verification' in content:
            print("Detected verification page")
            return True
            
        # 버튼 존재 여부 확인
        button = await page.query_selector('#holdButton')
        if button:
            print("Found verification button")
            return True
            
        return False
    except Exception as e:
        print(f"Error checking verification page: {str(e)}")
        return False

async def handle_press_and_hold(page) -> bool:
    """
    Handles the 'Press and Hold' verification page
    """
    try:
        print("Attempting verification process...")
        
        # 메타데이터 추출
        meta_data = await page.evaluate("""
            () => {
                const meta = document.querySelector('meta[data-uid][data-token][data-verify]');
                if (!meta) return null;
                return {
                    uid: meta.getAttribute('data-uid'),
                    token: meta.getAttribute('data-token'),
                    verify: meta.getAttribute('data-verify')
                };
            }
        """)
        
        if not meta_data:
            print("Failed to extract verification metadata")
            return False
        
        # 버튼 시뮬레이션
        success = await page.evaluate("""
            () => {
                return new Promise((resolve) => {
                    const button = document.querySelector('#holdButton');
                    const progressBar = document.querySelector('.progress-bar');
                    const status = document.querySelector('#status');
                    
                    if (!button || !progressBar || !status) {
                        resolve(false);
                        return;
                    }
                    
                    // 초기 이벤트 발생
                    const mousedownEvent = new MouseEvent('mousedown', {
                        bubbles: true,
                        cancelable: true,
                        view: window
                    });
                    button.dispatchEvent(mousedownEvent);
                    
                    // 프로그레스 모니터링
                    let startTime = Date.now();
                    const duration = 6000; // 6초
                    
                    const checkProgress = setInterval(() => {
                        const elapsed = Date.now() - startTime;
                        const progress = Math.min(100, (elapsed / duration) * 100);
                        
                        progressBar.style.width = `${progress}%`;
                        
                        if (elapsed >= duration) {
                            clearInterval(checkProgress);
                            
                            // 완료 이벤트 발생
                            const mouseupEvent = new MouseEvent('mouseup', {
                                bubbles: true,
                                cancelable: true,
                                view: window
                            });
                            button.dispatchEvent(mouseupEvent);
                            
                            // 검증 완료 확인
                            setTimeout(() => {
                                resolve(button.classList.contains('completed'));
                            }, 1000);
                        }
                    }, 100);
                });
            }
        """)
        
        if success:
            print("Verification completed successfully")
            try:
                # 페이지 리다이렉션 대기
                await page.wait_for_navigation(timeout=10000)
                return True
            except Exception as e:
                print(f"Navigation error after verification: {str(e)}")
                return False
        else:
            print("Verification process failed")
            return False
            
    except Exception as e:
        print(f"Verification handling error: {str(e)}")
        return False

def clean_text(text: str) -> str:
    """텍스트에서 불필요한 공백과 개행문자를 정리"""
    if not text:
        return text
    # 여러 줄의 공백을 하나의 공백으로
    cleaned = ' '.join(text.split())
    return cleaned.strip()

async def scrape_article_with_metadata(article: Dict) -> Optional[Dict]:
    """단일 기사의 내용을 스크래핑하고 메타데이터와 병합"""
    max_retries = 3
    base_wait = 5
    
    for attempt in range(max_retries):
        browser = None
        context = None
        page = None
        
        try:
            async with async_playwright() as playwright:
                browser_type, user_agent = await user_agent_manager.get_browser_config()
                
                browser = await getattr(playwright, browser_type).launch(
                    headless=True,
                    args=[
                        '--disable-blink-features=AutomationControlled',
                        '--disable-features=IsolateOrigins,site-per-process',
                        '--disable-dev-shm-usage',
                        '--disable-accelerated-2d-canvas',
                        '--no-first-run',
                        '--no-zygote',
                        '--disable-gpu'
                    ]
                )
                
                context = await browser.new_context(
                    user_agent=user_agent,
                    viewport={'width': 1920, 'height': 1080},
                    java_script_enabled=True,
                    bypass_csp=True,
                    extra_http_headers={
                        'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
                        'Accept-Language': 'en-US,en;q=0.5',
                        'Accept-Encoding': 'gzip, deflate, br',
                        'Connection': 'keep-alive',
                        'DNT': '1'
                    }
                )
                
                # 네트워크 요청 최적화
                await context.route('**/*.{png,jpg,jpeg,gif,svg,css,woff,woff2,ttf}',
                    lambda route: route.abort()
                )
                
                page = await context.new_page()
                
                # JavaScript 코드 주입
                await page.add_init_script("""
                    Object.defineProperty(navigator, 'webdriver', {get: () => undefined});
                    Object.defineProperty(navigator, 'automation', {get: () => undefined});
                    Object.defineProperty(navigator, 'plugins', {get: () => [1, 2, 3, 4, 5]});
                    Object.defineProperty(navigator, 'languages', {get: () => ['en-US', 'en']});
                """)
                
                # 랜덤 지연
                await asyncio.sleep(random.uniform(2, 5))
                
                try:
                    # 페이지 로딩 대기 설정
                    await page.route('**/*.{png,jpg,jpeg,gif,svg,css,woff,woff2,ttf}',
                        lambda route: route.abort()
                    )
                    
                    # 페이지 이동
                    response = await page.goto(article['link'], 
                                            wait_until='networkidle',
                                            timeout=30000)
                    
                    # 429 응답 처리
                    if response.status == 429:
                        wait_time = base_wait * (2 ** attempt)
                        print(f"Rate limit hit (429) for {article['link']}")
                        print(f"Waiting {wait_time}s before retry...")
                        await asyncio.sleep(wait_time)
                        continue
                    
                    # 다른 실패 응답 처리
                    if not response.ok:
                        print(f"Failed to load page (status {response.status}): {article['link']}")
                        if response.status != 429 and attempt < max_retries - 1:
                            wait_time = base_wait * (2 ** attempt)
                            print(f"Retrying in {wait_time}s...")
                            await asyncio.sleep(wait_time)
                        continue
                    
                    # DOM 로딩 대기
                    await page.wait_for_load_state('domcontentloaded')
                    
                    # 본문 컨테이너가 로드될 때까지 대기
                    selectors = [
                        'div.article-main',
                        'div[itemprop="articleBody"]',
                        'div.article-body',
                        '#article-body'
                    ]
                    
                    content_loaded = False
                    for selector in selectors:
                        try:
                            await page.wait_for_selector(selector, timeout=10000)
                            content_loaded = True
                            break
                        except:
                            continue
                    
                    if not content_loaded:
                        print(f"Content container not found for {article['link']}")
                        continue
                    
                    # 추가 렌더링 대기
                    await asyncio.sleep(2)
                    
                    # Press and Hold 버튼 처리 (필요 시)
                    if await check_for_verification_page(page):
                        if not await handle_press_and_hold(page):
                            print("Verification failed, retrying...")
                            continue
                    
                    # 에디터 노트 처리
                    editors_notes = await extract_editors_notes(page)
                    
                    # HTML 파싱
                    html = await page.content()
                    soup = BeautifulSoup(html, 'html.parser')
                    
                    # 메타데이터 추출
                    article_data = extract_article_data(soup, editors_notes)
                    
                    if article_data and article_data.get('content'):
                        return {
                            **article,  # 검색 결과 메타데이터
                            **article_data  # 상세 기사 데이터
                        }
                    else:
                        print(f"No content found for {article['link']}")
                    
                except Exception as e:
                    print(f"Error processing article {article['link']}: {str(e)}")
                    if attempt < max_retries - 1:
                        wait_time = base_wait * (2 ** attempt)
                        print(f"Retrying in {wait_time}s...")
                        await asyncio.sleep(wait_time)
                    continue
                        
                finally:
                    await page.close()
                    await context.close()
                    await browser.close()
                    
            if user_agent:
                await user_agent_manager.release_user_agent(user_agent)
                
        except Exception as e:
            print(f"Browser error for {article['link']}: {str(e)}")
            if attempt < max_retries - 1:
                wait_time = base_wait * (2 ** attempt)
                print(f"Retrying in {wait_time}s...")
                await asyncio.sleep(wait_time)
            continue
    
    return None

async def extract_editors_notes(page) -> Optional[Dict]:
    """에디터 노트 추출"""
    try:
        editors_notes_button = page.locator('a[data-toggle="factcheck"]')
        if await editors_notes_button.count() > 0:
            await editors_notes_button.click()
            await page.wait_for_selector('.popover-body', timeout=5000)
            
            raw_notes = await page.locator('.popover-body').text_content()
            notes_text = clean_text(raw_notes)
            
            attributes = []
            for attr in ['fact-checked', 'reputable news agency', 'proofread']:
                if attr.lower() in notes_text.lower():
                    attributes.append(attr)
            
            return {
                'text': notes_text,
                'attributes': attributes
            }
    except Exception as e:
        print(f"Error extracting editors notes: {str(e)}")
    return None

def extract_article_data(soup: BeautifulSoup, editors_notes: Optional[Dict]) -> Optional[Dict]:
    """BeautifulSoup를 사용하여 기사 데이터 추출"""
    try:
        # 필수 메타 태그 확인
        title_meta = soup.find('meta', property='og:title')
        description_meta = soup.find('meta', property='og:description')
        
        if not title_meta or not description_meta:
            return None
            
        # 본문 내용 추출
        article_main = soup.find('div', class_='article-main')
        if not article_main:
            return None
            
        # 불필요한 요소 제거
        for element in article_main.select('div.article__info, div.article-actions, div.article__readmore'):
            element.decompose()
        
        content = clean_text(article_main.get_text())
        
        # 저자 정보 추출 및 정리
        author_elem = soup.select_one('.article-byline.text-low')
        author = clean_text(author_elem.text).replace('by ', '') if author_elem else 'Unknown'
        
        # 날짜 정보 추출 및 정리
        date_elem = soup.select_one('.article__info-item.mr-auto p.text-uppercase.text-low')
        date = clean_text(date_elem.text) if date_elem else None
        
        return {
            'title': clean_text(title_meta['content']),
            'author': author,
            'date': date,
            'description': clean_text(description_meta['content']),
            'content': content,
            'topics': [clean_text(topic['content']) for topic in soup.find_all('meta', property='article:section')],
            'image_url': soup.find('meta', property='og:image')['content'],
            'editors_notes': editors_notes
        }
        
    except Exception as e:
        print(f"Error extracting article data: {str(e)}")
        return None

async def handle_rate_limit(page) -> bool:
    """Rate limit 페이지에서 press button 처리"""
    try:
        # Press and Hold 버튼 찾기
        button = page.locator('button:has-text("Press and Hold")')
        if await button.count() > 0:
            print("Found Press and Hold button, attempting to bypass...")
            
            # 버튼 위치 가져오기
            box = await button.bounding_box()
            if not box:
                return False
                
            # 마우스 이동 및 클릭 유지
            await page.mouse.move(box['x'] + box['width']/2, box['y'] + box['height']/2)
            await page.mouse.down()
            await asyncio.sleep(3)  # 3초 동안 클릭 유지
            await page.mouse.up()
            
            # 페이지 로딩 대기
            try:
                await page.wait_for_load_state('networkidle', timeout=10000)
                return True
            except:
                print("Failed to load page after button press")
                return False
                
    except Exception as e:
        print(f"Error handling rate limit: {str(e)}")
        return False
    
    return False

async def scrape_medicalxpress_search_raw(search_term: str, 
                                        max_retries: int = 3, 
                                        max_pages: int = 1,
                                        search_type: int = 0
                                        ) -> Optional[pd.DataFrame]:
    """검색 결과 스크래핑"""
    all_results = []
    base_wait = 5
    
    for page_num in range(1, max_pages + 1):
        for attempt in range(max_retries):
            try:
                print(f"\nScraping page {page_num}: https://medicalxpress.com/search/page{page_num}.html?search={search_term}&s={search_type}")
                
                async with async_playwright() as playwright:
                    browser_type, user_agent = await user_agent_manager.get_browser_config()
                    browser = await getattr(playwright, browser_type).launch(headless=True)
                    context = await browser.new_context(
                        user_agent=user_agent,
                        viewport={'width': 1920, 'height': 1080}
                    )
                    
                    page = await context.new_page()
                    
                    url = f"https://medicalxpress.com/search/page{page_num}.html?search={search_term}&s={search_type}"
                    print(f"\nScraping page {page_num}: {url}")
                    
                    try:
                        response = await page.goto(
                            url, 
                            wait_until='domcontentloaded',
                            timeout=30000
                        )
                        
                        await asyncio.sleep(random.uniform(3, 5))
                        
                        if response.status == 429:
                            print(f"Rate limit hit on page {page_num}, attempting to bypass...")
                            if await handle_press_and_hold(page):
                                print("Successfully bypassed rate limit")
                            else:
                                wait_time = base_wait * (2 ** attempt)
                                print(f"Failed to bypass rate limit, waiting {wait_time}s...")
                                await asyncio.sleep(wait_time)
                                continue
                        
                        # 검증 페이지 확인 및 처리
                        if await check_for_verification_page(page):
                            print("Detected verification page, attempting to handle...")
                            if not await handle_press_and_hold(page):
                                print(f"Verification failed on page {page_num}, retrying...")
                                await asyncio.sleep(base_wait)
                                continue
                        
                        # 검색 결과 대기
                        await page.wait_for_selector('article.sorted-article', 
                                                  timeout=20000,
                                                  state='visible')
                        
                        # JavaScript를 통한 콘텐츠 확인
                        articles_count = await page.evaluate("""
                            () => document.querySelectorAll('article.sorted-article').length
                        """)
                        
                        if articles_count == 0:
                            print(f"No articles found on page {page_num} after loading")
                            break
                            
                        print(f"Found {articles_count} articles on page {page_num}")
                        
                        # 검색 결과 추출
                        search_results = []
                        articles = await page.query_selector_all('article.sorted-article')
                        print(f"Found {len(articles)} articles on page {page_num}")
                        
                        page_results = []
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
                                # 날짜 정보 정리
                                if relative_date:
                                    relative_date = clean_text(relative_date)
                                    absolute_date = convert_relative_date(relative_date)
                                else:
                                    absolute_date = None
                                
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
                        
                        if search_results:
                            all_results.extend(search_results)
                            print(f"Successfully scraped {len(search_results)} articles from page {page_num}")
                            break  # 성공적으로 처리된 경우 다음 페이지로
                        
                    except Exception as e:
                        print(f"Error loading page {page_num}: {str(e)}")
                        if attempt < max_retries - 1:
                            wait_time = base_wait * (2 ** attempt)
                            print(f"Retrying page {page_num} in {wait_time}s...")
                            await asyncio.sleep(wait_time)
                            continue
                        
            finally:
                if user_agent:
                    await user_agent_manager.release_user_agent(user_agent)
    
    if not all_results:
        print("\nNo search results found across all pages")
        return None
    
    print(f"\nTotal articles found: {len(all_results)}")
    return pd.DataFrame(all_results)

@tool("scrape_medicalxpress_articles", return_direct=True)
async def scrape_medicalxpress_articles(search_term: str, 
                                      min_relevance: float = 0.1, 
                                      max_articles: int = None, 
                                      max_pages: int = 10,
                                      batch_size: int = 10) -> List[Dict]:
    """
    Searches for articles on MedicalXpress.com and scrapes their full content in batches.
    
    Args:
        search_term (str): The search term to query (e.g., "diabetes")
        min_relevance (float): Minimum relevance score (0-1) for including articles
        max_articles (int): Maximum number of articles to scrape
        max_pages (int): Maximum number of pages to search
        batch_size (int): Number of articles to process simultaneously
    """
    # 검색 결과 가져오기
    df = await scrape_medicalxpress_search_raw(
        search_term=search_term,
        max_pages=max_pages
    )
    
    if df is None or df.empty:
        return []
    
    # 관련도 점수 계산 및 필터링
    results = []
    for _, row in df.iterrows():
        article_data = row.to_dict()
        relevance = calculate_relevance_score(article_data, search_term)
        
        if relevance >= min_relevance:
            article_data['relevance_score'] = round(relevance, 3)
            results.append(article_data)
    
    # 관련도 점수로 정렬
    results.sort(key=lambda x: x['relevance_score'], reverse=True)
    
    # 최대 기사 수 제한
    if max_articles is not None and max_articles > 0:
        results = results[:max_articles]
    
    print(f"\nFound {len(results)} relevant articles to process")
    
    # 배치 단위로 처리
    final_results = []
    for i in range(0, len(results), batch_size):
        batch = results[i:i + batch_size]
        print(f"\nProcessing batch {i//batch_size + 1} ({len(batch)} articles)")
        
        # 배치 단위로 스크래핑
        tasks = [scrape_article_with_metadata(article) for article in batch]
        batch_results = await asyncio.gather(*tasks)
        
        # 성공적으로 스크래핑된 결과만 추가
        valid_results = [result for result in batch_results if result is not None]
        final_results.extend(valid_results)
        
        print(f"Successfully scraped {len(valid_results)} articles from current batch")
        
        # 배치 간 대기
        if i + batch_size < len(results):
            wait_time = random.uniform(3, 5)
            print(f"Waiting {wait_time:.1f}s before next batch...")
            await asyncio.sleep(wait_time)
    
    return final_results

if __name__ == "__main__":
    search_terms = [
        # 'diabetes+type+2',  # 당뇨 2형
        # 'diabetes+type+1',  # 당뇨 1형
        # 'diabetes',  # 당뇨(포괄적)
        # # 'diabetes+mellitus',  # 당뇨
        # # 'diabetes+insipidus', # 당뇨 중독
        # 'hypertension', # 고혈압
        # 'heart+disease', # 심장병
        # 'stroke', # 뇌졸중
        # 'hyperlipidemia', # 고지혈증
        # 'obesity', # 비만
        # 'cataract', # 백내장
        # 'rhinitis', # 비염
        'gastritis', # 위염
        # 'periodontal+disease', # 치주질환
        # 'hemorrhoids', # 출혈성 질환
        'hair+loss', # 탈모
        'Alzheimer', 'dementia' # 치매
    ]
    search_type = 0
    max_pages = 3
    
    async def process_search_term(search_term: str):
        """단일 검색어 처리"""
        try:
            print(f"\n{'='*50}")
            print(f"Starting search for '{search_term}' across {max_pages} pages...")
            print(f"{'='*50}\n")
            
            tool = scrape_medicalxpress_articles
            results = await tool.ainvoke({
                "search_term": search_term, 
                "min_relevance": 0.1,
                "max_articles": 30,
                "max_pages": max_pages,
                "batch_size": 10
            })
            
            if results:
                filename = f"medi_press_full_{search_term}_{search_type}_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
                with open(filename, 'w', encoding='utf-8') as f:
                    json.dump(results, ensure_ascii=False, indent=2, fp=f)
                print(f"\nFull articles saved to {filename}")
                print(f"Successfully scraped {len(results)} articles for '{search_term}'")
                return len(results)
            else:
                print(f"\nNo articles found for '{search_term}'")
                return 0
                
        except Exception as e:
            print(f"\nError processing '{search_term}': {str(e)}")
            return 0
    
    async def main():
        total_articles = 0
        failed_terms = []
        
        print(f"\nStarting scraping process for {len(search_terms)} search terms...")
        start_time = datetime.now()
        
        for search_term in search_terms:
            try:
                # 검색어 처리
                articles_count = await process_search_term(search_term)
                total_articles += articles_count
                
                # 검색어 간 대기 시간
                if search_term != search_terms[-1]:  # 마지막 검색어가 아닌 경우
                    wait_time = random.uniform(5, 10)
                    print(f"\nWaiting {wait_time:.1f}s before next search term...")
                    await asyncio.sleep(wait_time)
                    
            except Exception as e:
                print(f"\nFailed to process '{search_term}': {str(e)}")
                failed_terms.append(search_term)
        
        # 최종 결과 출력
        end_time = datetime.now()
        duration = end_time - start_time
        
        print("\n" + "="*50)
        print("Scraping Process Complete")
        print("="*50)
        print(f"Total articles scraped: {total_articles}")
        print(f"Total time taken: {duration}")
        
        if failed_terms:
            print("\nFailed search terms:")
            for term in failed_terms:
                print(f"- {term}")
    
    # 메인 함수 실행
    asyncio.run(main())