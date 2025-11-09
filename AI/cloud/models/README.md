# 📦 Models 폴더

파인튜닝된 LoRA 모델을 저장하는 폴더입니다.

---

## 📁 폴더 구조

```
models/
└── ems-lora-checkpoint/       ← checkpoint-500 폴더를 여기에 복사
    ├── adapter_config.json
    ├── adapter_model.safetensors
    ├── tokenizer.json
    ├── tokenizer_config.json
    ├── special_tokens_map.json
    ├── vocab.json
    └── merges.txt
```

### 3️⃣ 필요한 파일

✅ adapter_config.json          (LoRA 설정)
✅ adapter_model.safetensors    (학습된 가중치 - 가장 중요!)
✅ tokenizer.json               (토크나이저)
✅ tokenizer_config.json        (토크나이저 설정)
✅ special_tokens_map.json      (특수 토큰)
✅ vocab.json                   (어휘)
✅ merges.txt                   (BPE 병합)
---

## 🔧 환경 변수 설정

`config/.env` 파일에 추가:
```bash
# LoRA 어댑터 경로
LORA_ADAPTER_PATH=./models/ems-lora-checkpoint

# 베이스 모델
BASE_MODEL_NAME=Qwen/Qwen2.5-3B-Instruct
```

---

## 🔄 모델 버전 관리

새로운 파인튜닝 모델로 교체:

```bash
# 1. 새 체크포인트 복사
cp -r checkpoint-600 models/ems-lora-checkpoint-v2

# 2. API로 모델 교체
curl -X POST http://localhost:8000/stt-to-json/reload-model?adapter_path=./models/ems-lora-checkpoint-v2

# 또는 환경 변수 수정 후 서버 재시작
LORA_ADAPTER_PATH=./models/ems-lora-checkpoint-v2
```

---

## 📊 모델 크기 예상

- **adapter_model.safetensors**: 약 50-100MB
- **tokenizer 파일들**: 약 5MB
- **총합**: 약 55-105MB

---

## 💡 팁

### 여러 버전 관리
```
models/
├── ems-lora-checkpoint-v1/    (최초 버전)
├── ems-lora-checkpoint-v2/    (개선 버전)
└── ems-lora-checkpoint-best/  (최고 성능)
```

### Git에서 제외
`.gitignore`에 추가:
```
models/*.safetensors
models/ems-lora-*/
```

---

**준비되면 서버를 시작하세요!** 🚀

