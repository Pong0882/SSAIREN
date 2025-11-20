import os
from openai import OpenAI

# 클라이언트를 함수 내부에서 초기화하도록 지연 (환경변수 로드 타이밍 문제 해결)
def get_openai_client():
    """OpenAI 클라이언트 인스턴스 반환 (lazy initialization)"""
    return OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

'''
from openai import OpenAI
client = OpenAI()

audio_file = open("speech.mp3", "rb")

<defalut code>
transcript = client.audio.transcriptions.create(
  model="gpt-4o-transcribe",
  file=audio_file
)

<stream code>
stream = client.audio.transcriptions.create(
  model="gpt-4o-mini-transcribe",
  file=audio_file,
  stream=True
)

for event in stream:
  print(event)
'''

def transcribe_audio_stream(audio_file_path: str, language: str = "ko"):
    """Whisper API를 사용한 음성-텍스트 변환 (스트리밍, 한국어 전용)"""
    client = get_openai_client()  # 함수 호출 시점에 클라이언트 생성
    with open(audio_file_path, "rb") as audio_file:
        stream = client.audio.transcriptions.create(
            file=audio_file,
            model="gpt-4o-transcribe-diarize",
            language="ko",
            chunking_strategy="auto",  # 화자 분리 모델 필수 파라미터
            stream=True
        )
        
        # 스트림 이벤트를 순회하며 yield
        for event in stream:
            yield event

