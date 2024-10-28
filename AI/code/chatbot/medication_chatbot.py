from db_manager import MedicationDatabaseManager
from medication_info_manager import MedicationInfoManager
from chatbot_manager import ChatbotManager
from langchain_openai import ChatOpenAI
from dotenv import load_dotenv


class MedicationChatbot:
    def __init__(self):
        load_dotenv()
        self.llm = ChatOpenAI(
            temperature=0,
            # model_name='gpt-4o',
            )
        self.db_manager = MedicationDatabaseManager()
        self.med_info_manager = MedicationInfoManager(self.llm)
        self.chatbot_manager = ChatbotManager(self.llm)
    
    # 약물정보 추가
    def add_medication_info(self, user_id, medication_info):
        # 약물 정보를 db에 저장
        self.db_manager.store_medication_info(user_id, medication_info)
        # 약물 정보의 요약본을 db에 저장
        summary = self.med_info_manager.summarize_medication_info(medication_info)
        self.db_manager.store_summary(user_id, summary)
        # print('debugging: add_medication_info:',summary)
        return summary

    # 약물정보 요약본 요청
    def ask_question(self, user_id, user_question):
        conn = self.db_manager.connect()
        cursor = conn.cursor()
        cursor.execute("SELECT summary FROM summaries WHERE user_id = ?", (user_id,))
        result = cursor.fetchall()
        # print('debugging: ask_question: result:',result)
        summary = result[-1] if result else None
        conn.close()
        
        if summary:
            # print('debugging: ask_question:',summary)
            return self.chatbot_manager.get_user_question_response(user_id, user_question, summary)
        else:
            return "요약된 약물 정보가 없습니다. 약물 정보를 먼저 저장하세요."
