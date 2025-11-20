"""
GGUF 포맷의 양자화된 모델을 사용한 LLM 서비스
STT 대본 → JSON 추출

llama-cpp-python 기반으로 Q5_K_M 양자화 지원
"""
import os
import json
from typing import Optional, Dict, Any
from llama_cpp import Llama

class LoRALLMService:
    """GGUF 양자화 모델 서비스 (Singleton)"""

    _instance: Optional['LoRALLMService'] = None
    _model = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    def __init__(self):
        """모델 초기화 (최초 1회만)"""
        if self._model is None:
            self._load_model()

    def _load_model(self):
        """GGUF 모델 로드 (Q5_K_M 양자화)"""
        # 환경 변수에서 경로 읽기
        model_path = os.getenv("GGUF_MODEL_PATH", "./models/gguf/qwen2.5-1.5b-instruct-q5_k_m.gguf")
        n_ctx = int(os.getenv("N_CTX", "1024"))  # 컨텍스트 길이 (6GB RAM 환경)
        n_threads = int(os.getenv("N_THREADS", "2"))  # CPU 스레드 수 (4코어 환경)
        n_gpu_layers = int(os.getenv("N_GPU_LAYERS", "0"))  # GPU 레이어 수 (0 = CPU only)

        print(f"🔄 GGUF 모델 로딩: {model_path}")
        print(f"⚙️  컨텍스트 길이: {n_ctx}")
        print(f"⚙️  CPU 스레드: {n_threads}")
        print(f"⚙️  GPU 레이어: {n_gpu_layers}")

        if not os.path.exists(model_path):
            raise FileNotFoundError(
                f"❌ GGUF 모델 파일을 찾을 수 없습니다: {model_path}\n"
                f"먼저 모델을 GGUF로 변환하고 Q5_K_M 양자화를 수행하세요.\n"
                f"변환 스크립트: python scripts/convert_to_gguf.py"
            )

        try:
            self._model = Llama(
                model_path=model_path,
                n_ctx=n_ctx,
                n_threads=n_threads,
                n_gpu_layers=n_gpu_layers,
                verbose=False  # 상세 로그 비활성화
            )

            print("✅ GGUF 모델 로딩 완료!")
            print(f"📊 모델 크기: Q5_K_M (약 2.5GB)")

        except Exception as e:
            print(f"❌ 모델 로딩 실패: {e}")
            raise

    def extract_json_from_conversation(
        self,
        conversation: str,
        max_tokens: int = 700,
        temperature: float = 0.1,
        top_p: float = 0.9
    ) -> Dict[str, Any]:
        """
        대화에서 JSON 추출

        Args:
            conversation: STT로 변환된 대화 텍스트
            max_tokens: 최대 생성 토큰 수
            temperature: 샘플링 온도 (낮을수록 결정적)
            top_p: nucleus sampling

        Returns:
            추출된 JSON (dict)
        """
        if self._model is None:
            raise RuntimeError("모델이 로드되지 않았습니다")

        # 프롬프트 구성 (llm_service.py의 상세 프롬프트 적용)
        prompt = f"""아래 STT로 변환된 응급대화를 읽고 GBNF 스키마에 맞는 JSON을 생성하세요.

**핵심 원칙**:
1. **실제로 언급된 정보만 포함** - 추측 금지
2. 정보가 없으면 해당 필드 **생략** (null/빈 값 사용 금지)
3. 숫자는 정확한 값만 추출
4. 환자가 호소하는 증상, 응급구조사가 관찰/질문한 내용 모두 포함

**주요 선택지 매핑**:

[신고 방법]: 일반전화 | 휴대전화 | 기타(value 필요)

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

[의식 상태]: A(명료) | V(음성에 반응) | P(통증에 반응) | U(무반응)

[기도 확보]: 도수조작 | 기도유지기 | 기관삽관 | 성문외기도유지기 | 흡인기 | 기도폐쇄처치

[산소 공급 장비]: 비관 | 안면마스크 | 비재호흡마스크 | BVM | 산소소생기 | 네뷸라이저 | 기타

[심폐소생술]: 실시 | 거부 | DNR | 유보

**추출 예시 1**:
입력:
안녕하십니까 응급구조사 이준호입니다 성함을 말씀해주세요 김민정입니다 어디가 아프세요 머리가 너무 아파요 고혈압 있으세요 네 있습니다

출력:
{{"patientInfo": {{"patient": {{"name": "김민정"}}}}, "dispatch": {{"symptoms": {{"pain": [{{"name": "두통"}}]}}}}, "incidentType": {{"medicalHistory": {{"status": "있음", "items": [{{"name": "고혈압"}}]}}, "category": "질병"}}}}

**추출 예시 2**:
입력:
응급구조사 최상인입니다 성함을 말씀해주세요 이수정예요 성별 나이 여쭤봐도 될까요 56살 남성이에요 발이 눌려서 시퍼래요 생체징후 혈압 109/64 맥박 57 호흡 22 체온 36.1 SpO2 96%입니다

출력:
{{"patientInfo": {{"patient": {{"name": "이수정", "gender": "남성", "ageYears": 56}}}}, "dispatch": {{"symptoms": {{"trauma": [{{"name": "압궤손상"}}]}}}}, "assessment": {{"vitalSigns": {{"first": {{"bloodPressure": "109/64", "pulse": 57, "respiration": 22, "temperature": 36.1, "spo2": 96}}}}}}}}

**중요 규칙**:
1. 반드시 순수 JSON만 출력 (코드 블록이나 설명 금지)
2. 언급된 정보만 포함 (추측 금지)
3. 시간 형식: HH:MM, 날짜: ISO-8601
4. **선택지 매핑**: 위의 [선택지] 목록에서 가장 가까운 항목 선택
5. **value 사용법**:
   - 기본 선택지에 있으면: name만 기입, value 생략
   - "기타" 선택 시: name="기타", value에 상세 내용 필수
   - 특정 항목(암/감염병/신부전): value 필수

**입력 대화**:
{conversation}

### 응답:
"""

        # 생성
        try:
            output = self._model(
                prompt,
                max_tokens=max_tokens,
                temperature=temperature,
                top_p=top_p,
                echo=False,  # 프롬프트 반복 안함
                stop=["###", "\n\n\n"]  # 정지 토큰
            )

            # 생성된 텍스트 추출
            generated_text = output['choices'][0]['text'].strip()
            
            # 디버깅: 원본 출력 로깅
            print(f"[DEBUG] 모델 원본 출력 길이: {len(generated_text)}자")
            print(f"[DEBUG] 모델 원본 출력 (처음 500자):\n{generated_text[:500]}")

            # 코드블록 제거 (``` 제거)
            if "```" in generated_text:
                # 첫 번째 ```와 마지막 ``` 사이의 내용 추출
                start = generated_text.find("```")
                end = generated_text.rfind("```")
                if start != -1 and end != -1 and start < end:
                    generated_text = generated_text[start + 3:end].strip()
                    # json이라는 언어 태그 제거
                    if generated_text.startswith("json"):
                        generated_text = generated_text[4:].strip()
                    print(f"[DEBUG] 코드블록 제거 후: {generated_text[:200]}")

            # JSON 파싱
            try:
                print(f"[DEBUG] JSON 파싱 시도: {generated_text[:200]}")
                parsed_json = json.loads(generated_text)
                print(f"[DEBUG] JSON 파싱 성공!")
                return {
                    "success": True,
                    "json": parsed_json,
                    "raw_text": generated_text
                }
            except json.JSONDecodeError as e:
                # JSON 파싱 실패 시 원문 반환
                print(f"[ERROR] JSON 파싱 실패: {str(e)}")
                print(f"[ERROR] 파싱 실패한 텍스트: {generated_text[:500]}")
                return {
                    "success": False,
                    "error": str(e),
                    "raw_text": generated_text
                }

        except Exception as e:
            return {
                "success": False,
                "error": f"모델 추론 실패: {str(e)}",
                "raw_text": ""
            }


# 전역 인스턴스
_lora_llm_service: Optional[LoRALLMService] = None

def get_lora_llm_service() -> LoRALLMService:
    """LoRA LLM 서비스 인스턴스 반환 (Singleton)"""
    global _lora_llm_service
    if _lora_llm_service is None:
        _lora_llm_service = LoRALLMService()
    return _lora_llm_service
