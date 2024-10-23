from dotenv import load_dotenv
import os
import csv, json
import pandas as pd
from langchain.llms import OpenAI
from langchain_openai import ChatOpenAI
from langchain_core.prompts import load_prompt

class Summarizer:
    def __init__(self):
        load_dotenv()
        self.llm = ChatOpenAI(
            temperature=0,
            model_name="gpt-4o",  # 모델명
        )

    def get_prompt_template(self, usage:str):
        '''resturct | summarize | verify'''
        prompt = load_prompt(f'prompts/{usage}.yaml', encoding='utf-8')
        return prompt
    
    def restruct(self, context, llm):
        prompt = self.get_prompt_template('restruct')
        chain = prompt | llm
        return chain.invoke(context).content
    
    def summarize(self, context, llm):
        prompt = self.get_prompt_template('summarize')
        chain = prompt | llm
        return chain.invoke(context).content
    
    def verify(self, pair:tuple, llm):
        prompt = self.get_prompt_template('verify')
        chain = prompt | llm
        return chain.invoke({'original_content':pair[0], 'summary': pair[1]}).content
    
    