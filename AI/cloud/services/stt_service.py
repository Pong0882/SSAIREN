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

def transcribe_audio(audio_file_path: str, language: str = "en") -> str:
    """Whisper API를 사용한 음성-텍스트 변환"""
    with open(audio_file_path, "rb") as audio_file:
        transcript = client.audio.transcriptions.create(
            file=audio_file,
            model="gpt-4o-transcribe-diarize",
            stream=True

        )
    return transcript

