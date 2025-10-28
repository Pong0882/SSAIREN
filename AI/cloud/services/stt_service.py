import os
from openai import OpenAI

client = OpenAI(api_key=os.getenv("OPENAI_API_KEY"))

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

def transcribe_audio_stream(audio_file_path: str, language: str = "en"):
    """Whisper API를 사용한 음성-텍스트 변환 (스트리밍)"""
    with open(audio_file_path, "rb") as audio_file:
        # language가 None이면 파라미터에서 제외 (자동 감지)
        params = {
            "file": audio_file,
            "model": "gpt-4o-transcribe-diarize",
            "stream": True
        }
        
        if language:
            params["language"] = language
        
        stream = client.audio.transcriptions.create(**params)
        
        # 스트림 이벤트를 순회하며 yield
        for event in stream:
            yield event

