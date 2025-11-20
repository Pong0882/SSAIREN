"""
로컬 Whisper STT 직접 테스트 스크립트

사용법:
    python test_local_stt_direct.py <audio_file_path>

예시:
    python test_local_stt_direct.py ./sample.mp3
"""

import sys
import os
from pathlib import Path

# 프로젝트 루트를 Python path에 추가
sys.path.insert(0, str(Path(__file__).parent))

from services.stt_service_local import transcribe_audio_stream, transcribe_audio_full


def test_stream_mode(audio_file_path: str):
    """스트리밍 모드 테스트"""
    print("=" * 80)
    print("🔴 [스트리밍 모드 테스트]")
    print("=" * 80)

    for i, segment in enumerate(transcribe_audio_stream(audio_file_path), start=1):
        print(f"\n[세그먼트 {i}]")
        print(f"  시간: {segment['start']:.2f}s ~ {segment['end']:.2f}s")
        print(f"  텍스트: {segment['text']}")
        print(f"  확률: {segment['avg_logprob']:.3f} (무음 확률: {segment['no_speech_prob']:.3f})")


def test_full_mode(audio_file_path: str):
    """전체 텍스트 모드 테스트"""
    print("\n" + "=" * 80)
    print("🟢 [전체 텍스트 모드 테스트]")
    print("=" * 80)

    result = transcribe_audio_full(audio_file_path)

    print(f"\n✅ 전체 텍스트:")
    print(f"{result['text']}\n")

    print(f"📊 통계:")
    print(f"  - 언어: {result['language']}")
    print(f"  - 세그먼트 수: {len(result['segments'])}")
    print(f"  - 처리 시간: {result['duration']}초")

    if result['segments']:
        total_duration = result['segments'][-1]['end']
        print(f"  - 오디오 길이: {total_duration:.2f}초")
        print(f"  - 처리 속도: {total_duration / result['duration']:.2f}x 실시간")


def main():
    if len(sys.argv) < 2:
        print("❌ 사용법: python test_local_stt_direct.py <audio_file_path>")
        print("\n예시:")
        print("  python test_local_stt_direct.py ./sample.mp3")
        print("  python test_local_stt_direct.py ./test_audio/korean_speech.wav")
        sys.exit(1)

    audio_file_path = sys.argv[1]

    # 파일 존재 확인
    if not os.path.exists(audio_file_path):
        print(f"❌ 파일을 찾을 수 없습니다: {audio_file_path}")
        sys.exit(1)

    print(f"🎤 오디오 파일: {audio_file_path}")
    print(f"📦 파일 크기: {os.path.getsize(audio_file_path) / 1024 / 1024:.2f} MB")

    # 환경변수 설정 (선택사항)
    # os.environ["WHISPER_MODEL_SIZE"] = "medium"  # tiny/base/small/medium/large-v3
    # os.environ["WHISPER_DEVICE"] = "cpu"  # cpu/cuda
    # os.environ["WHISPER_COMPUTE_TYPE"] = "int8"  # int8/float16/float32

    # 테스트 실행
    try:
        # 1. 스트리밍 모드 테스트
        test_stream_mode(audio_file_path)

        # 2. 전체 텍스트 모드 테스트
        test_full_mode(audio_file_path)

        print("\n" + "=" * 80)
        print("✅ 모든 테스트 완료!")
        print("=" * 80)

    except Exception as e:
        print(f"\n❌ 오류 발생: {str(e)}")
        import traceback
        traceback.print_exc()
        sys.exit(1)


if __name__ == "__main__":
    main()
