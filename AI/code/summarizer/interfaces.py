from typing import Protocol
from .models import ProcessResult, Topic

class IPromptManager(Protocol):
    async def get_prompt_template(self, usage: str) -> str:
        pass

class IProcessor(Protocol):
    async def process(self, content: str) -> ProcessResult:
        pass 