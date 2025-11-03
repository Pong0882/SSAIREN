"""
통합 오디오 처리 API

오디오 파일 업로드 → STT 변환 → LLM 정제를 한 번에 처리합니다.
"""

import os
import tempfile
from fastapi import APIRouter, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse

from services.stt_service import transcribe_audio_stream
from services.llm_service import refine_stt_result
from schemas.llm_schemas import STTSegment

router = APIRouter(prefix="/integrated", tags=["integrated"])


@router.post("/process-audio")
async def process_audio_complete(
    file: UploadFile = File(...),
    language: str = "ko",
    context: str = "119 응급 상황"
):
    """
    오디오 파일을 업로드하면 STT → LLM 정제까지 자동으로 처리
    
    전체 프로세스:
    1. 오디오 파일 → STT (Whisper API, 화자 분리)
    2. STT 결과 → LLM 정제 (GPT-4.1, 텍스트 교정 + 정보 추출)
    3. 정제된 JSON 반환
    
    Args:
        file: 오디오 파일 (wav, mp3, m4a 등)
        language: 언어 코드 (기본값: 'ko')
        context: 상황 맥락 (기본값: '119 응급 상황')
    
    Returns:
        dict: {
            "stt_result": {
                "text": "전체 텍스트",
                "segments": [...],
                "speaker_count": 4
            },
            "llm_result": {
                "success": true,
                "original_text": "...",
                "corrected_text": "...",
                "structured_data": {...},
                "model_used": "gpt-4.1-2025-04-14",
                "processing_time": 5.2
            },
            "total_processing_time": 8.5
        }
    
    Example:
        POST /api/integrated/process-audio
        (form-data)
        file: audio.wav
        
        Response:
        {
            "stt_result": {
                "text": "안녕하세요. 어디가 불편하신가요? 머리가 아파요.",
                "segments": [
                    {"id": "seg_0", "speaker": "A", ...},
                    {"id": "seg_1", "speaker": "B", ...}
                ],
                "speaker_count": 2
            },
            "llm_result": {
                "success": true,
                "corrected_text": "[화자 A]: 안녕하세요...",
                "structured_data": {
                    "patientInfo": {...}
                },
                "processing_time": 3.2
            },
            "total_processing_time": 6.5
        }
    """
    import time
    start_time = time.time()
    
    # 임시 파일로 저장
    suffix = os.path.splitext(file.filename or "audio.mp3")[1]
    tmp_path = None
    
    try:
        # 1단계: 오디오 파일 저장
        with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as tmp:
            tmp.write(await file.read())
            tmp_path = tmp.name
        
        print(f"[통합 API] 오디오 파일 저장 완료: {tmp_path}")
        
        # 2단계: STT 실행 (스트리밍 결과를 모두 수집)
        print(f"[통합 API] STT 변환 시작...")
        stt_segments = []
        full_text_parts = []
        
        for event in transcribe_audio_stream(tmp_path, language):
            # 이벤트에서 세그먼트 정보 추출
            if hasattr(event, 'model_dump'):
                event_data = event.model_dump()
            elif hasattr(event, 'dict'):
                event_data = event.dict()
            else:
                continue
            
            # 세그먼트 정보가 있으면 저장
            if 'segments' in event_data:
                for seg in event_data['segments']:
                    stt_segments.append({
                        "id": seg.get('id', f"seg_{len(stt_segments)}"),
                        "speaker": seg.get('speaker', 'Unknown'),
                        "start": seg.get('start', 0.0),
                        "end": seg.get('end', 0.0),
                        "text": seg.get('text', '')
                    })
            
            # 텍스트 수집
            if 'text' in event_data:
                full_text_parts.append(event_data['text'])
        
        # 전체 텍스트 조합
        full_text = ' '.join(full_text_parts) if full_text_parts else ''
        
        # 세그먼트가 없으면 전체 텍스트로 하나의 세그먼트 생성
        if not stt_segments and full_text:
            stt_segments = [{
                "id": "seg_0",
                "speaker": "A",
                "start": 0.0,
                "end": 0.0,
                "text": full_text
            }]
        
        print(f"[통합 API] STT 완료: {len(stt_segments)}개 세그먼트, {len(full_text)}자")
        
        # STT 결과가 비어있으면 에러
        if not full_text:
            raise HTTPException(
                status_code=400,
                detail="STT 변환 결과가 비어있습니다. 오디오 파일을 확인해주세요."
            )
        
        # 화자 수 계산
        speaker_count = len(set(seg['speaker'] for seg in stt_segments))
        
        stt_result = {
            "text": full_text,
            "segments": stt_segments,
            "speaker_count": speaker_count
        }
        
        # 3단계: LLM 정제 실행
        print(f"[통합 API] LLM 정제 시작...")
        
        # STTSegment 객체로 변환
        segment_objects = [
            STTSegment(
                id=seg['id'],
                speaker=seg['speaker'],
                start=seg['start'],
                end=seg['end'],
                text=seg['text']
            )
            for seg in stt_segments
        ]
        
        llm_result = refine_stt_result(
            transcription=full_text,
            segments=segment_objects,
            context=context
        )
        
        print(f"[통합 API] LLM 정제 완료")
        
        # 전체 처리 시간 계산
        total_time = time.time() - start_time
        
        # 최종 응답
        response = {
            "stt_result": stt_result,
            "llm_result": llm_result,
            "total_processing_time": round(total_time, 2)
        }
        
        return JSONResponse(content=response)
        
    except HTTPException:
        raise
    except Exception as e:
        print(f"[통합 API] 오류 발생: {str(e)}")
        import traceback
        traceback.print_exc()
        
        raise HTTPException(
            status_code=500,
            detail=f"오디오 처리 중 오류 발생: {str(e)}"
        )
    finally:
        # 임시 파일 삭제
        if tmp_path and os.path.exists(tmp_path):
            try:
                os.remove(tmp_path)
                print(f"[통합 API] 임시 파일 삭제 완료: {tmp_path}")
            except Exception as e:
                print(f"[통합 API] 임시 파일 삭제 실패: {str(e)}")


@router.get("/health")
async def integrated_health_check():
    """
    통합 API 서비스 상태 확인
    
    Returns:
        dict: 서비스 상태 정보
    """
    import os
    
    # OpenAI API 키 확인
    api_key_configured = bool(os.getenv("OPENAI_API_KEY"))
    
    return {
        "status": "healthy" if api_key_configured else "warning",
        "service": "Integrated Audio Processing Service",
        "features": [
            "STT (Whisper API, 화자 분리)",
            "LLM (GPT-4.1, 텍스트 정제 + 정보 추출)"
        ],
        "api_key_configured": api_key_configured,
        "message": "통합 서비스가 정상 작동 중입니다." if api_key_configured 
                   else "OpenAI API 키가 설정되지 않았습니다."
    }

