from dataclasses import dataclass
from typing import Optional
from datetime import datetime, UTC
from pydantic import BaseModel, Field

@dataclass(frozen=False)
class ProcessResult:
    """처리 결과 데이터 구조"""
    index: Optional[int] = None
    restructured: str = ""
    summary: str = ""
    fewshots: str = ""
    failed: str = ""
    processed_at: datetime = Field(default_factory=lambda: datetime.now(UTC))
    
    @property
    def is_successful(self) -> bool:
        return not bool(self.failed)
    
    def to_dict(self) -> dict:
        return {
            "restructured": self.restructured,
            "summary": self.summary,
            "fewshots": self.fewshots,
            "failed": self.failed,
            "processed_at": self.processed_at.isoformat(),
            "success": self.is_successful
        }

class Topic(BaseModel):
    """검증 결과 데이터 구조"""
    score: int = Field(ge=0, le=100)
    feedback: str
    
    class Config:
        json_schema_extra = {
            "example": {
                "score": 85,
                "feedback": "Summary captures main points but needs more detail about dosage."
            }
        }