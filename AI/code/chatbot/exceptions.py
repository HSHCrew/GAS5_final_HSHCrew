class ChatbotError(Exception):
    """기본 챗봇 예외"""
    def __init__(self, message: str, code: str = None):
        super().__init__(message)
        self.code = code or self.__class__.__name__

class ChatbotSessionError(ChatbotError):
    """세션 관련 예외"""
    pass

class MessageError(ChatbotError):
    """메시지 처리 관련 예외"""
    pass

class ValidationError(ChatbotError):
    """데이터 검증 관련 예외"""
    pass

class StorageError(ChatbotError):
    """저장소 관련 예외"""
    pass

class ResourceError(ChatbotError):
    """리소스 관리 관련 예외"""
    pass 