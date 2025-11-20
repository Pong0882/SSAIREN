"""
로컬 Whisper STT API 테스트 스크립트

사용법:
    1. FastAPI 서버 실행 (별도 터미널):
       uvicorn app:app --reload --port 8000

    2. API 테스트 실행:
       python test_local_stt_api.py <audio_file_path>

예시:
    python test_local_stt_api.py ./sample.mp3
"""

import sys
import os
import requests
import time
from pathlib import Path


BASE_URL = "http://localhost:8000/api"


def test_health_check():
    """헬스체크 테스트"""
    print("=" * 80)
    print("🏥 [헬스체크]")
    print("=" * 80)

    try:
        response = requests.get(f"{BASE_URL}/stt/local/health", timeout=30)
        response.raise_for_status()
        data = response.json()

        print(f"✅ 상태: {data['status']}")
        print(f"  - 모델 크기: {data['model_size']}")
        print(f"  - 디바이스: {data['device']}")
        print(f"  - 연산 타입: {data['compute_type']}")
        print(f"  - 모델 로드됨: {data['model_loaded']}")

        return True

    except requests.exceptions.ConnectionError:
        print("❌ 서버에 연결할 수 없습니다.")
        print("\n서버 실행 방법:")
        print("  uvicorn app:app --reload --port 8000")
        return False

    except Exception as e:
        print(f"❌ 헬스체크 실패: {str(e)}")
        return False


def test_stt_full(audio_file_path: str):
    """전체 텍스트 API 테스트"""
    print("\n" + "=" * 80)
    print("🟢 [전체 텍스트 API 테스트]")
    print("=" * 80)

    try:
        with open(audio_file_path, "rb") as f:
            files = {"file": (os.path.basename(audio_file_path), f, "audio/mpeg")}
            data = {"language": "ko"}

            print("📤 업로드 중...")
            start_time = time.time()

            response = requests.post(
                f"{BASE_URL}/stt/local/full",
                files=files,
                data=data,
                timeout=300  # 5분 타임아웃
            )
            response.raise_for_status()

            elapsed = time.time() - start_time
            result = response.json()

            print(f"\n✅ 전체 텍스트:")
            print(f"{result['text']}\n")

            print(f"📊 통계:")
            print(f"  - 언어: {result['language']}")
            print(f"  - 세그먼트 수: {len(result['segments'])}")
            print(f"  - 서버 처리 시간: {result['duration']}초")
            print(f"  - 총 소요 시간: {elapsed:.2f}초 (업로드 포함)")

            if result['segments']:
                total_duration = result['segments'][-1]['end']
                print(f"  - 오디오 길이: {total_duration:.2f}초")
                print(f"  - 처리 속도: {total_duration / result['duration']:.2f}x 실시간")

            return True

    except Exception as e:
        print(f"❌ API 호출 실패: {str(e)}")
        return False


def test_stt_stream(audio_file_path: str):
    """스트리밍 API 테스트"""
    print("\n" + "=" * 80)
    print("🔴 [스트리밍 API 테스트]")
    print("=" * 80)

    try:
        with open(audio_file_path, "rb") as f:
            files = {"file": (os.path.basename(audio_file_path), f, "audio/mpeg")}
            data = {"language": "ko"}

            print("📤 스트리밍 시작...\n")

            response = requests.post(
                f"{BASE_URL}/stt/local/stream",
                files=files,
                data=data,
                stream=True,
                timeout=300
            )
            response.raise_for_status()

            segment_count = 0
            for line in response.iter_lines():
                if line:
                    line_str = line.decode('utf-8')
                    if line_str.startswith('data: '):
                        segment_count += 1
                        import json
                        segment = json.loads(line_str[6:])

                        print(f"[세그먼트 {segment_count}]")
                        print(f"  시간: {segment['start']:.2f}s ~ {segment['end']:.2f}s")
                        print(f"  텍스트: {segment['text']}\n")

            print(f"✅ 총 {segment_count}개 세그먼트 수신")
            return True

    except Exception as e:
        print(f"❌ 스트리밍 실패: {str(e)}")
        return False


def main():
    if len(sys.argv) < 2:
        print("❌ 사용법: python test_local_stt_api.py <audio_file_path>")
        print("\n예시:")
        print("  python test_local_stt_api.py ./sample.mp3")
        print("\n서버 실행:")
        print("  uvicorn app:app --reload --port 8000")
        sys.exit(1)

    audio_file_path = sys.argv[1]

    # 파일 존재 확인
    if not os.path.exists(audio_file_path):
        print(f"❌ 파일을 찾을 수 없습니다: {audio_file_path}")
        sys.exit(1)

    print(f"🎤 오디오 파일: {audio_file_path}")
    print(f"📦 파일 크기: {os.path.getsize(audio_file_path) / 1024 / 1024:.2f} MB")

    # 테스트 실행
    success = True

    # 1. 헬스체크
    if not test_health_check():
        sys.exit(1)

    # 2. 전체 텍스트 API
    success &= test_stt_full(audio_file_path)

    # 3. 스트리밍 API
    success &= test_stt_stream(audio_file_path)

    # 결과
    print("\n" + "=" * 80)
    if success:
        print("✅ 모든 API 테스트 완료!")
    else:
        print("⚠️  일부 테스트 실패")
    print("=" * 80)


if __name__ == "__main__":
    main()
