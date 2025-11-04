import { useEffect, useRef, useCallback } from 'react';
import { getWebSocketClient } from '@/lib/websocketClient';
import { useAuthStore } from '@/features/auth/store/authStore';

interface EmergencyRequest {
  type?: string;          // 메시지 타입 (선택적)
  hospitalSelectionId: number;
  emergencyReportId: number;
  status?: string;        // COMPLETED 메시지용
  patientInfo?: {
    emergencyReportId: number;
    age: number;
    gender: string;
    chiefComplaint: string;
    mentalStatus: string;
    hr: number;           // Heart Rate
    bp: string;           // Blood Pressure
    spo2: number;         // SpO2
    rr: number;           // Respiratory Rate
    bt: number;           // Body Temperature
    hasGuardian: boolean; // 보호자 유무
    hx: string;           // History
    onsetTime: string;    // 발령 시간
    lnt: string;          // LNT (Last Normal Time)
    recordTime: string;   // 기록 시간
  };
}

interface CompletedMessage {
  type: "COMPLETED";
  hospitalSelectionId: number;
  emergencyReportId: number;
}

interface UseHospitalWebSocketOptions {
  onNewRequest?: ((request: EmergencyRequest) => void) | undefined;
  onCompleted?: ((message: CompletedMessage) => void) | undefined;
  onError?: (error: Error) => void;
}

/**
 * 병원용 WebSocket 훅
 * 로그인한 병원의 채널을 구독하여 실시간 수용 요청을 받습니다.
 */
export function useHospitalWebSocket(options?: UseHospitalWebSocketOptions) {
  const { user } = useAuthStore();
  const wsClient = useRef(getWebSocketClient());
  const isConnecting = useRef(false);

  const { onNewRequest, onCompleted, onError } = options || {};

  // 메시지 핸들러
  const handleMessage = useCallback(
    (message: EmergencyRequest | CompletedMessage) => {
      // COMPLETED 메시지 처리
      if (message.type === 'COMPLETED') {
        console.log('❌ 요청 완료 알림:', message);
        if (onCompleted) {
          onCompleted(message as CompletedMessage);
        }
        return;
      }

      // 새로운 요청 처리
      console.log('🚨 새로운 수용 요청:', message);
      if (onNewRequest) {
        onNewRequest(message as EmergencyRequest);
      }
    },
    [onNewRequest, onCompleted]
  );

  // WebSocket 연결 및 구독
  useEffect(() => {
    if (!user?.id) {
      console.log('⚠️ 사용자 정보가 없어 WebSocket 연결을 건너뜁니다.');
      return;
    }

    // 중복 연결 방지
    if (isConnecting.current) {
      return;
    }

    const connectAndSubscribe = async () => {
      try {
        isConnecting.current = true;

        // 이미 연결되어 있지 않으면 연결
        if (!wsClient.current.isConnected()) {
          console.log('🔌 WebSocket 연결 시도...');
          await wsClient.current.connect();
        }

        // 병원 채널 구독
        const topic = `/topic/hospital.${user.id}`;
        wsClient.current.subscribe(topic, handleMessage);
      } catch (error) {
        console.error('❌ WebSocket 연결 실패:', error);
        onError?.(error as Error);
      } finally {
        isConnecting.current = false;
      }
    };

    connectAndSubscribe();

    // 컴포넌트 언마운트 시 구독 취소 (연결은 유지)
    return () => {
      if (user?.id) {
        const topic = `/topic/hospital.${user.id}`;
        wsClient.current.unsubscribe(topic);
      }
    };
  }, [user?.id, handleMessage, onError]);

  return {
    isConnected: wsClient.current.isConnected(),
    disconnect: () => wsClient.current.disconnect(),
  };
}
