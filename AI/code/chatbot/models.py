from pydantic import BaseModel, Field, validator
from datetime import datetime
from typing import Literal, Optional, List, Dict, Any

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
    medication_info: List[MedicationInfo]
    created_at: datetime = Field(default_factory=datetime.utcnow)
    last_accessed: datetime = Field(default_factory=datetime.utcnow)
    metadata: Dict[str, Any] = Field(default_factory=dict)