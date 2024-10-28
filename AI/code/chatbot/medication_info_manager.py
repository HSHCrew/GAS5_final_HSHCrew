from langchain.prompts import PromptTemplate
from langchain_core.prompts import load_prompt
import os

class MedicationInfoManager:
    def __init__(self, llm):
        self.llm = llm
        # self.medication_prompt = PromptTemplate(
        #     input_variables=["medication_info"],
        #     template="사용자가 복용 중인 약물 정보: {medication_info}. 이 정보를 요약해 주세요."
        # )
        self.medication_prompt = load_prompt('prompts/summarize_info.yaml', encoding='utf-8')

    def summarize_medication_info(self, medication_info):
        # formatted_prompt = self.medication_prompt.format(medication_info=medication_info)
        # summary = self.llm.invoke(formatted_prompt)
        chain = self.medication_prompt | self.llm
        summary = chain.invoke({'medication_info':medication_info})
        # print('debugging: summarize_medication_info:',summary.content)
        return summary.content

# print(os.getcwd())
