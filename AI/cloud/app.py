from fastapi import FastAPI

app = FastAPI(title='CLOUD AI', description = "클라우드 AI를 기반으로 한 서비스를 제공하는 서버")

@app.get("/")
def read_root():
    return {"message": "Hello, Cloud AI!"}

