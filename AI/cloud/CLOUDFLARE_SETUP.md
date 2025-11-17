# Cloudflare Tunnel 설정 가이드

외부 인터넷에서 AI 서비스에 안전하게 접근하기 위한 Cloudflare Tunnel 설정 방법입니다.

## 사전 준비

1. Cloudflare 계정 (무료)
2. 도메인 (선택사항 - Cloudflare에서 무료로 제공하는 임시 도메인 사용 가능)

## 설정 단계

### 1. Cloudflare 계정 생성 및 로그인

https://dash.cloudflare.com/ 에서 계정을 생성하거나 로그인합니다.

### 2. Cloudflare Tunnel 생성

1. Cloudflare Dashboard 접속
2. 좌측 메뉴에서 **Zero Trust** 선택
3. **Networks** > **Tunnels** 메뉴 선택
4. **Create a tunnel** 버튼 클릭
5. **Cloudflared** 선택
6. Tunnel 이름 입력 (예: `ai-cloud-tunnel`)
7. **Save tunnel** 클릭

### 3. Tunnel Token 복사

생성 후 표시되는 Token을 복사합니다. 다음과 같은 형태입니다:

```
eyJhIjoixxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx...
```

### 4. 환경 변수 설정

`config/.env` 파일에 Token을 추가합니다:

```bash
# 기존 환경 변수들...

# Cloudflare Tunnel Token
CLOUDFLARE_TUNNEL_TOKEN=여기에_복사한_토큰_붙여넣기
```

### 5. Public Hostname 설정

Cloudflare Dashboard의 Tunnel 설정 페이지에서:

1. **Public Hostname** 탭 선택
2. **Add a public hostname** 클릭
3. 다음 정보 입력:
   - **Subdomain**: 원하는 서브도메인 (예: `ai-api`)
   - **Domain**: Cloudflare에서 제공하는 도메인 선택 또는 본인 도메인
   - **Path**: 비워두기
   - **Type**: `HTTP`
   - **URL**: `ai-cloud:8000`
4. **Save hostname** 클릭

### 6. Docker Compose 실행

```bash
docker-compose up -d
```

### 7. 접근 확인

설정한 도메인으로 접근합니다:

```
https://ai-api.your-tunnel-domain.com
```

## 보안 설정 (선택사항)

### Access Policy 설정

특정 사용자만 접근하도록 제한:

1. Cloudflare Dashboard > **Zero Trust** > **Access** > **Applications**
2. **Add an application** 클릭
3. **Self-hosted** 선택
4. 접근 정책 설정 (이메일, IP 등)

### Rate Limiting

API 호출 제한:

1. Cloudflare Dashboard > 도메인 선택
2. **Security** > **WAF** > **Rate limiting rules**
3. 규칙 추가

## 문제 해결

### Tunnel이 연결되지 않을 때

```bash
# Cloudflared 로그 확인
docker logs cloudflared-tunnel

# Cloudflared 재시작
docker-compose restart cloudflared
```

### Token 오류

- `.env` 파일의 Token이 올바른지 확인
- Token 앞뒤 공백 제거 확인
- Cloudflare Dashboard에서 새 Token 생성 후 재시도

### 서비스 연결 오류

- `ai-cloud` 컨테이너가 정상 실행 중인지 확인
- Public Hostname의 URL이 `ai-cloud:8000`으로 정확히 설정되었는지 확인
- 네트워크가 `ssairen-net`으로 동일한지 확인

## 대안: ngrok (임시 테스트용)

빠른 테스트가 필요한 경우 ngrok 사용:

```bash
# ngrok 설치 (Windows)
choco install ngrok

# 또는 https://ngrok.com/download 에서 다운로드

# 실행
ngrok http 8000
```

ngrok은 무료 플랜에서 제한적이지만, 빠르게 테스트할 수 있습니다.

## 참고

- Cloudflare Tunnel은 무료이며 트래픽 제한이 없습니다
- SSL/TLS 인증서가 자동으로 제공됩니다
- DDoS 보호 기능이 포함되어 있습니다
- 방화벽 포트 포워딩이 필요 없습니다
