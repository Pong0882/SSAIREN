"""
파인튜닝된 LoRA 모델을 사용한 LLM 서비스
STT 대본 → JSON 추출

모델 교체가 쉽도록 설계됨
"""
import os
import json
import torch
from typing import Optional, Dict, Any
from transformers import AutoTokenizer, AutoModelForCausalLM
from peft import PeftModel

class LoRALLMService:
    """파인튜닝된 LoRA 모델 서비스 (Singleton)"""
    
    _instance: Optional['LoRALLMService'] = None
    _model = None
    _tokenizer = None
    _device = None
    
    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance
    
    def __init__(self):
        """모델 초기화 (최초 1회만)"""
        if self._model is None:
            self._load_model()
    
    def _load_model(self):
        """LoRA 모델 로드"""
        # 환경 변수에서 경로 읽기
        base_model_name = os.getenv("BASE_MODEL_NAME", "Qwen/Qwen2.5-3B-Instruct")
        adapter_path = os.getenv("LORA_ADAPTER_PATH", "./models/ems-lora-checkpoint")
        
        print(f"🔄 베이스 모델 로딩: {base_model_name}")
        print(f"🔄 LoRA 어댑터 로딩: {adapter_path}")
        
        # GPU 사용 가능 여부 확인
        self._device = "cuda" if torch.cuda.is_available() else "cpu"
        print(f"💻 디바이스: {self._device}")
        
        try:
            # 토크나이저 로드
            self._tokenizer = AutoTokenizer.from_pretrained(base_model_name)
            
            # 베이스 모델 로드
            self._model = AutoModelForCausalLM.from_pretrained(
                base_model_name,
                torch_dtype=torch.float16 if self._device == "cuda" else torch.float32,
                device_map="auto" if self._device == "cuda" else None,
                low_cpu_mem_usage=True
            )
            
            # LoRA 어댑터 적용
            if os.path.exists(adapter_path):
                print(f"✅ LoRA 어댑터 발견: {adapter_path}")
                self._model = PeftModel.from_pretrained(self._model, adapter_path)
                print("✅ LoRA 어댑터 적용 완료!")
            else:
                print(f"⚠️  LoRA 어댑터 없음, 베이스 모델만 사용: {adapter_path}")
            
            # 추론 모드
            self._model.eval()
            
            if self._device == "cpu":
                self._model = self._model.to("cpu")
            
            print("✅ 모델 로딩 완료!")
            
        except Exception as e:
            print(f"❌ 모델 로딩 실패: {e}")
            raise
    
    def extract_json_from_conversation(
        self,
        conversation: str,
        max_new_tokens: int = 700,
        temperature: float = 0.1,
        top_p: float = 0.9
    ) -> Dict[str, Any]:
        """
        대화에서 JSON 추출
        
        Args:
            conversation: STT로 변환된 대화 텍스트
            max_new_tokens: 최대 생성 토큰 수
            temperature: 샘플링 온도 (낮을수록 결정적)
            top_p: nucleus sampling
            
        Returns:
            추출된 JSON (dict)
        """
        if self._model is None or self._tokenizer is None:
            raise RuntimeError("모델이 로드되지 않았습니다")
        
        # 프롬프트 구성 (파인튜닝된 형식에 맞춤)
        prompt = f"""아래는 응급구조사와 환자의 대화입니다.
이 대화를 읽고, GBNF 스키마에 맞는 JSON만 출력하세요.
- 설명 문장 쓰지 말 것
- 마크다운 코드블록( ``` ) 쓰지 말 것
- 순수 JSON만 출력할 것

{conversation}

### 응답:
"""
        
        # 토크나이징
        inputs = self._tokenizer(prompt, return_tensors="pt")
        
        if self._device == "cuda":
            inputs = {k: v.to("cuda") for k, v in inputs.items()}
        
        # 생성
        with torch.no_grad():
            outputs = self._model.generate(
                **inputs,
                max_new_tokens=max_new_tokens,
                temperature=temperature,
                top_p=top_p,
                do_sample=False,  # 결정적 생성
                pad_token_id=self._tokenizer.pad_token_id,
                eos_token_id=self._tokenizer.eos_token_id
            )
        
        # 디코딩
        decoded = self._tokenizer.decode(outputs[0], skip_special_tokens=True)
        
        # 응답 부분만 추출
        generated_part = decoded.split("### 응답:")[-1].strip()
        
        # 코드블록 제거 (``` 제거)
        if "```" in generated_part:
            # 첫 번째 ```와 마지막 ``` 사이의 내용 추출
            start = generated_part.find("```")
            end = generated_part.rfind("```")
            if start != -1 and end != -1 and start < end:
                generated_part = generated_part[start + 3:end].strip()
                # json이라는 언어 태그 제거
                if generated_part.startswith("json"):
                    generated_part = generated_part[4:].strip()
        
        # JSON 파싱
        try:
            parsed_json = json.loads(generated_part)
            return {
                "success": True,
                "json": parsed_json,
                "raw_text": generated_part
            }
        except json.JSONDecodeError as e:
            # JSON 파싱 실패 시 원문 반환
            return {
                "success": False,
                "error": str(e),
                "raw_text": generated_part
            }
    
    def reload_model(self, adapter_path: str):
        """
        새로운 어댑터로 모델 재로드
        (학습 후 모델 교체 시 사용)
        
        Args:
            adapter_path: 새로운 LoRA 어댑터 경로
        """
        print(f"🔄 모델 재로드 시작: {adapter_path}")
        
        # 기존 모델 메모리 해제
        if self._model is not None:
            del self._model
            if torch.cuda.is_available():
                torch.cuda.empty_cache()
        
        # 새 경로로 환경 변수 업데이트
        os.environ["LORA_ADAPTER_PATH"] = adapter_path
        
        # 모델 재로드
        self._model = None
        self._load_model()
        
        print("✅ 모델 재로드 완료!")


# 전역 인스턴스
_lora_llm_service: Optional[LoRALLMService] = None

def get_lora_llm_service() -> LoRALLMService:
    """LoRA LLM 서비스 인스턴스 반환 (Singleton)"""
    global _lora_llm_service
    if _lora_llm_service is None:
        _lora_llm_service = LoRALLMService()
    return _lora_llm_service

