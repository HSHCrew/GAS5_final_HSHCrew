from langchain_core.callbacks.base import AsyncCallbackHandler
from typing import Any, Dict, Optional

class AsyncRunCollector(AsyncCallbackHandler):
    """비동기 실행을 위한 콜백 핸들러"""
    
    async def on_chain_start(
        self, 
        serialized: Dict[str, Any],
        inputs: Dict[str, Any],
        *,
        run_id: str,
        parent_run_id: Optional[str] = None,
        **kwargs: Any,
    ) -> None:
        """체인 시작 시 호출"""
        pass

    async def on_chain_end(
        self,
        outputs: Dict[str, Any],
        *,
        run_id: str,
        parent_run_id: Optional[str] = None,
        **kwargs: Any,
    ) -> None:
        """체인 종료 시 호출"""
        pass

    async def on_chain_error(
        self,
        error: Exception,
        *,
        run_id: str,
        parent_run_id: Optional[str] = None,
        **kwargs: Any,
    ) -> None:
        """체인 에러 발생 시 호출"""
        pass 