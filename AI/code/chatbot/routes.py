from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.ext.asyncio import AsyncSession
from .database import get_db
from .models import ChatRequest
from .chatbot_manager import ChatbotManager

router = APIRouter()

@router.post('/user/medications/chat/reset')
async def chat_reset(
    request: ChatRequest,
    db: AsyncSession = Depends(get_db)
):
    try:
        # Reset chat using ChatbotManager
        new_chatbot = await ChatbotManager.reset_chat(request.user_id, db)
        
        initial_response = await new_chatbot.start_chat()
        
        return {
            "message": "Chat session reset successfully",
            "initial_response": initial_response
        }
    except Exception as e:
        print(f"Error resetting chat: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e)) 