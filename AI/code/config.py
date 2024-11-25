from pydantic_settings import BaseSettings
from urllib.parse import quote_plus
import os
from dotenv import load_dotenv

load_dotenv()

class DatabaseSettings(BaseSettings):
    DB_USER: str = os.getenv('DB_USER', 'root')
    DB_PASSWORD: str = os.getenv('DB_PASSWORD', 'dpdlem1590@!')
    DB_HOST: str = os.getenv('DB_HOST', '34.47.82.4')
    DB_PORT: int = int(os.getenv('DB_PORT', '3306'))
    DB_NAME: str = os.getenv('DB_NAME', 'medication_db')
    
    @property
    def DATABASE_URL(self) -> str:
        encoded_password = quote_plus(self.DB_PASSWORD)
        return f"mysql+aiomysql://{self.DB_USER}:{encoded_password}@{self.DB_HOST}:{self.DB_PORT}/{self.DB_NAME}"

    class Config:
        env_file = ".env"