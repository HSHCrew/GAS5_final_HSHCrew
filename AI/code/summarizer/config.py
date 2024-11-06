from pydantic_settings import BaseSettings

class SummarizerSettings(BaseSettings):
    MODEL_NAME: str = "gpt-4o-mini"
    TEMPERATURE: float = 0
    VERIFICATION_THRESHOLD: int = 80
    MAX_RETRY_ATTEMPTS: int = 2
    
    class Config:
        env_file = ".env" 