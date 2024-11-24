import os
from dotenv import load_dotenv
from typing import List, Dict, Any
import json
from dataclasses import dataclass
from collections import defaultdict
from langchain_openai import ChatOpenAI, OpenAIEmbeddings
from langchain_community.vectorstores import Chroma
from langchain.chains import LLMChain
from langchain.prompts import PromptTemplate
from langchain.chains.summarize import load_summarize_chain
from langchain.docstore.document import Document
from langchain.text_splitter import RecursiveCharacterTextSplitter
import time

@dataclass
class RaptorNode:
    content: str
    metadata: Dict[str, Any]
    children: List['RaptorNode']
    summary: str = ""
    embedding: List[float] = None
    
class RaptorTree:
    def __init__(self, openai_api_key: str):
        self.root = RaptorNode("root", {}, [], "")
        self.llm = ChatOpenAI(
            temperature=0, 
            openai_api_key=openai_api_key,
            model="gpt-4o",  
            max_tokens=1000  # 더 긴 응답 허용
        )
        self.embeddings = OpenAIEmbeddings(
            openai_api_key=openai_api_key,
            model="text-embedding-3-small"
        )
        self.vector_store = None
        self.text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=1000,
            chunk_overlap=200,
            length_function=len
        )

    def build_from_documents(self, documents: List[Dict]):
        """문서 처리 및 트리 구축"""
        topic_groups = defaultdict(list)
        
        # 벡터 저장소 초기화
        texts = []
        metadatas = []
        
        # description별 분석 결과를 캐싱
        analysis_cache = {}
        
        # 1단계: 문서 분석 및 주제별 그룹화
        for doc in documents:
            description = doc['metadata']['description']
            
            # 캐시된 분석 결과가 있는지 확인
            if description not in analysis_cache:
                doc_analysis = self._generate_document_summary(doc)
                medical_topic = self._extract_medical_topic(
                    doc['metadata']['description'], 
                    doc_analysis["summary"]
                )
                
                analysis_cache[description] = {
                    "analysis": doc_analysis,
                    "topic": medical_topic
                }
            
            # 벡터 저장소용 메타데이터 준비
            metadata = {
                "title": doc['metadata']['title'],
                "source": doc['metadata']['source'],
                "description": doc['metadata']['description'],
                "language": doc['metadata']['language'],
                "topic": analysis_cache[description]["topic"],
                "summary": analysis_cache[description]["analysis"]["summary"],
                "questions": json.dumps(analysis_cache[description]["analysis"]["questions"])
            }
            
            texts.append(doc['page_content'])
            metadatas.append(metadata)
            
            # 주제별 그룹화
            topic_groups[analysis_cache[description]["topic"]].append({
                **doc,
                "analysis": analysis_cache[description]["analysis"]
            })
        
        # 2단계: 트리 구조 구축
        # 루트 노드 초기화
        self.root = RaptorNode("의료 정보 문서", {"type": "root"}, [], "")
        
        # 주제별 노드 생성
        for topic, docs in topic_groups.items():
            # 주제 노드 생성
            topic_summary = self._generate_topic_summary(docs)
            topic_node = RaptorNode(
                content=topic,
                metadata={"type": "topic", "document_count": len(docs)},
                children=[],
                summary=topic_summary
            )
            
            # 문서 노드 추가
            for doc in docs:
                doc_node = RaptorNode(
                    content=doc['page_content'],
                    metadata={
                        "type": "document",
                        "title": doc['metadata']['title'],
                        "source": doc['metadata']['source'],
                        "description": doc['metadata']['description']
                    },
                    children=[],
                    summary=doc['analysis']['summary']
                )
                
                # 질문 노드 추가
                for question in doc['analysis']['questions']:
                    question_node = RaptorNode(
                        content=question['question'],
                        metadata={
                            "type": "question",
                            "category": question['category'],
                            "importance": question.get('importance', '중'),
                            "search_keywords": question['search_keywords']
                        },
                        children=[],
                        summary=""
                    )
                    doc_node.children.append(question_node)
                
                topic_node.children.append(doc_node)
            
            self.root.children.append(topic_node)
        
        # 3단계: 벡터 저장소 생성
        self.vector_store = Chroma.from_texts(
            texts=texts,
            metadatas=metadatas,
            embedding=self.embeddings
        )

    def _group_chunks_by_source(self, docs: List[Dict]) -> Dict[str, List[Dict]]:
        """같은 출처의 청크들을 그룹화"""
        grouped = defaultdict(list)
        for doc in docs:
            source = doc['metadata']['source']
            grouped[source].append(doc)
        return grouped

    def _generate_document_summary(self, doc: Dict) -> Dict[str, Any]:
        """문서 요약 및 핵심 질문 생성"""
        max_length = 8000
        text = doc['page_content'][:max_length]
        
        # 의료 정보 요약 프롬프트
        summary_prompt = PromptTemplate(
            input_variables=["text"],
            template="""다음 의료/건강 관련 문서를 분석하여 요약해주세요:

1. 핵심 요약 (400토큰 이내):
- 주요 의료/건강 정보
- 관련 약물, 치료법, 또는 의료 기기
- 주요 연구/임상 결과나 정책 변화

2. 환자 관련 정보:
- 대상 환자군 또는 질환
- 주의사항 및 권고사항
- 실제 적용 방법

3. 의료진 참고사항:
- 임상적 의의
- 처방/치료 시 고려사항
- 최신 가이드라인 변경점

4. 추가 정보:
- 관련 기관 및 문의처
- 참고할 만한 연구나 문헌
- 후속 조치나 모니터링 계획

문서:
{text}

응답:"""
        )

        try:
            # 직접 LLM으로 요약 생성
            chain = summary_prompt | self.llm
            summary_result = chain.invoke({"text": text})
            summary = summary_result.content

            # 질문 생성
            questions = self._generate_medical_questions(text, summary)
            
            # 주제 추출
            topic = self._extract_medical_topic(doc['metadata']['title'], summary)

            return {
                "summary": summary,
                "questions": questions,
                "metadata": {
                    "title": doc['metadata']['title'],
                    "source": doc['metadata']['source'],
                    "description": doc['metadata']['description'],
                    "language": doc['metadata']['language'],
                    "topic": topic
                }
            }
        except Exception as e:
            print(f"문서 요약 생성 중 오류 발생: {e}")
            return {
                "summary": doc['metadata'].get('description', '요약 생성 실패'),
                "questions": [],
                "metadata": doc['metadata']
            }

    def _generate_medical_questions(self, text: str, summary: str) -> List[Dict[str, str]]:
        """의료 정보 관련 질문 생성 - 답변은 후속 RAG 단계에서 처리"""
        question_prompt = PromptTemplate(
            input_variables=["text", "summary"],
            template="""다음 의료/건강 관련 문서와 요약을 바탕으로, 독자들이 가질 수 있는 중요한 질문 3개만 생성해주세요.

문서:
{text}

요약:
{summary}

다음 형식으로 정확히 3개의 질문을 JSON 배열로 생성해주세요:
[
    {{"category": "치료/약물 효과", "question": "질문내용", "search_keywords": ["키워드1", "키워드2"]}},
    {{"category": "안전성/부작용", "question": "질문내용", "search_keywords": ["키워드1", "키워드2"]}},
    {{"category": "실제 적용", "question": "질문내용", "search_keywords": ["키워드1", "키워드2"]}}
]"""
        )

        try:
            chain = question_prompt | self.llm
            result = chain.invoke({
                "text": text,
                "summary": summary
            })
            
            # JSON 파싱 전처리
            content = result.content.strip()
            if "```" in content:
                content = content.split("```")[1]
                if content.startswith("json"):
                    content = content[4:]
            content = content.strip()
            
            try:
                questions = json.loads(content)
                filtered_questions = []
                
                # 중요도 "상"인 질문 먼저 추가
                for q in questions:
                    if q.get("importance") == "상":
                        keywords = q.get("search_keywords", "").split(",") if isinstance(q.get("search_keywords"), str) else []
                        filtered_questions.append({
                            "category": q.get("category", "기타"),
                            "question": q.get("question", ""),
                            "importance": "상",
                            "search_keywords": keywords
                        })
                
                # 남은 자리가 있으면 중요도 "중"인 질문 추가
                if len(filtered_questions) < 3:
                    for q in questions:
                        if len(filtered_questions) >= 3:
                            break
                        if q.get("importance") == "중":
                            keywords = q.get("search_keywords", "").split(",") if isinstance(q.get("search_keywords"), str) else []
                            filtered_questions.append({
                                "category": q.get("category", "기타"),
                                "question": q.get("question", ""),
                                "importance": "중",
                                "search_keywords": keywords
                            })
                
                # 여전히 남은 자리가 있으면 중요도 "하"인 질문 추가
                if len(filtered_questions) < 3:
                    for q in questions:
                        if len(filtered_questions) >= 3:
                            break
                        if q.get("importance") == "하":
                            keywords = q.get("search_keywords", "").split(",") if isinstance(q.get("search_keywords"), str) else []
                            filtered_questions.append({
                                "category": q.get("category", "기타"),
                                "question": q.get("question", ""),
                                "importance": "하",
                                "search_keywords": keywords
                            })
                
                return filtered_questions[:3]  # 최대 3개 질문 반환
                
            except json.JSONDecodeError as e:
                print(f"JSON 파싱 오류: {e}")
                print(f"받은 응답: {result.content}")
                return []
                
        except Exception as e:
            print(f"질문 생성 중 오류 발생: {e}")
            return []

    def _extract_medical_topic(self, title: str, summary: str) -> str:
        """의료/건강 관련 주제를 의미론적으로 분류"""
        from langchain.prompts import PromptTemplate
        
        topic_template = """
        다음 의료/건강 관련 문서의 제목과 요약을 분석하여, 가장 적절한 주제 카테고리와 분류 신뢰도를 평가해주세요.

        주제 카테고리:
        1. 약물치료: 약물, 처방, 투약 관련 정보
        2. 질병관리: 질환의 진단, 증상, 관리, 예방
        3. 임상연구: 의학 연구, 임상시험, 새로운 치료법
        4. 건강정보: 일반적인 건강 관리, 생활 수칙
        5. 의료정책: 보험, 제도, 규제, 의료 시스템
        6. 신약개발: 신약 연구, 개발, 허가, 출시
        7. 의료기기: 의료장비, 진단기기, 치료기기
        8. 디지털헬스케어: AI 의료, 디지털 치료제, 원격의료
        9. 기타: 위 카테고리에 명확히 속하지 않는 경우

        응답 형식:
        카테고리명|신뢰도점수(0-100)

        분류 기준:
        - 신뢰도 70점 이상: 해당 카테고리로 명확히 분류 가능
        - 신뢰도 70점 미만: '기타'로 분류
        - 여러 주제가 혼재된 경우: 가장 주된 주제 선택, 신뢰도는 그에 따라 조정

        제목: {title}
        요약: {summary}

        주제 분류 결과:
        """
        
        topic_prompt = PromptTemplate(
            template=topic_template,
            input_variables=["title", "summary"]
        )

        try:
            # 최신 방식으로 체인 실행
            chain = topic_prompt | self.llm
            result = chain.invoke({
                "title": title,
                "summary": summary
            })
            
            # 응답 파싱
            try:
                topic, confidence = result.content.strip().split('|')
                confidence = float(confidence)
            except ValueError:
                # 파싱 실패 시 기본값 사용
                return "기타"
            
            # 기본 카테고리 목록
            valid_topics = [
                "약물치료", "질병관리", "임상연구", "건강정보", 
                "의료정책", "신약개발", "의료기기", "디지털헬스케어", "기타"
            ]
            
            # 신뢰도 검사 및 카테고리 검증
            if topic.strip() not in valid_topics or confidence < 70:
                return "기타"
                
            return topic.strip()
            
        except Exception as e:
            print(f"주제 분류 중 오류 발생: {e}")
            return "기타"  # 오류 발생 시 기본값

    def _generate_topic_summary(self, docs: List[Dict]) -> str:
        """주제별 요약 생성"""
        combined_text = "\n".join([doc['page_content'][:1000] for doc in docs[:5]])
        
        # 프롬프트 템플릿 생성
        summary_prompt = PromptTemplate(
            input_variables=["text"],
            template="""
            다음은 동일한 주제와 관련된 여러 문서들입니다. 
            이 문서들의 공통된 주제와 핵심 내용을 500토큰 이내로 요약해주세요.
            가능한 경우 다음 정보를 포함해주세요:
            - 주요 이슈나 트렌드
            - 관련된 기관이나 인물
            - 중요한 수치나 통계
            - 시사점이나 향후 전망

            문서들:
            {text}
            
            요약:
            """
        )
        
        try:
            # 최신 방식으로 체인 실행
            chain = summary_prompt | self.llm
            result = chain.invoke({
                "text": combined_text
            })
            return f"관련 문서 {len(docs)}건: {result.content}"
        except Exception as e:
            print(f"주제 요약 생성 중 오류 발생: {e}")
            return f"관련 문서 {len(docs)}건"

    def search(self, query: str, max_results: int = 5) -> List[Dict]:
        """개선된 하이브리드 검색"""
        try:
            query_embedding = self.embeddings.embed_query(query)
            
            # 트리 검색
            tree_results = []
            self._search_recursive(self.root, query, query_embedding, tree_results)
            
            # 벡터 검색
            vector_results = []
            if self.vector_store:
                try:
                    vector_results = self.vector_store.similarity_search_with_score(
                        query,
                        k=max_results
                    )
                except Exception as e:
                    print(f"벡터 검색 중 오류 발생: {e}")
            
            # 결과 통합 및 정렬
            combined_results = []
            seen_titles = set()
            
            # 트리 검색 결과 처리
            for node, score in tree_results:
                metadata = node.metadata or {}
                title = metadata.get('title', 'Unknown Title')
                
                if title not in seen_titles:
                    combined_results.append({
                        "content": node.content,
                        "metadata": metadata,
                        "score": score,
                        "search_type": "tree",
                        "summary": node.summary or "",
                        "importance_score": getattr(node, 'importance_score', 0.0),
                        "title": title  # 명시적으로 title 추가
                    })
                    seen_titles.add(title)
            
            # 벡터 검색 결과 처리
            for doc, score in vector_results:
                metadata = getattr(doc, 'metadata', {}) or {}
                title = metadata.get('title', 'Unknown Title')
                
                if title not in seen_titles:
                    combined_results.append({
                        "content": getattr(doc, 'page_content', ''),
                        "metadata": metadata,
                        "score": score,
                        "search_type": "vector",
                        "summary": metadata.get("summary", ""),
                        "importance_score": metadata.get("importance_score", 0.0),
                        "title": title  # 명시적으로 title 추가
                    })
                    seen_titles.add(title)
            
            # 최종 점수 계산 및 정렬
            for result in combined_results:
                result["final_score"] = (
                    result["score"] * 0.6 +
                    result["importance_score"] * 0.4
                )
            
            sorted_results = sorted(
                combined_results, 
                key=lambda x: x["final_score"], 
                reverse=True
            )
            
            return sorted_results[:max_results]
            
        except Exception as e:
            print(f"검색 중 오류 발생: {e}")
            return []

    def _search_recursive(self, node: RaptorNode, query: str, query_embedding: List[float], results: List[RaptorNode]):
        # 컨텐츠와 요약에서 검색어 매칭
        if query.lower() in node.content.lower() or query.lower() in node.summary.lower():
            results.append(node)
            
        # 자식 노드들도 재귀적으로 검색
        for child in node.children:
            self._search_recursive(child, query, query_embedding, results)
            
    def save_to_disk(self, path: str):
        """트리 구조를 디스크에 저장"""
        if self.vector_store:
            self.vector_store.persist()
            
    def load_from_disk(self, path: str):
        """디스크에서 트리 구조 로드"""
        self.vector_store = Chroma(
            embedding_function=self.embeddings,
            persist_directory=path
        )

    def _calculate_tree_score(self, node: RaptorNode, query: str) -> float:
        """트리 검색 결과의 점수 계산"""
        base_score = 0.8
        content_match = 0.1 if query.lower() in node.content.lower() else 0
        summary_match = 0.1 if query.lower() in node.summary.lower() else 0
        return base_score + content_match + summary_match

# 메인 실행 코드를 if __name__ == "__main__": 블록으로 이동
if __name__ == "__main__":
    load_dotenv()
    
    # 초기화
    raptor = RaptorTree(openai_api_key=os.getenv("OPENAI_API_KEY"))

    # 문서로부터 트리 구축
    with open("./data/dailymedi_news.json", "r") as f:
        documents = json.load(f)
    raptor.build_from_documents(documents)

    # 검색 수행
    results = raptor.search("의료정책", max_results=5)

    # 결과 저장
    raptor.save_to_disk("raptor_index")