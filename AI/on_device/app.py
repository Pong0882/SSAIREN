from fastapi import FastAPI

app = FastAPI(title='On Device AI', description = "온디바이스 AI를 기반으로 한 서비스를 제공하는 서버")

@app.get("/")
def read_root():
    return {"message": "Hello, On Device AI!"}

