# 🧪 로컬 Whisper STT 테스트 가이드

## 📋 테스트 전 준비

### 1. 환경 변수 설정 (선택사항)

`config/.env`에 추가 (이미 있다면 생략):

```env
# Whisper 모델 설정
WHISPER_MODEL_SIZE=medium    # tiny/base/small/medium 중 선택
WHISPER_DEVICE=cpu
WHISPER_COMPUTE_TYPE=int8
```

### 2. 의존성 설치

```bash
pip install faster-whisper
```

또는

```bash
pip install -r requirements.txt
```

### 3. 샘플 오디오 파일 준비

**방법 A: 직접 녹음**
- Windows 음성 녹음기로 한국어 음성 녹음
- 저장 위치: `./sample_audio.mp3`

**방법 B: 온라인에서 다운로드**
```bash
# YouTube 오디오 다운로드 (yt-dlp 필요)
pip install yt-dlp
yt-dlp -x --audio-format mp3 --output "sample_audio.mp3" [유튜브_URL]
```

**방법 C: TTS로 생성 (빠른 테스트)**
```python
# test_audio_generator.py 파일 생성 후 실행
from gtts import gTTS
text = "안녕하세요. 이것은 음성 인식 테스트입니다. 한국어 STT 모델이 잘 작동하는지 확인해봅시다."
tts = gTTS(text=text, lang='ko')
tts.save("sample_audio.mp3")
print("✅ sample_audio.mp3 생성 완료!")
```

---

## 🚀 테스트 방법

### ✅ 방법 1: 직접 함수 호출 (가장 빠름)

**장점**: 서버 없이 바로 테스트 가능

```bash
python test_local_stt_direct.py sample_audio.mp3
```

**예상 출력**:
```
🔄 Loading Whisper model: medium on cpu with int8
✅ Whisper model loaded in 3.21s

🎤 Transcribing: sample_audio.mp3
🌍 Detected language: ko (probability: 0.99)

[세그먼트 1]
  시간: 0.00s ~ 2.50s
  텍스트: 안녕하세요
  확률: -0.234 (무음 확률: 0.001)

✅ 전체 텍스트:
안녕하세요 이것은 음성 인식 테스트입니다

📊 통계:
  - 처리 시간: 0.85초
  - 처리 속도: 8.47x 실시간
```

---

### ✅ 방법 2: FastAPI 서버 테스트

**Step 1: 라우터 등록**

`app.py` 수정:
```python
from routers.stt_router_local import router as stt_router_local

# 라우터 등록 (맨 아래 추가)
app.include_router(stt_router_local, prefix="/api")
```

**Step 2: 서버 실행**
```bash
uvicorn app:app --reload --port 8000
```

**Step 3-A: 테스트 스크립트 사용**
```bash
# 새 터미널에서 실행
python test_local_stt_api.py sample_audio.mp3
```

**Step 3-B: cURL로 테스트**
```bash
# 헬스체크
curl http://localhost:8000/api/stt/local/health

# 전체 텍스트 변환
curl -X POST http://localhost:8000/api/stt/local/full \
  -F "file=@sample_audio.mp3" \
  -F "language=ko"
```

**Step 3-C: Swagger UI로 테스트**
1. 브라우저에서 http://localhost:8000/docs 접속
2. `POST /api/stt/local/full` 클릭
3. "Try it out" 클릭
4. 파일 업로드 후 "Execute" 클릭

---

### ✅ 방법 3: 간단한 Python 스크립트

빠른 테스트를 위한 최소 코드:

```python
# quick_test.py
import sys
sys.path.insert(0, '.')

from services.stt_service_local import transcribe_audio_full

result = transcribe_audio_full("sample_audio.mp3")
print(f"✅ 변환 결과: {result['text']}")
print(f"⏱️  처리 시간: {result['duration']}초")
```

실행:
```bash
python quick_test.py
```

---

## 🎯 추천 테스트 순서

### 1단계: 직접 함수 호출 (5분)
```bash
# 샘플 오디오 준비
# (녹음 또는 TTS로 생성)

# 테스트 실행
python test_local_stt_direct.py sample_audio.mp3
```

**✅ 성공 시**: 모델 로딩 및 STT 기본 기능 확인 완료
**❌ 실패 시**: 에러 메시지 확인 → 의존성 설치 확인

---

### 2단계: 서버 API 테스트 (10분)
```bash
# 터미널 1: 서버 실행
uvicorn app:app --reload --port 8000

# 터미널 2: API 테스트
python test_local_stt_api.py sample_audio.mp3
```

**✅ 성공 시**: 실제 배포 환경에서도 작동 확인
**❌ 실패 시**: 라우터 등록 확인

---

### 3단계: 실제 음성 데이터 테스트 (15분)
```bash
# 긴 오디오 파일로 테스트 (1-5분)
python test_local_stt_direct.py long_audio.mp3

# 처리 속도 및 정확도 확인
```

---

## 📊 성능 벤치마크 방법

```bash
# 여러 파일로 테스트
python test_local_stt_direct.py audio_10sec.mp3
python test_local_stt_direct.py audio_30sec.mp3
python test_local_stt_direct.py audio_1min.mp3
python test_local_stt_direct.py audio_5min.mp3
```

**확인 사항**:
- 처리 속도 (실시간 대비 몇 배?)
- 정확도 (텍스트가 정확한가?)
- 메모리 사용량 (작업 관리자 확인)

---

## 🐛 문제 해결

### 문제 1: `ModuleNotFoundError: No module named 'faster_whisper'`
```bash
pip install faster-whisper
```

### 문제 2: 모델 다운로드가 느림
- 최초 실행 시 ~1.5GB 다운로드 (medium 모델)
- 인터넷 연결 확인
- 다운로드 진행률은 콘솔에 표시됨

### 문제 3: `CUDA not available` 경고
- 정상입니다 (CPU 사용)
- 무시하고 진행하세요
- GPU 사용 원하면: `WHISPER_DEVICE=cuda` 설정

### 문제 4: 메모리 부족
```env
# 더 작은 모델 사용
WHISPER_MODEL_SIZE=small  # medium → small
```

### 문제 5: 처리가 너무 느림
```python
# stt_service_local.py 수정
beam_size=5  # 10 → 5 (속도 향상)
best_of=1    # 5 → 1
```

---

## 💡 유용한 팁

### 1. 모델 캐시 확인
```bash
ls -lh models/whisper/
```

### 2. 모델 재다운로드
```bash
rm -rf models/whisper/
# 다음 실행 시 자동으로 재다운로드
```

### 3. 여러 모델 비교
```bash
# tiny 모델 테스트 (빠름)
WHISPER_MODEL_SIZE=tiny python test_local_stt_direct.py sample.mp3

# medium 모델 테스트 (정확함)
WHISPER_MODEL_SIZE=medium python test_local_stt_direct.py sample.mp3
```

### 4. 로그 레벨 조정
```python
# stt_service_local.py 상단에 추가
import logging
logging.getLogger("faster_whisper").setLevel(logging.INFO)
```

---

## 🎉 테스트 완료 체크리스트

- [ ] faster-whisper 설치 완료
- [ ] 샘플 오디오 파일 준비
- [ ] 직접 함수 호출 테스트 성공
- [ ] FastAPI 서버 실행 확인
- [ ] API 엔드포인트 테스트 성공
- [ ] 실제 음성 데이터 정확도 확인
- [ ] 처리 속도 벤치마크 확인

---

## 📞 다음 단계

테스트 성공 후:
1. `app.py`에 라우터 정식 등록
2. Docker 이미지 빌드
3. 프로덕션 배포
4. 모니터링 설정

자세한 내용은 `LOCAL_STT_GUIDE.md` 참조!
