from pydantic import BaseModel, Field, validator
from datetime import datetime
from typing import Literal, Optional, List, Dict, Any
import json

class ChatMessage(BaseModel):
    role: Literal["human", "assistant"]
    content: str
    timestamp: datetime = Field(default_factory=datetime.utcnow)
    metadata: Optional[Dict[str, Any]] = Field(default_factory=dict)

    @validator('content')
    def validate_content(cls, v):
        if not v or not v.strip():
            raise ValueError("Message content cannot be empty")
        return v.strip()

class MedicationInfo(BaseModel):
    summary: str
    name: str
    details: Dict[str, Any]
    
    @validator('summary', 'name')
    def validate_strings(cls, v):
        if not v or not v.strip():
            raise ValueError("Field cannot be empty")
        return v.strip()

class ChatSession(BaseModel):
    user_id: int
    medication_info: List[str] = Field(default_factory=list)
    user_info: str = ""
    created_at: datetime = Field(default_factory=datetime.utcnow)
    last_accessed: datetime = Field(default_factory=datetime.utcnow)
    metadata: Dict[str, Any] = Field(default_factory=dict)

    def to_redis_hash(self) -> dict:
        """Redis hash로 저장하기 위한 직렬화"""
        return {
            'user_id': str(self.user_id),
            'medication_info': json.dumps(self.medication_info),
            'user_info': self.user_info or "",
            'created_at': self.created_at.isoformat(),
            'last_accessed': self.last_accessed.isoformat(),
            'metadata': json.dumps(self.metadata)
        }

    @classmethod
    def from_redis_hash(cls, data: dict) -> 'ChatSession':
        """Redis hash에서 역직렬화"""
        return cls(
            user_id=int(data.get('user_id', 0)),
            medication_info=json.loads(data.get('medication_info', '[]')),
            user_info=data.get('user_info', ""),
            created_at=datetime.fromisoformat(data.get('created_at', datetime.utcnow().isoformat())),
            last_accessed=datetime.fromisoformat(data.get('last_accessed', datetime.utcnow().isoformat())),
            metadata=json.loads(data.get('metadata', '{}'))
        )

class MedicationItem(BaseModel):
    medication_id: int

class UserMedicationsRequest(BaseModel):
    user_id: int
    medications: list[MedicationItem]

    class Config:
        from_attributes = True

class ChatRequest(BaseModel):
    user_id: int
    message: str = None
    user_info: str = None
    medication_info: list[str] = None
    
    class Config:
        min_length_message = 1
        max_length_message = 1000