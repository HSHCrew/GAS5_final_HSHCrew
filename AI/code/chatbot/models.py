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

class ChatSession:
    def __init__(
        self,
        user_id: int,
        medication_info: List[str],
        user_info: str = "",
        created_at: Optional[datetime] = None,
        last_accessed: Optional[datetime] = None,
        metadata: Dict = None
    ):
        self.user_id = user_id
        self.medication_info = medication_info
        self.user_info = user_info
        self.created_at = created_at or datetime.utcnow()
        self.last_accessed = last_accessed or datetime.utcnow()
        self.metadata = metadata or {}

    def to_redis_hash(self) -> Dict[str, str]:
        """세션 데이터를 Redis hash로 변환"""
        return {
            "user_id": str(self.user_id),
            "medication_info": json.dumps(self.medication_info),
            "user_info": self.user_info,
            "created_at": self.created_at.isoformat(),
            "last_accessed": self.last_accessed.isoformat(),
            "metadata": json.dumps(self.metadata)
        }

    @classmethod
    def from_redis_hash(cls, data: Dict[bytes, bytes]) -> "ChatSession":
        """Redis hash에서 세션 데이터 복원"""
        try:
            # bytes를 문자열로 디코딩
            decoded_data = {k.decode('utf-8'): v.decode('utf-8') for k, v in data.items()}
            
            # medication_info JSON 파싱
            medication_info = json.loads(decoded_data.get('medication_info', '[]'))
            if isinstance(medication_info, str):  # 문자열로 저장된 경우 다시 파싱
                medication_info = json.loads(medication_info)
            
            return cls(
                user_id=int(decoded_data.get('user_id', 0)),
                medication_info=medication_info,
                user_info=decoded_data.get('user_info', ''),
                created_at=datetime.fromisoformat(decoded_data.get('created_at', datetime.utcnow().isoformat())),
                last_accessed=datetime.fromisoformat(decoded_data.get('last_accessed', datetime.utcnow().isoformat())),
                metadata=json.loads(decoded_data.get('metadata', '{}'))
            )
        except Exception as e:
            print(f"[ERROR] Error parsing Redis data: {str(e)}")
            print(f"Raw data: {data}")
            raise

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

class IntentClassification(BaseModel):
    intent: Literal["medical", "harmful", "irrelevant", "clarification"]
    confidence: float = Field(ge=0, le=1)
    explanation: str