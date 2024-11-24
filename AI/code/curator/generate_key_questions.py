from langchain_openai import ChatOpenAI
from langchain.prompts import ChatPromptTemplate
from langchain.tools import tool
from typing import List, Dict
import json
import os
from dotenv import load_dotenv
from langchain_teddynote import logging
import asyncio

load_dotenv()

logging.langsmith(project_name="gas5fp_curator")

# ChatGPT 모델 초기화
chat = ChatOpenAI(
    model="gpt-4o",
    temperature=0.3,
    openai_api_key=os.getenv('OPENAI_API_KEY')
)

QUESTION_PROMPT_TEMPLATE = """The following is the content of medical news articles. Please analyze this content comprehensively and generate 5 key questions.

Article content:
{article_content}

Please generate questions based on the following criteria:
1. Questions related to the core themes of the articles
2. Questions about the major research findings or discoveries discussed in the articles
3. Questions regarding the impact on actual medical practice or patients
4. Questions about future research directions or challenges
5. Questions that help integrate and understand the content of the articles

Output format:
1. [First question]
2. [Second question]
3. [Third question]
4. [Fourth question]
5. [Fifth question]

Each question should be clear and specific, allowing for an in-depth exploration of the article's content."""

@tool("generate_key_questions", return_direct=True)
async def generate_key_questions(articles: List[Dict]) -> List[Dict]:
    """
    Generates 5 key questions from the each content of a single medical article.
    
    Args:
        articles (List[Dict]): List of dictionaries containing article content and metadata
        
    Returns:
        List[Dict]: List of articles with added key_questions field
    """
    async def process_article(article):
        """단일 기사에 대한 질문 생성"""
        try:
            if not article.get('content'):
                print(f"Skipping article '{article.get('title', 'Unknown')}' due to missing content")
                return article
            
            # 프롬프트 생성
            prompt = ChatPromptTemplate.from_template(QUESTION_PROMPT_TEMPLATE)
                
            # 메시지 생성
            messages = prompt.format_messages(
                article_content=f"title: {article['title']}\ncontent: {article['content']}\n"
            )
                
            # ChatGPT로 질문 생성
            response = await chat.ainvoke(messages)

            # 응답 파싱
            questions = [
                q.strip() for q in response.content.split('\n')
                if q.strip() and q.strip()[0].isdigit()
            ]
                
            # 번호와 점 제거
            questions = [q.split('. ', 1)[1] if '. ' in q else q for q in questions]
                
            # 기존 기사 데이터 복사 후 질문 추가
            article_with_questions = article.copy()
            article_with_questions['key_questions'] = questions[:5]  # 정확히 5개의 질문만 저장
            return article_with_questions
        except Exception as e:
            print(f"Error generating questions for article '{article.get('title', 'Unknown')}': {str(e)}")
            return article
        
    try:
        result_articles = await asyncio.gather(*[process_article(article) for article in articles])
        return result_articles
        
    except Exception as e:
        print(f"Error generating questions: {str(e)}")
        return articles

# 사용 예시
if __name__ == "__main__":
    async def main():
        try:
            # 테스트용 기사 데이터 로드
            path = "./data/medi_xpress/medicalxpress_full_articles_20241119_000025.json"
            with open(path, 'r', encoding='utf-8') as f:
                articles = json.load(f)
            
            # 도구 실행
            tool = generate_key_questions
            result_articles = await tool.ainvoke({"articles": articles})
            
            if result_articles:
                # print("\nGenerated Key Questions:")
                # for i, article in enumerate(result_articles, 1):
                #     print(f"\nArticle {i}: {article['title']}")
                #     print("Key Questions:")
                #     for j, question in enumerate(article['key_questions'], 1):
                #         print(f"{j}. {question}")
                with open(path, 'w', encoding='utf-8') as f:
                    json.dump(result_articles, f, ensure_ascii=False, indent=4)
            else:
                print("Failed to generate questions")
                
        except Exception as e:
            print(f"Error: {str(e)}")
    
    # 비동기 실행
    import asyncio
    asyncio.run(main())
