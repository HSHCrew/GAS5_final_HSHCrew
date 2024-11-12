from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from .database import get_db
from .models import ChatRequest
from .manager import ChatbotManager

router = APIRouter(prefix="/user/medications")

def get_chatbot_manager():
    from main import chatbot_manager  # 순환 참조 방지를 위한 지연 임포트
    return chatbot_manager

@router.post("/chat_message")
async def chat_message(
    request: ChatRequest,
    db: AsyncSession = Depends(get_db),
    manager: ChatbotManager = Depends(get_chatbot_manager)
):
    try:
        chatbot = await manager.get_chatbot(request.user_id, db)
        response = await chatbot.respond(request.message)
        return {"response": response}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e)) 