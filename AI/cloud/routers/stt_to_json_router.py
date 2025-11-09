"""
STT → JSON 추출 통합 라우터
음성 파일 → STT → 파인튜닝된 LLM → JSON

전체 플로우를 한 번에 처리
"""
from fastapi import APIRouter, UploadFile, File, HTTPException
from pydantic import BaseModel, Field
from typing import Dict, Any
from services.stt_service import transcribe_audio_stream
from services.lora_llm_service import get_lora_llm_service
import tempfile
import os

router = APIRouter(prefix="/stt-to-json", tags=["STT to JSON"])

class ConversationToJsonRequest(BaseModel):
    """대화 텍스트 → JSON 요청"""
    conversation: str = Field(..., description="대화 텍스트 (STT 결과 또는 직접 입력)")
    max_new_tokens: int = Field(700, ge=400, le=1500, description="최대 생성 토큰")
    temperature: float = Field(0.1, ge=0.0, le=1.0, description="샘플링 온도")

class SttToJsonResponse(BaseModel):
    """STT → JSON 응답"""
    success: bool
    stt_text: str = Field(..., description="STT로 변환된 텍스트")
    extracted_json: Dict[str, Any] = Field(..., description="추출된 EMS JSON")
    raw_output: str = Field(..., description="LLM 원본 출력")

@router.post("/audio-to-json", response_model=SttToJsonResponse)
async def audio_to_json(audio_file: UploadFile = File(...)):
    """
    음성 파일 → STT → JSON 추출 (전체 플로우)
    
    ## 프로세스
    1. 음성 파일 업로드
    2. Whisper STT로 텍스트 변환
    3. 파인튜닝된 Qwen 모델로 JSON 추출
    4. 결과 반환
    
    ## 지원 형식
    - wav, mp3, m4a, webm 등
    """
    try:
        # 1. 임시 파일로 저장
        with tempfile.NamedTemporaryFile(delete=False, suffix=".wav") as tmp_file:
            content = await audio_file.read()
            tmp_file.write(content)
            tmp_path = tmp_file.name
        
        try:
            # 2. STT 처리
            print(f"🎤 STT 처리 시작: {audio_file.filename}")
            stt_result = []
            for chunk in transcribe_audio_stream(tmp_path, language="ko"):
                stt_result.append(chunk)
            
            # STT 결과 합치기
            stt_text = "".join(stt_result)
            print(f"✅ STT 완료: {len(stt_text)}자")
            
            # 3. LLM으로 JSON 추출
            print(f"🤖 JSON 추출 시작")
            llm_service = get_lora_llm_service()
            result = llm_service.extract_json_from_conversation(
                conversation=stt_text,
                max_new_tokens=700,
                temperature=0.1
            )
            
            if result["success"]:
                print(f"✅ JSON 추출 완료")
                return SttToJsonResponse(
                    success=True,
                    stt_text=stt_text,
                    extracted_json=result["json"],
                    raw_output=result["raw_text"]
                )
            else:
                print(f"⚠️ JSON 파싱 실패: {result['error']}")
                raise HTTPException(
                    status_code=500,
                    detail=f"JSON 파싱 실패: {result['error']}"
                )
        
        finally:
            # 임시 파일 삭제
            if os.path.exists(tmp_path):
                os.unlink(tmp_path)
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"처리 실패: {str(e)}")

@router.post("/text-to-json")
async def text_to_json(request: ConversationToJsonRequest):
    """
    대화 텍스트 → JSON 추출 (STT 결과 직접 입력)
    
    ## 용도
    - STT가 이미 완료된 경우
    - 테스트용 대화 직접 입력
    """
    try:
        print(f"🤖 JSON 추출 시작 (대화 길이: {len(request.conversation)}자)")
        
        llm_service = get_lora_llm_service()
        result = llm_service.extract_json_from_conversation(
            conversation=request.conversation,
            max_new_tokens=request.max_new_tokens,
            temperature=request.temperature
        )
        
        if result["success"]:
            print(f"✅ JSON 추출 완료")
            return {
                "success": True,
                "extracted_json": result["json"],
                "raw_output": result["raw_text"]
            }
        else:
            raise HTTPException(
                status_code=500,
                detail=f"JSON 파싱 실패: {result['error']}"
            )
    
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"처리 실패: {str(e)}")

@router.post("/reload-model")
async def reload_model(adapter_path: str):
    """
    새로운 LoRA 모델로 교체
    
    ## 사용 시점
    - 파인튜닝 완료 후 새 모델 적용
    - 모델 버전 업그레이드
    
    ## 예시
    ```
    POST /stt-to-json/reload-model?adapter_path=./models/ems-lora-checkpoint-v2
    ```
    """
    try:
        llm_service = get_lora_llm_service()
        llm_service.reload_model(adapter_path)
        return {
            "success": True,
            "message": f"모델 재로드 완료: {adapter_path}"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"모델 재로드 실패: {str(e)}")

@router.get("/health")
async def health_check():
    """헬스 체크"""
    try:
        llm_service = get_lora_llm_service()
        return {
            "status": "healthy",
            "model_loaded": llm_service._model is not None,
            "device": llm_service._device
        }
    except Exception as e:
        raise HTTPException(status_code=503, detail=f"서비스 불가: {str(e)}")

