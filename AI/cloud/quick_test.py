"""
빠른 테스트 스크립트 - 샘플 오디오 생성 + STT 테스트 올인원

실행:
    pip install gtts
    python quick_test.py
"""

import sys
import os
from pathlib import Path

print("=" * 80)
print("🚀 로컬 Whisper STT 빠른 테스트")
print("=" * 80)

# 1. 샘플 오디오 생성
print("\n[1단계] 샘플 오디오 생성 중...")
sample_file = "sample_audio.mp3"

if os.path.exists(sample_file):
    print(f"✅ 기존 파일 사용: {sample_file}")
else:
    try:
        from gtts import gTTS
        text = "안녕하세요. 이것은 음성 인식 테스트입니다. 한국어 STT 모델이 잘 작동하는지 확인해봅시다."
        tts = gTTS(text=text, lang='ko')
        tts.save(sample_file)
        print(f"✅ TTS로 생성 완료: {sample_file}")
    except ImportError:
        print("❌ gTTS가 설치되지 않았습니다.")
        print("\n해결 방법 1: gTTS 설치")
        print("  pip install gtts")
        print("\n해결 방법 2: 직접 오디오 파일 준비")
        print("  sample_audio.mp3 파일을 현재 디렉토리에 넣어주세요")
        sys.exit(1)
    except Exception as e:
        print(f"❌ TTS 생성 실패: {e}")
        print("\n대안: sample_audio.mp3 파일을 직접 준비해주세요")
        sys.exit(1)

# 2. STT 테스트
print("\n[2단계] STT 모델 로딩 및 변환 시작...")

try:
    # 프로젝트 루트를 경로에 추가
    sys.path.insert(0, str(Path(__file__).parent))

    from services.stt_service_local import transcribe_audio_full

    # STT 실행
    result = transcribe_audio_full(sample_file, language="ko")

    # 결과 출력
    print("\n" + "=" * 80)
    print("✅ STT 변환 완료!")
    print("=" * 80)
    print(f"\n📝 변환된 텍스트:")
    print(f"   {result['text']}\n")

    print(f"📊 통계:")
    print(f"   - 언어: {result['language']}")
    print(f"   - 세그먼트 수: {len(result['segments'])}")
    print(f"   - 처리 시간: {result['duration']}초")

    if result['segments']:
        total_duration = result['segments'][-1]['end']
        print(f"   - 오디오 길이: {total_duration:.2f}초")
        print(f"   - 처리 속도: {total_duration / result['duration']:.2f}x 실시간")

    print("\n" + "=" * 80)
    print("🎉 테스트 성공! 로컬 Whisper STT가 정상 작동합니다.")
    print("=" * 80)

    print("\n📚 다음 단계:")
    print("  1. 실제 음성 파일로 테스트:")
    print("     python test_local_stt_direct.py your_audio.mp3")
    print("\n  2. FastAPI 서버로 테스트:")
    print("     uvicorn app:app --reload")
    print("     python test_local_stt_api.py sample_audio.mp3")

except ImportError as e:
    print(f"\n❌ 모듈 임포트 실패: {e}")
    print("\n해결 방법:")
    print("  pip install faster-whisper")
    sys.exit(1)

except Exception as e:
    print(f"\n❌ STT 처리 실패: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)
