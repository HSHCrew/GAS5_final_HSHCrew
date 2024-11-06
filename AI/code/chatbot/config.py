from pydantic_settings import BaseSettings
from datetime import timedelta
from typing import Dict, Any

class ChatbotSettings(BaseSettings):
    MODEL_NAME: str = "gpt-4o"
    TEMPERATURE: float = 0.0
    MESSAGE_TTL: int = 7  # days
    SESSION_TTL: int = 1  # hours
    MAX_HISTORY_LENGTH: int = 100
    REDIS_PREFIX: str = "chatbot"
    MAX_MESSAGE_LENGTH: int = 4096
    CLEANUP_INTERVAL: int = 3600  # seconds
    MAX_INSTANCES: int = 1000
    
    @property
    def session_ttl_seconds(self) -> int:
        return self.SESSION_TTL * 3600
    
    @property
    def message_ttl_seconds(self) -> int:
        return self.MESSAGE_TTL * 86400
    
    @property
    def cleanup_interval(self) -> int:
        return self.CLEANUP_INTERVAL
    
    @property
    def max_instances(self) -> int:
        return self.MAX_INSTANCES
    
    class Config:
        env_file = ".env"
        env_prefix = "CHATBOT_"  # 환경변수 prefix 추가