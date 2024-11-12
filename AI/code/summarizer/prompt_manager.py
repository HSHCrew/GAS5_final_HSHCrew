import os
from langchain_core.prompts import load_prompt, PromptTemplate

class PromptManager:
    def __init__(self, base_dir: str):
        self.prompt_dir = os.path.join(base_dir, 'prompts')

    async def get_prompt_template(self, usage: str) -> PromptTemplate:
        '''목적에 따른 프롬프트 호출'''
        try:
            prompt_path = os.path.join(self.prompt_dir, f'{usage}.yaml')
            return load_prompt(prompt_path, encoding='utf-8')
        except FileNotFoundError:
            raise ValueError(f"Prompt template '{usage}' not found")
        except Exception as e:
            raise ValueError(f"Error loading prompt template: {str(e)}") 