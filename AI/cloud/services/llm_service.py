"""
LLM 기반 STT 결과물 정제 서비스

GPT-4.1 모델을 사용하여:
1. 화자별 세그먼트를 분석하여 환자/응급구조사 구분
2. 맥락 기반으로 뭉개진 단어 복구
3. STT 결과물을 구조화된 JSON으로 변환 (실제 정보만 포함)

OpenAI Structured Outputs를 활용하여 JSON 형식을 보장합니다.
"""

import os
import json
import time
from typing import Dict, Any, Optional, List
from openai import OpenAI

from schemas.llm_schemas import LLMRefinedResult, STTSegment


def get_openai_client():
    """OpenAI 클라이언트 인스턴스 반환 (lazy initialization)"""
    return OpenAI(api_key=os.getenv("OPENAI_API_KEY"))


# ============================================================
# 화자 분리 및 그룹핑
# ============================================================

def parse_segments_by_speaker(segments: List[STTSegment]) -> Dict[str, List[str]]:
    """
    화자별로 세그먼트를 그룹핑합니다.
    
    Args:
        segments: STT 세그먼트 리스트
        
    Returns:
        Dict[str, List[str]]: 화자별 발화 내용 {'A': ['텍스트1', '텍스트2'], 'B': [...]}
    """
    speaker_texts = {}
    for segment in segments:
        speaker = segment.speaker
        text = segment.text.strip()
        
        if speaker not in speaker_texts:
            speaker_texts[speaker] = []
        
        if text:  # 빈 텍스트 제외
            speaker_texts[speaker].append(text)
    
    return speaker_texts


def format_conversation_for_llm(segments: List[STTSegment]) -> str:
    """
    세그먼트를 LLM이 이해하기 쉬운 대화 형식으로 변환합니다.
    
    Args:
        segments: STT 세그먼트 리스트
        
    Returns:
        str: 포맷팅된 대화 텍스트
    """
    conversation_lines = []
    
    for segment in segments:
        speaker = segment.speaker
        text = segment.text.strip()
        
        if text:
            conversation_lines.append(f"[화자 {speaker}]: {text}")
    
    return "\n".join(conversation_lines)


# ============================================================
# 프롬프트 템플릿 관리
# ============================================================

def get_text_correction_prompt() -> str:
    """
    1단계: 맥락 기반 텍스트 교정 프롬프트
    
    화자별 대화에서 잘못 인식된 단어를 의료/응급 상황 맥락에서 교정합니다.
    """
    return """당신은 응급 상황 음성 인식 전문가입니다.

**임무**: 119 응급 상황 대화에서 STT로 잘못 인식된 단어를 교정하세요.

**상황 이해**:
- 이 대화는 응급구조사와 환자 간의 대화입니다
- 여러 화자(A, B, C, D 등)가 등장할 수 있습니다
- 일반적으로 응급구조사가 질문하고 환자가 답변합니다

**교정 지침**:
1. 의료/응급 용어로 교정 (예: "두동" → "두통", "삽형급실" → "응급실")
2. 맥락상 어색한 표현 수정 (예: "아바요" → "아파요")
3. 명백한 오류만 교정, 과도한 수정 금지
4. 화자 구분 [화자 X] 형식은 유지

**교정 예시**:
입력: "[화자 B]: 머리가 두동이 있나요?"
출력: "[화자 B]: 머리가 두통이 있나요?"

**입력 대화**:
{conversation}

**교정된 대화만 출력하세요**:"""


def get_information_extraction_prompt() -> str:
    """
    2단계: 정보 추출 및 구조화 프롬프트
    
    화자별 대화에서 환자 정보를 추출하여 JSON으로 구조화합니다.
    """
    return """당신은 119 응급 의료 정보 구조화 전문가입니다.

**임무**: 응급구조사와 환자의 대화를 분석하여 환자 정보를 JSON으로 추출하세요.

**상황 이해**:
- 화자 중 일부는 응급구조사(질문하는 사람), 일부는 환자(증상을 호소하는 사람)
- 대화 내용에서 환자에 대한 정보만 추출

**핵심 원칙**:
1. **실제로 언급된 정보만 포함** - 추측 금지
2. 정보가 없으면 해당 필드 **생략** (null/빈 값 사용 금지)
3. 숫자는 정확한 값만 추출
4. 환자가 호소하는 증상, 응급구조사가 관찰/질문한 내용 모두 포함

**추출 카테고리**:
1. **patientInfo**: 환자 기본 정보 (이름, 성별, 나이, 연락처 등)


**추출 예시**:

입력:
[화자 B]: 환자분 이름이 어떻게 되시나요?
[화자 C]: 홍길동입니다. 남자 45세입니다.

출력 (순수 JSON만, 코드 블록 없이):
{{"patientInfo": {{"schema_version": 1, "patient": {{"name": "홍길동", "gender": "남성", "ageYears": 45}}}}}}

**중요**: 반드시 순수 JSON만 출력하세요. 코드 블록(```)이나 설명 텍스트를 포함하지 마세요.

**입력 대화**:
{corrected_conversation}

**출력 (순수 JSON만)**:"""


# ============================================================
# LLM 호출 함수
# ============================================================

def correct_conversation_text(
    conversation: str,
    model: str = "gpt-4.1-2025-04-14"
) -> str:
    """
    1단계: 화자별 대화의 텍스트 교정
    
    Args:
        conversation: 화자별로 포맷팅된 대화 텍스트
        model: 사용할 GPT 모델명
        
    Returns:
        str: 교정된 대화 텍스트
        
    Raises:
        Exception: OpenAI API 호출 실패 시
    """
    client = get_openai_client()
    
    prompt = get_text_correction_prompt().format(conversation=conversation)
    
    try:
        response = client.chat.completions.create(
            model=model,
            messages=[
                {
                    "role": "system",
                    "content": "당신은 119 응급 상황 대화의 음성 인식 텍스트를 교정하는 전문가입니다."
                },
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            temperature=0.3,  # 낮은 temperature로 일관성 있는 교정
            max_tokens=2000
        )
        
        corrected_conversation = response.choices[0].message.content.strip()
        return corrected_conversation
        
    except Exception as e:
        print(f"대화 교정 중 오류 발생: {str(e)}")
        raise


def extract_structured_information(
    corrected_conversation: str,
    model: str = "gpt-4.1-2025-04-14"
) -> Dict[str, Any]:
    """
    2단계: 교정된 대화에서 구조화된 정보 추출
    
    OpenAI의 JSON mode를 사용하여 유효한 JSON을 보장합니다.
    화자별 대화를 분석하여 환자 정보만 추출합니다.
    
    Args:
        corrected_conversation: 교정된 대화 텍스트
        model: 사용할 GPT 모델명
        
    Returns:
        Dict[str, Any]: 구조화된 환자 정보 (실제 정보만 포함)
        
    Raises:
        Exception: OpenAI API 호출 또는 JSON 파싱 실패 시
    """
    client = get_openai_client()
    
    prompt = get_information_extraction_prompt().format(corrected_conversation=corrected_conversation)
    
    try:
        response = client.chat.completions.create(
            model=model,
            messages=[
                {
                    "role": "system",
                    "content": "당신은 119 응급 의료 정보를 구조화하는 전문가입니다. 반드시 유효한 JSON만 출력하세요."
                },
                {
                    "role": "user",
                    "content": prompt
                }
            ],
            response_format={"type": "json_object"},  # JSON 모드 강제
            temperature=0.1,  # 매우 낮은 temperature로 정확한 추출
            max_tokens=3000
        )
        
        content = response.choices[0].message.content.strip()
        
        # 마크다운 코드 블록 제거 (```json ... ``` 또는 ``` ... ```)
        if content.startswith("```"):
            # 첫 줄 제거 (```json 또는 ```)
            lines = content.split('\n')
            if lines[0].startswith("```"):
                lines = lines[1:]
            # 마지막 줄 제거 (```)
            if lines and lines[-1].strip() == "```":
                lines = lines[:-1]
            content = '\n'.join(lines).strip()
        
        # JSON 파싱
        structured_data = json.loads(content)
        
        # 빈 객체나 null 필드 제거 (재귀적으로)
        cleaned_data = remove_empty_fields(structured_data)
        
        return cleaned_data
        
    except json.JSONDecodeError as e:
        print(f"JSON 파싱 실패: {str(e)}")
        print(f"응답 내용 (처리 후): {content}")
        raise Exception(f"LLM이 유효한 JSON을 반환하지 않았습니다: {str(e)}")
    except Exception as e:
        print(f"정보 추출 중 오류 발생: {str(e)}")
        raise


def remove_empty_fields(data: Any) -> Any:
    """
    재귀적으로 빈 필드, null, 빈 문자열, 빈 객체, 빈 배열 제거
    
    Args:
        data: 정제할 데이터 (dict, list, 또는 기타)
        
    Returns:
        Any: 정제된 데이터 (빈 값 제거됨)
    """
    if isinstance(data, dict):
        return {
            k: remove_empty_fields(v)
            for k, v in data.items()
            if v is not None 
            and v != "" 
            and v != {} 
            and v != []
            and remove_empty_fields(v) not in (None, "", {}, [])
        }
    elif isinstance(data, list):
        return [
            remove_empty_fields(item)
            for item in data
            if item is not None 
            and item != "" 
            and remove_empty_fields(item) not in (None, "", {}, [])
        ]
    else:
        return data


# ============================================================
# 통합 정제 함수 (메인 엔트리 포인트)
# ============================================================

def refine_stt_result(
    transcription: str,
    segments: List[STTSegment],
    model: str = "gpt-4.1-2025-04-14",
    context: Optional[str] = None
) -> Dict[str, Any]:
    """
    STT 결과물 통합 정제 프로세스 (화자 분리 지원)
    
    3단계 프로세스:
    1. 화자별 세그먼트를 대화 형식으로 변환
    2. 대화 텍스트 교정 (맥락 기반 단어 복구)
    3. 구조화된 정보 추출 (JSON 형식)
    
    Args:
        transcription: STT 전체 텍스트 (원본)
        segments: 화자별 세그먼트 리스트
        model: 사용할 GPT 모델명 (기본값: gpt-4.1-2025-04-14)
        context: 추가 맥락 정보 (선택사항)
        
    Returns:
        Dict[str, Any]: {
            "success": bool,
            "original_text": str,
            "corrected_text": str,
            "structured_data": dict,
            "model_used": str,
            "processing_time": float,
            "speaker_count": int
        }
        
    Raises:
        Exception: 정제 프로세스 중 오류 발생 시
    """
    start_time = time.time()
    
    try:
        # 1단계: 화자별 세그먼트를 대화 형식으로 변환
        print(f"[LLM 정제] 1단계: 화자별 대화 포맷팅...")
        conversation = format_conversation_for_llm(segments)
        
        # 화자 정보 분석
        speaker_groups = parse_segments_by_speaker(segments)
        speaker_count = len(speaker_groups)
        
        print(f"[LLM 정제] 화자 수: {speaker_count}명")
        for speaker, texts in speaker_groups.items():
            print(f"  - 화자 {speaker}: {len(texts)}개 발화")
        
        # 맥락 정보 추가
        if context:
            conversation = f"[상황 맥락: {context}]\n\n{conversation}"
        
        # 2단계: 대화 텍스트 교정
        print(f"[LLM 정제] 2단계: 대화 텍스트 교정 시작...")
        corrected_conversation = correct_conversation_text(conversation, model)
        print(f"[LLM 정제] 2단계 완료: {len(corrected_conversation)}자")
        
        # 3단계: 구조화된 정보 추출
        print(f"[LLM 정제] 3단계: 환자 정보 추출 시작...")
        structured_data = extract_structured_information(corrected_conversation, model)
        print(f"[LLM 정제] 3단계 완료: {len(structured_data)}개 카테고리 추출")
        
        # 처리 시간 계산
        processing_time = time.time() - start_time
        
        # 결과 반환
        return {
            "success": True,
            "original_text": transcription,
            "corrected_text": corrected_conversation,
            "structured_data": structured_data,
            "model_used": model,
            "processing_time": round(processing_time, 2),
            "speaker_count": speaker_count
        }
        
    except Exception as e:
        # 오류 발생 시에도 구조화된 응답 반환
        processing_time = time.time() - start_time
        print(f"[LLM 정제] 오류 발생: {str(e)}")
        
        return {
            "success": False,
            "original_text": transcription,
            "corrected_text": "",
            "structured_data": {},
            "model_used": model,
            "processing_time": round(processing_time, 2),
            "speaker_count": 0,
            "error": str(e)
        }



