import requests
from bs4 import BeautifulSoup
import pandas as pd

def scrape_medicalxpress_search(search_term):
    """
    Scrapes articles' urls from MedicalXpress.com based on a search term.
    
    Args:
        search_term (str): The search term to query

    Returns:
        pandas.DataFrame: Articles' urls with title, summary, topic, date, and engagement metrics
    """
    # """
    # Scrapes search results from MedicalXpress.com based on a search term.
    
    # This function performs a search on MedicalXpress.com and extracts detailed information
    # about each article in the search results, including titles, summaries, topics, dates,
    # and engagement metrics.

    # Args:
    #     search_term (str): The search term to query on MedicalXpress.com

    # Returns:
    #     pandas.DataFrame: A DataFrame containing the following columns:
    #         - title (str): The article title
    #         - link (str): URL link to the full article
    #         - summary (str): Brief summary/excerpt of the article
    #         - topic (str): Article topic/category
    #         - date (str): Publication date
    #         - comments (str): Number of comments
    #         - shares (str): Number of shares
    #         - image_url (str): URL of the article's thumbnail image, if available

    # Raises:
    #     requests.RequestException: If there is an error fetching the webpage
        
    # Example:
    #     >>> results = scrape_medicalxpress_search("diabetes")
    #     >>> print(results.columns)
    #     Index(['title', 'link', 'summary', 'topic', 'date', 'comments', 'shares', 
    #            'image_url'])
    # """
    
    # 검색 URL 생성
    url = f"https://medicalxpress.com/search/?search={search_term}&s=0"
    
    # HTTP 요청 헤더 설정
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
    }
    
    try:
        # 웹페이지 요청
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        
        # BeautifulSoup 객체 생성
        soup = BeautifulSoup(response.text, 'html.parser')
        
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
            
            # 날짜 정보
            date = article.find('p', class_='text-uppercase text-low').text.strip()
            
            # 댓글 수와 공유 수
            info_items = article.find_all('span', class_='article__info-item')
            comments = info_items[1].find('span').text
            shares = info_items[2].find('span').text
            
            # 이미지 URL (있는 경우)
            img_elem = article.find('img')
            img_url = img_elem['src'] if img_elem else None
            
            search_results.append({
                'title': title,
                'link': link, 
                'summary': summary,
                'topic': topic,
                'date': date,
                'comments': comments,
                'shares': shares,
                'image_url': img_url
            })
        
        # 결과를 DataFrame으로 변환
        df = pd.DataFrame(search_results)
        return df
    
    except requests.RequestException as e:
        print(f"Error fetching data: {e}")
        return None

# 사용 예시
if __name__ == "__main__":
    search_term = "diabetes"
    results = scrape_medicalxpress_search(search_term)
    
    if results is not None:
        print(results)
        # CSV 파일로 저장
        results.to_csv(f'medicalxpress_{search_term}_results.csv', index=False)