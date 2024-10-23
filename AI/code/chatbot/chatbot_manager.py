from langchain.prompts import PromptTemplate
from langchain.memory import ConversationBufferMemory

class ChatbotManager:
    def __init__(self, llm):
        self.llm = llm
        self.chatbot_prompt = PromptTemplate(
            input_variables=["question", "summary"],
            template="사용자가 물어본 질문: {question}. 이 질문에 대한 답변을 {summary}참고해서 완성된 문장을 생성해 주세요."
        )
        self.user_memories = {}

    def get_user_question_response(self, user_id, user_question, summary):
        if user_id not in self.user_memories:
            self.user_memories[user_id] = ConversationBufferMemory()
        memory = self.user_memories[user_id]
        formatted_prompt = self.chatbot_prompt.format(question=user_question, summary=summary)
        response = self.llm.invoke(formatted_prompt)
        memory.save_context({"input": user_question}, {"output": response.content})
        return response.content
