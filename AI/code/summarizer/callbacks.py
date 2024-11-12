from langchain_core.callbacks.base import AsyncCallbackHandler
from typing import Any, Dict, Optional
from datetime import datetime

class AsyncRunCollector(AsyncCallbackHandler):
    def __init__(self):
        self.runs = {}
        self.start_times = {}
    
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
        self.start_times[run_id] = datetime.now()
        self.runs[run_id] = {
            'status': 'started',
            'parent_run_id': parent_run_id,
            'inputs': inputs
        }

    async def on_chain_end(
        self,
        outputs: Dict[str, Any],
        *,
        run_id: str,
        **kwargs: Any,
    ) -> None:
        """체인 종료 시 호출"""
        duration = datetime.now() - self.start_times[run_id]
        self.runs[run_id].update({
            'status': 'completed',
            'outputs': outputs,
            'duration': duration.total_seconds()
        })

    async def on_chain_error(
        self,
        error: Exception,
        *,
        run_id: str,
        parent_run_id: Optional[str] = None,
        **kwargs: Any,
    ) -> None:
        """체인 에러 발생 시 호출"""
        self.runs[run_id].update({
            'status': 'failed',
            'error': str(error),
            'duration': datetime.now() - self.start_times[run_id]
        })