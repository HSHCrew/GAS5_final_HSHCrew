from langchain.prompts import PromptTemplate
from langchain.memory import ConversationBufferMemory
from langchain_core.prompts import load_prompt
class ChatbotManager:
    def __init__(self, llm):
        self.llm = llm
        # self.chatbot_prompt = PromptTemplate(
        #     input_variables=["user_question", "summary"],
        #     template="사용자가 물어본 질문: {user_question}. 이 질문에 대한 답변을 {summary}참고해서 완성된 문장을 생성해 주세요."
        # )
        self.chatbot_prompt = load_prompt('prompts/summary_question.yaml', encoding='utf-8')
        self.user_memories = dict()

    def get_user_question_response(self, user_id, user_question, summary):
        # 유저별로 대화내역을 저장하기 위해 메모리 객체 생성
        if user_id not in self.user_memories:
            self.user_memories[user_id] = ConversationBufferMemory()
        memory = self.user_memories[user_id]
        
        # summary를 참조해 유저의 질문에 대한 답변 생성
        # formatted_prompt = self.chatbot_prompt.format(user_question=user_question, summary=summary)
        # response = self.llm.invoke(formatted_prompt)
        chain = self.chatbot_prompt | self.llm
        response = chain.invoke({'user_question':user_question, 'summary':summary})
        # 대화내역 저장
        memory.save_context({"input": user_question}, {"output": response.content})
        # print('debugging: get_user_question_response:',summary)
        return response.content
