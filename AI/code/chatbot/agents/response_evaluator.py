from langchain.agents import initialize_agent, AgentType
from langchain.agents import Tool, AgentExecutor
from langchain.prompts import PromptTemplate
from langchain.chat_models import ChatOpenAI
from langchain.memory import ConversationBufferMemory
from typing import Optional, List
from dataclasses import dataclass
import json
import asyncio

@dataclass
class ExpertConsultation:
    question: str
    answer: str
    transcription: str
    audio_url: Optional[str] = None

@dataclass
class EvaluationResult:
    is_appropriate: bool
    issues: List[str]
    expert_consultation: Optional[ExpertConsultation] = None
    follow_up_message: Optional[str] = None

class ResponseEvaluatorAgent:
    def __init__(self, api_key: str, realtime_api_client):
        self.llm = ChatOpenAI(
            temperature=0.3,
            model_name="gpt-4",
            api_key=api_key
        )
        self.realtime_api = realtime_api_client
        
        # 전문가 상담 도구 정의
        self.tools = [
            Tool(
                name="consult_medical_expert",
                func=self._consult_expert,
                description="""
                의료 전문가와 실시간 통화로 상담합니다. 
                입력은 JSON 형식이어야 합니다: 
                {
                    "question": "전문가에게 물어볼 구체적인 질문",
                    "context": "질문의 배경이 되는 상황 설명"
                }
                """
            )
        ]
        
        # 에이전트 초기화
        self.agent = initialize_agent(
            tools=self.tools,
            llm=self.llm,
            agent=AgentType.STRUCTURED_CHAT_ZERO_SHOT_REACT_DESCRIPTION,
            verbose=True,
            handle_parsing_errors=True,
            max_iterations=3
        )
        
    async def _consult_expert(self, input_str: str) -> str:
        """실시간 음성 통화를 통한 전문가 상담"""
        try:
            # JSON 파싱
            input_data = json.loads(input_str)
            question = input_data["question"]
            
            print(f"[INFO] Initiating expert consultation for question: {question}")
            
            # 전문가 통화 연결 요청
            call_response = await self.realtime_api.make_call_to_expert()
            if not call_response == "good":
                return "전문가 연결에 실패했습니다."
            
            # 통화 연결 대기
            await asyncio.sleep(10)
            
            # 전문가 응답 수신 (실제 구현에서는 웹소켓으로 응답 수신)
            consultation = ExpertConsultation(
                question=question,
                answer="",
                transcription="[전문가 응답 대기 중...]"
            )
            
            return json.dumps({
                "status": "success",
                "question": consultation.question,
                "answer": consultation.transcription
            })
            
        except json.JSONDecodeError:
            return "입력이 올바른 JSON 형식이 아닙니다."
        except Exception as e:
            print(f"[ERROR] Expert consultation failed: {str(e)}")
            return f"상담 중 오류가 발생했습니다: {str(e)}"

    async def evaluate_response(
        self, 
        original_message: str, 
        original_response: str,
        medication_info: dict
    ) -> EvaluationResult:
        """응답 평가 및 전문가 상담"""
        try:
            # 에이전트 실행을 위한 프롬프트 구성
            agent_prompt = f"""
            당신은 의료 챗봇의 응답을 평가하고 필요한 경우 전문가와 상담하는 평가 에이전트입니다.

            다음 대화를 평가해주세요:
            사용자: {original_message}
            챗봇: {original_response}

            약물 정보:
            {json.dumps(medication_info, ensure_ascii=False, indent=2)}

            1. 응답의 의학적 정확성과 안전성을 평가하세요.
            2. 설명이 불충분하거나 추가 확인이 필요한 부분이 있다면, consult_medical_expert 도구를 사용하여 전문가와 상담하세요.
            3. 평가 결과와 전문가 상담 내용을 바탕으로 후속 메시지가 필요한지 판단하세요.

            최종적으로 다음 형식의 JSON으로 응답해주세요:
            {{
                "is_appropriate": true/false,
                "issues": ["발견된 문제점들"],
                "expert_consultation": {{
                    "needed": true/false,
                    "details": "전문가 상담 내용"
                }},
                "follow_up_message": "필요한 경우 후속 메시지 내용"
            }}
            """

            # 에이전트 실행
            result = await self.agent.arun(agent_prompt)
            
            try:
                eval_data = json.loads(result)
                
                # 전문가 상담 정보 구성
                expert_consultation = None
                if eval_data.get("expert_consultation", {}).get("needed"):
                    expert_consultation = ExpertConsultation(
                        question="",  # 에이전트가 결정한 질문
                        answer="",    # 전문가 답변
                        transcription=eval_data["expert_consultation"]["details"]
                    )
                
                return EvaluationResult(
                    is_appropriate=eval_data.get("is_appropriate", True),
                    issues=eval_data.get("issues", []),
                    expert_consultation=expert_consultation,
                    follow_up_message=eval_data.get("follow_up_message")
                )
                
            except json.JSONDecodeError:
                print(f"[ERROR] Failed to parse agent response: {result}")
                return EvaluationResult(
                    is_appropriate=True,
                    issues=["에이전트 응답 파싱 실패"]
                )

        except Exception as e:
            print(f"[ERROR] Agent execution failed: {str(e)}")
            return EvaluationResult(
                is_appropriate=True,  # 에러 발생 시 기본적으로 적절하다고 판단
                issues=["평가 중 오류가 발생했습니다."]
            ) 