import { Client, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

// SockJS는 http/https URL을 사용하고, 자동으로 WebSocket으로 업그레이드됩니다
const WS_URL = import.meta.env.VITE_WS_URL || 'http://localhost:8080/ws';

// wss:// 를 https:// 로 변환 (SockJS는 http/https만 허용)
const SOCKJS_URL = WS_URL.replace(/^wss:\/\//, 'https://').replace(/^ws:\/\//, 'http://');

export class WebSocketClient {
  private client: Client | null = null;
  private subscriptions: Map<string, StompSubscription> = new Map();

  /**
   * WebSocket 연결
   */
  connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.client = new Client({
        webSocketFactory: () => new SockJS(SOCKJS_URL) as any,
        debug: (str) => {
          console.log('[WebSocket Debug]', str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          console.log('✅ WebSocket 연결 성공');
          resolve();
        },
        onStompError: (frame) => {
          console.error('❌ STOMP 에러:', frame);
          reject(new Error(frame.headers['message']));
        },
        onWebSocketError: (event) => {
          console.error('❌ WebSocket 에러:', event);
          reject(event);
        },
      });

      this.client.activate();
    });
  }

  /**
   * 특정 토픽 구독
   * @param destination 구독할 토픽 경로 (예: "/topic/hospital.1")
   * @param callback 메시지 수신 시 실행할 콜백 함수
   * @returns 구독 ID
   */
  subscribe<T = any>(
    destination: string,
    callback: (message: T, source: string) => void
  ): string | null {
    if (!this.client?.connected) {
      console.error('❌ WebSocket이 연결되지 않았습니다.');
      return null;
    }

    // 이미 구독 중이면 기존 구독 반환
    if (this.subscriptions.has(destination)) {
      console.log('⚠️ 이미 구독 중인 토픽:', destination);
      return destination;
    }

    const subscription = this.client.subscribe(destination, (message) => {
      try {
        const parsedMessage = JSON.parse(message.body);
        const sourceDestination = message.headers.destination || destination;
        console.log(`📩 메시지 수신 [${sourceDestination}]:`, parsedMessage);
        callback(parsedMessage, sourceDestination);
      } catch (error) {
        console.error('❌ 메시지 파싱 에러:', error);
      }
    });

    this.subscriptions.set(destination, subscription);
    console.log(`✅ 토픽 구독 성공: ${destination}`);

    return destination;
  }

  /**
   * 구독 취소
   * @param destination 구독 취소할 토픽 경로
   */
  unsubscribe(destination: string): void {
    const subscription = this.subscriptions.get(destination);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(destination);
      console.log(`✅ 구독 취소: ${destination}`);
    }
  }

  /**
   * 모든 구독 취소
   */
  unsubscribeAll(): void {
    this.subscriptions.forEach((subscription) => {
      subscription.unsubscribe();
    });
    this.subscriptions.clear();
    console.log('✅ 모든 구독 취소');
  }

  /**
   * WebSocket 연결 해제
   */
  disconnect(): void {
    if (this.client) {
      this.unsubscribeAll();
      this.client.deactivate();
      this.client = null;
      console.log('✅ WebSocket 연결 해제');
    }
  }

  /**
   * 연결 상태 확인
   */
  isConnected(): boolean {
    return this.client?.connected || false;
  }

  /**
   * 메시지 전송
   * @param destination 메시지를 보낼 경로
   * @param body 전송할 메시지 본문
   */
  send(destination: string, body: any): void {
    if (!this.client?.connected) {
      console.error('❌ WebSocket이 연결되지 않았습니다.');
      return;
    }

    this.client.publish({
      destination,
      body: JSON.stringify(body),
    });

    console.log(`📤 메시지 전송 [${destination}]:`, body);
  }
}

// 싱글톤 인스턴스
let websocketClient: WebSocketClient | null = null;

/**
 * WebSocket 클라이언트 싱글톤 인스턴스 가져오기
 */
export function getWebSocketClient(): WebSocketClient {
  if (!websocketClient) {
    websocketClient = new WebSocketClient();
  }
  return websocketClient;
}
