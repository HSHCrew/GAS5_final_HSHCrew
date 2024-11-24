from pydantic_settings import BaseSettings

class SummarizerSettings(BaseSettings):
    MODEL_NAME: str = "gpt-4o"
    TEMPERATURE: float = 0
    VERIFICATION_THRESHOLD: int = 80
    MAX_RETRY_ATTEMPTS: int = 2
    BATCH_SIZE: int = 10
    TIMEOUT_SECONDS: int = 30
    
    class Config:
        env_prefix = "SUMMARIZER_"
        env_file = ".env" 