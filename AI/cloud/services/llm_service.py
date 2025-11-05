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
입력: "[화자 B]: 머리가 두동이 이이나요?"
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

**추출 카테고리** (8개):
1. **patientInfo**: 환자 기본 정보 (이름, 성별, 나이, 연락처, 보호자 등)
2. **dispatch**: 구급 출동 정보 (시간, 장소, 증상 등)
3. **incidentType**: 환자 발생 유형 (병력, 사고 유형 등)
4. **patientAssessment**: 환자 평가 (의식, 동공, 활력징후 등)
5. **emergencyTreatment**: 응급처치 (기도확보, 산소공급, CPR, AED 등)
6. **medicalGuidance**: 의료지도 (연결 여부, 요청 방법, 지시 내용 등)
7. **patientTransport**: 환자 이송 (이송 병원, 재이송 사유, 인수자 등)
8. **detailReport**: 세부상황표 (의사/대원 정보, 장애 요인 등)

**주요 선택지 매핑** (대화 내용을 아래 선택지로 매핑):

[신고 방법]: 일반전화 | 휴대전화 | 기타(value 필요)

[출동 유형]: 정상 | 오인 | 거짓 | 취소 | 기타

[환자 발생 장소]: 집 | 집단거주시설 | 도로 | 도로외교통지역 | 오락/문화/공시설 | 
학교/교육시설 | 운동시설 | 상업시설 | 의료관련시설 | 공장/산업/건설시설 | 
일차산업장 | 바다/강/산/논밭 | 기타(value 필요)

[증상 - 통증]: 두통 | 흉통 | 복통 | 요통 | 분만진통 | 그 밖의 통증(value에 상세 내용)

[증상 - 외상]: 골절 | 탈구 | 삠 | 열상 | 찰과상 | 타박상 | 절단 | 압궤손상 | 화상

[증상 - 기타]: 의식장애 | 기도이물 | 기침 | 호흡곤란 | 호흡정지 | 두근거림 | 
가슴불편감 | 심정지 | 경련/발작 | 실신 | 오심 | 구토 | 설사 | 변비 | 
배뇨장애 | 객혈 | 토혈 | 혈변 | 비출혈 | 질출혈 | 그 밖의 출혈 | 고열 | 
저체온증 | 어지러움 | 마비 | 전신쇠약 | 정신장애 | 그 밖의 이물감 | 기타(value에 상세 내용)

[병력]: 고혈압 | 당뇨 | 뇌혈관질환 | 심장질환 | 폐질환 | 결핵 | 간염 | 간경화 | 
알레르기 | 암(value 필수) | 신부전(value="예/아니오") | 
감염병(value 필수) | 기타(value 필수)

[환자 발생 분류]: 질병 | 질병외 | 기타

[교통사고 세부]: 운전자 | 동승자 | 보행자 | 자전거 | 오토바이 | 개인형 이동장치 | 
그 밖의 탈 것(value 필수) | 미상

[그 외 손상 세부]: 낙상 | 추락 | 그 밖의 둔상 | 관통상 | 기계 | 농기계

[비외상성 손상 세부]: 동물/곤충(value 필수) | 익사 | 중독 | 화상 | 
감전 | 질식 | 이물 | 저체온증 | 고체온증

[기타 세부]: 자연재해 | 임산부 | 신생아 | 단순주취 | 기타(value 필수)

[범죄의심]: 경찰통보 | 경찰인계 | 긴급이송 | 관련기관 통보

[기도 확보]: 도수조작 | 기도유지기 | 기관삽관 | 성문외기도유지기 | 흡인기 | 기도폐쇄처치

[산소 공급 장비]: 비관 | 안면마스크 | 비재호흡마스크 | BVM | 산소소생기 | 네뷸라이저 | 기타

[심폐소생술]: 실시 | 거부 | DNR | 유보

[AED 사용]: shock | monitoring | 기타(value 필수)

[순환보조]: 정맥로 확보 | 수액공급 확보(value에 cc 입력 필수)

[고정]: 목뼈 | 척추 | 부목 | 머리

[상처처치]: 지혈 | 상처 소독 처리

[보온]: 온 | 냉

[의료지도 연결 여부]: 연결 | 미연결

[의료지도 요청 방법(type)]: 일반전화 | 휴대전화 | 무전기 | 기타(value 필수)

[휴대전화 세부]: 음성 | 화상 (휴대전화 선택 시 value에 입력)

[의료지도 기관]: 소방 | 병원 | 기타(value 필수)

[이송 구역]: 관할 | 타시·도

[의료기관 선정자]: 구급대 | 119상황실 | 구급상황센터 | 환자보호자 | 병원수용곤란등 | 기타(value 필수)

[재이송 사유]: 병상부족(name에 세부 리스트) | 전문의부재 | 검사불가 | 원내 CPR | 기타(value 필수)

[환자 인수자]: 의사 | 간호사 | 응급구조사 | 기타(value 필수)

[구급대원 등급]: 1급 | 2급 | 간호사 | 구급교육 | 기타(value 필수)

[계급]: 사 | 교 | 장 | 위 | 경 | 령 | 정 |

[장애 요인]: 없음 | 장거리 이송 | 보호자 요구 | 원거리 병원 | 원거리 출동 | 만취자 | 폭행 | 언어폭력 | 환자 과체중 | 기관협조 미흡 | 환자위치 불명확 | 교통정체 | 폭우 | 폭설 | 기타(value 필요)

**추출 예시 1** (기본 선택지 사용):

입력:
[화자 B]: 어디가 아프세요?
[화자 C]: 머리가 아파요.
[화자 B]: 고혈압 있으세요?
[화자 C]: 네, 있습니다.

출력:
{{"dispatch": {{"schema_version": 1, "symptoms": {{"pain": [{{"name": "두통"}}]}}}}, "incidentType": {{"schema_version": 1, "medicalHistory": {{"status": "있음", "items": [{{"name": "고혈압"}}]}}}}}}

**추출 예시 2** (value 사용 예시):

입력:
[화자 B]: 어디가 불편하세요?
[화자 C]: 손가락 끝이 찢어졌어요.
[화자 B]: 암 병력 있으세요?
[화자 C]: 네, 폐암입니다.

출력:
{{"dispatch": {{"schema_version": 1, "symptoms": {{"trauma": [{{"name": "열상"}}]}}}}, "incidentType": {{"schema_version": 1, "medicalHistory": {{"status": "있음", "items": [{{"name": "암", "value": "폐암"}}]}}}}}}

**추출 예시 3** (교통사고):

입력:
[화자 B]: 어떤 사고였나요?
[화자 C]: 차를 운전하다가 추돌 당했어요.
[화자 B]: 고혈압 있으시죠?
[화자 C]: 네, 있습니다.

출력:
{{"incidentType": {{"schema_version": 1, "medicalHistory": {{"status": "있음", "items": [{{"name": "고혈압"}}]}}, "category": "질병외", "subCategory_traffic": {{"type": "교통사고", "name": "운전자"}}}}}}

**추출 예시 4** (응급처치 - value 사용):

입력:
[화자 B]: 기도 확보하겠습니다. 산소 10리터 투여할게요.
[화자 B]: AED 쇼크 실시했습니다.
[화자 B]: 수액 200cc 투여 완료.
[화자 C]: (환자 의식 회복)

출력:
{{"emergencyTreatment": {{"schema_version": 1, "airwayManagement": {{"methods": ["기도유지"]}}, "oxygenTherapy": {{"flowRateLpm": 10, "device": "비재호흡마스크"}}, "aed": {{"type": "shock"}}, "circulation": {{"type": "수액공급 확보", "value": "200"}}}}}}

**추출 예시 5** (의료지도):

입력:
[화자 B]: 의료지도 연결했습니다. 병원 이의사 선생님이세요.
[화자 B]: 일반전화로 요청했고 기관삽관 지시 받았습니다.
[화자 B]: 드레싱도 하라고 하네요.
[화자 B]: 활성탄 투여하고 병원 선정 진행합니다.

출력:
{{"medicalGuidance": {{"schema_version": 1, "contactStatus": "연결", "guidanceAgency": {{"type": "병원"}}, "guidanceDoctor": {{"name": "이의사"}}, "guidanceContent": {{"emergencyTreatment": [{{"name": "기관삽관"}}, {{"name": "기타", "value": "드레싱"}}], "medication": [{{"name": "기타", "value": "활성탄"}}], "hospitalRequest": true}}}}}}

**추출 예시 6** (환자 이송):

입력:
[화자 B]: 첫 번째로 OO병원 갔는데 응급실, 중환자실 병상이 없다고 재이송 요청했습니다.
[화자 B]: 상황실에서 선정했고 의사 선생님께 인계했어요.
[화자 B]: 이후 △△병원으로 다시 가서 간호사에게 인계하고 서명 받았습니다.

출력:
{{"patientTransport": {{"schema_version": 1, "firstTransport": {{"hospitalName": "OO병원", "regionType": "관할", "retransportReason": [{{"type": "병상부족", "name": ["응급실", "중환자실"]}}], "selectedBy": "119상황실", "receiver": "의사"}}, "secondTransport": {{"hospitalName": "△△병원", "regionType": "타시·도", "receiver": "간호사"}}}}}}

**추출 예시 7** (세부상황표):

입력:
[화자 B]: 의사 홍길동 선생님께 확인 받았습니다.
[화자 B]: 1급 소방교 김철수, 2급 소방사 박영희가 탑승했고 운전은 구급교육 소방교 이운전이 맡았습니다.
[화자 B]: 기타로 최지원 대원이 함께 했고 장애요인은 보호자 요구였습니다.

출력:
{{"detailReport": {{"schema_version": 1, "doctor": {{"affiliation": "소방", "name": "홍길동"}}, "paramedic1": {{"grade": "1급", "rank": "교", "name": "김철수"}}, "paramedic2": {{"grade": "2급", "rank": "사", "name": "박영희"}}, "driver": {{"grade": "구급교육", "rank": "소방교", "name": "이운전"}}, "other": {{"grade": "기타", "name": "최지원"}}, "obstacles": {{"type": "보호자 요구"}}}}}}

**중요 규칙**:
1. 반드시 순수 JSON만 출력 (코드 블록이나 설명 금지)
2. 언급된 정보만 포함 (추측 금지)
3. 시간 형식: HH:MM, 날짜: ISO-8601
4. **선택지 매핑**: 위의 [선택지] 목록에서 가장 가까운 항목 선택
5. **value 사용법**:
   - 기본 선택지에 있으면: name만 기입, value 생략
   - "기타" 선택 시: name="기타", value에 상세 내용 필수
   - 특정 항목(암/감염병/신부전/동물·곤충/그 밖의 탈 것): value 필수
   - 의료지도 요청 방법이 "휴대전화"이고 음성/화상이 언급되면 value에 해당 값을 입력
   - 수액공급 확보: value에 cc 입력 필수
   - 환자 이송 재이송 사유가 "병상부족"이면 name 목록에 세부 병상을 배열로 입력, 기타 유형이면 value에 상세 내용 기재
   - 환자 이송에서 선정자/인수자가 "기타"이면 각각 selectedByValue, receiverValue에 상세 내용 기재
6. **incidentType 규칙**:
   - category가 "질병"이면 subCategory 없음
   - category가 "질병외"이면 type에 따라:
     * "교통사고" → subCategory_traffic 사용
     * "그 외 손상" → subCategory_injury 사용
     * "비외상성 손상" → subCategory_nonTrauma 사용
   - category가 "기타"이면 category_other="기타", subCategory_other 사용
7. **emergencyTreatment 규칙**:
   - airwayManagement.methods는 배열 (복수 선택 가능)
   - circulation.type이 "수액공급 확보"일 때: value에 cc 입력 필수
   - circulation.type이 "정맥로 확보"일 때: value 생략
   - ecg는 boolean (true/false)
   - 시간은 HH:MM 형식 (deliverytime)
8. **"기타" 및 추가 정보 필요 항목**:
   - 신고 방법="기타" → reporter.value 필수
   - 환자 발생 장소="기타" → sceneLocation.value 필수
   - AED 사용="기타" → aed.value 필수
   - 의료지도 요청 방법="기타" → requestMethod.value 필수
   - 의료지도 기관="기타" → guidanceAgency.value 필수
   - 의료지도 emergencyTreatment/medication 항목이 "기타"이면 value에 상세 내용 필수
   - 의료기관 선정자="기타" → selectedByValue 필수
   - 환자 인수자="기타" → receiverValue 필수
   - 재이송 사유 type="기타" → value 필수
   - 장애 요인="기타" → obstacles.value 필수
9. **서명 데이터**: receiverSign.data 및 detailReport.*.signature는 Base64 문자열만 허용 (설명 텍스트 금지)

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



