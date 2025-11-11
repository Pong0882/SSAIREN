FROM python:3.11-slim

WORKDIR /app

# 시스템 패키지 업데이트 및 필수 도구 설치
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    git \
    && rm -rf /var/lib/apt/lists/*

# 의존성 파일 복사 및 설치
COPY requirements.txt .
RUN pip install --no-cache-dir --timeout 300 --retries 10 -r requirements.txt

# 애플리케이션 코드 복사
COPY . .

# 포트 노출
EXPOSE 8000

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD python -c "import requests; requests.get('http://localhost:8000/')" || exit 1

# PyTorch 메모리 최적화 (1.5B 모델용)
ENV PYTORCH_CUDA_ALLOC_CONF=max_split_size_mb:256
ENV TOKENIZERS_PARALLELISM=false
ENV MALLOC_TRIM_THRESHOLD_=100000

# uvicorn 실행 (워커 1개, 메모리 5-6GB 제한)
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "8000", "--workers", "1", "--limit-concurrency", "5"]
