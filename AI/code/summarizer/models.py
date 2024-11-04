from dataclasses import dataclass
from typing import Optional
from pydantic import BaseModel, Field

@dataclass
class ProcessResult:
    '''Process 결과 데이터 구조'''
    index: Optional[int] = None
    restructured: str = ""
    summary: str = ""
    fewshots: str = ""
    failed: str | int = 0

class Topic(BaseModel):
    '''verify 결과 데이터 구조'''
    score: int = Field(description="Overall Assessment Score  [0,100]")
    feedback: str = Field(description="feedback detailing what needs to be improved or added") 