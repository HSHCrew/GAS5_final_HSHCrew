from pydantic_settings import BaseSettings
from urllib.parse import quote_plus
import aiomysql
import asyncio
from dotenv import load_dotenv
import os

load_dotenv()

class DatabaseSettings(BaseSettings):
    DB_HOST: str = os.getenv('DB_HOST')
    DB_PORT: int = int(os.getenv('DB_PORT', 3306))
    DB_USER: str = os.getenv('DB_USER')
    DB_PASSWORD: str = os.getenv('DB_PASSWORD')
    DB_NAME: str = os.getenv('DB_NAME')
    
    @property
    def DATABASE_URL(self) -> str:
        encoded_password = quote_plus(self.DB_PASSWORD)
        return f"mysql+aiomysql://{self.DB_USER}:{encoded_password}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"
    
    class Config:
        env_file = ".env"

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