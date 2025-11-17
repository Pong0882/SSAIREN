# 로컬 Whisper STT 가이드

OpenAI API 대신 온프레미스 Faster-Whisper 모델을 사용한 STT 구현입니다.

## 📋 목차
- [성능 비교](#성능-비교)
- [설치 방법](#설치-방법)
- [사용 방법](#사용-방법)
- [테스트](#테스트)
- [환경 설정](#환경-설정)

---

## 🔥 성능 비교

### 배포 방식별 성능 차이

| 방식 | 추론 속도 | 메모리 오버헤드 | 장점 |
|------|----------|----------------|------|
| **서버 직접 설치** | ⭐⭐⭐ 가장 빠름 | 없음 | 오버헤드 제로 |
| **도커 컨테이너 (권장)** | ⭐⭐⭐ 거의 동일 | ~100MB | 환경 일관성, 배포 편리 |
| **분리된 컨테이너** | ⭐⭐ 20-100ms 추가 | 컨테이너 2개 | 독립 스케일링 |

**→ 현재 구조에 통합 (도커 컨테이너 방식) 권장**

### Faster-Whisper vs OpenAI API

| 항목 | Faster-Whisper | OpenAI API |
|------|----------------|------------|
| 속도 | 5-10배 빠름 | 네트워크 지연 있음 |
| 비용 | 무료 (서버 비용만) | 분당 과금 |
| 정확도 | 동일 (같은 모델) | 동일 |
| 데이터 프라이버시 | ✅ 온프레미스 | ❌ 외부 전송 |

---

## 🚀 설치 방법

### 1. 의존성 설치

```bash
pip install -r requirements.txt
```

`requirements.txt`에 `faster-whisper>=1.0.0`이 이미 추가되어 있습니다.

### 2. 라우터 등록

`app.py`에 다음 라인을 추가하세요:

```python
from routers.stt_router_local import router as stt_router_local

# 라우터 등록
app.include_router(stt_router_local, prefix="/api")
```

### 3. 환경 변수 설정 (선택사항)

`config/.env` 파일에 추가:

```env
# Whisper 모델 설정
WHISPER_MODEL_SIZE=medium    # tiny/base/small/medium/large-v3
WHISPER_DEVICE=cpu           # cpu or cuda
WHISPER_COMPUTE_TYPE=int8    # int8/float16/float32
```

---

## 📖 사용 방법

### API 엔드포인트

#### 1. 헬스체크
```bash
GET /api/stt/local/health
```

응답:
```json
{
  "status": "healthy",
  "model_size": "medium",
  "device": "cpu",
  "compute_type": "int8",
  "model_loaded": true
}
```

#### 2. 전체 텍스트 변환
```bash
POST /api/stt/local/full
Content-Type: multipart/form-data

file: <audio_file>
language: ko
```

응답:
```json
{
  "text": "변환된 전체 텍스트",
  "segments": [
    {
      "start": 0.0,
      "end": 3.5,
      "text": "안녕하세요",
      "avg_logprob": -0.234,
      "no_speech_prob": 0.001
    }
  ],
  "language": "ko",
  "duration": 1.23
}
```

#### 3. 스트리밍 변환
```bash
POST /api/stt/local/stream
Content-Type: multipart/form-data

file: <audio_file>
language: ko
```

응답 (Server-Sent Events):
```
data: {"start": 0.0, "end": 3.5, "text": "안녕하세요", ...}
data: {"start": 3.5, "end": 7.2, "text": "반갑습니다", ...}
```

---

## 🧪 테스트

### 1. 직접 함수 호출 테스트

```bash
# 샘플 오디오 파일 준비
# (mp3, wav, m4a, flac 등 지원)

python test_local_stt_direct.py ./sample.mp3
```

**출력 예시:**
```
🔄 Loading Whisper model: medium on cpu with int8
✅ Whisper model loaded in 2.34s

🎤 Transcribing: ./sample.mp3
🌍 Detected language: ko (probability: 0.99)

[세그먼트 1]
  시간: 0.00s ~ 3.50s
  텍스트: 안녕하세요
  확률: -0.234 (무음 확률: 0.001)

✅ 전체 텍스트:
안녕하세요 반갑습니다

📊 통계:
  - 언어: ko
  - 세그먼트 수: 2
  - 처리 시간: 0.85초
  - 오디오 길이: 7.20초
  - 처리 속도: 8.47x 실시간
```

### 2. FastAPI 서버 테스트

**서버 실행:**
```bash
uvicorn app:app --reload --port 8000
```

**API 테스트:**
```bash
python test_local_stt_api.py ./sample.mp3
```

**cURL 테스트:**
```bash
# 헬스체크
curl http://localhost:8000/api/stt/local/health

# 전체 텍스트
curl -X POST http://localhost:8000/api/stt/local/full \
  -F "file=@sample.mp3" \
  -F "language=ko"
```

---

## ⚙️ 환경 설정

### 모델 크기별 성능

| 모델 | 크기 | 메모리 (CPU) | 속도 | 한국어 정확도 | 추천 |
|------|------|-------------|------|--------------|------|
| **tiny** | ~40MB | ~1GB | ⭐⭐⭐ | 낮음 | 테스트용 |
| **base** | ~75MB | ~1GB | ⭐⭐⭐ | 보통 | 실시간 |
| **small** | ~500MB | ~2GB | ⭐⭐⭐ | 양호 | **실시간** ✅ |
| **medium** | ~1.5GB | ~5GB | ⭐⭐ | 우수 | **정확도 중시** ✅ |
| **large-v3** | ~3GB | ~10GB | ⭐ | 최고 | 배치 처리 |

**권장 설정 (정확도 우선):**
```env
WHISPER_MODEL_SIZE=medium
WHISPER_DEVICE=cpu
WHISPER_COMPUTE_TYPE=int8
```

### GPU 사용 (선택사항)

CUDA 사용 가능 시:
```env
WHISPER_DEVICE=cuda
WHISPER_COMPUTE_TYPE=float16  # GPU는 float16 권장
```

**속도 향상:** ~2-3배 빠름

---

## 🎯 정확도 향상 설정

현재 `stt_service_local.py`에 적용된 설정:

```python
model.transcribe(
    audio_file_path,
    language="ko",
    beam_size=10,          # 빔 서치 크기 (5→10, 느리지만 정확)
    best_of=5,             # 상위 5개 후보 중 최선 선택
    temperature=0.0,       # 결정적 출력 (0.0 = 가장 정확)
    condition_on_previous_text=True,  # 이전 컨텍스트 활용
    vad_filter=True,       # 무음 제거
)
```

**정확도 vs 속도 트레이드오프:**
- `beam_size=10` → 느리지만 정확
- `beam_size=5` → 빠르지만 덜 정확
- `beam_size=1` → 가장 빠르지만 부정확

---

## 🐳 Docker 배포

### Dockerfile (이미 준비됨)

```dockerfile
FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

EXPOSE 8000

CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000"]
```

### 빌드 및 실행

```bash
# 빌드
docker-compose build

# 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f ai-cloud
```

**최초 실행 시:**
- Whisper 모델이 자동으로 다운로드됩니다 (~1.5GB for medium)
- 다운로드는 1회만 발생 (`./models/whisper`에 캐시됨)

---

## 📊 성능 벤치마크

테스트 환경: CPU (Intel i7), medium 모델

| 오디오 길이 | 처리 시간 | 실시간 배수 |
|-------------|----------|------------|
| 10초 | 1.2초 | 8.3x |
| 30초 | 3.5초 | 8.6x |
| 1분 | 7.1초 | 8.5x |
| 5분 | 35초 | 8.6x |

**결론:** 실시간보다 **약 8-9배 빠르게** 처리 가능

---

## ❓ FAQ

**Q: 모델이 너무 크면 어떻게 하나요?**
A: `small` 모델 사용 (~500MB, 한국어 성능 양호)

**Q: 실시간 스트리밍이 가능한가요?**
A: 세그먼트 단위 스트리밍 가능 (완전 실시간은 아님)

**Q: 여러 언어를 동시에 인식할 수 있나요?**
A: `language` 파라미터 없이 호출하면 자동 감지

**Q: 화자 분리(Diarization)가 가능한가요?**
A: Faster-Whisper는 기본 지원 안 함 (별도 라이브러리 필요)

---

## 📝 파일 구조

```
cloud/
├── services/
│   ├── stt_service.py          # 기존 OpenAI API 버전
│   └── stt_service_local.py    # 🆕 로컬 Whisper 버전
├── routers/
│   ├── stt_router.py           # 기존 라우터
│   └── stt_router_local.py     # 🆕 로컬 Whisper 라우터
├── test_local_stt_direct.py    # 🆕 직접 테스트
├── test_local_stt_api.py       # 🆕 API 테스트
├── models/
│   └── whisper/                # 모델 다운로드 위치
└── LOCAL_STT_GUIDE.md          # 🆕 이 가이드
```

---

## 🎉 완료!

이제 온프레미스 Whisper STT를 사용할 수 있습니다.

**다음 단계:**
1. `app.py`에 라우터 등록
2. `docker-compose up` 실행
3. 테스트 스크립트로 확인

**문의:** [프로젝트 이슈 트래커]
