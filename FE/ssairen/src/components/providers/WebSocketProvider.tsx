import { ReactNode, useCallback, useEffect, useState } from "react";
import { useAuthStore } from "@/features/auth/store/authStore";
import { useHospitalWebSocket } from "@/features/patients/hooks/useHospitalWebSocket";
import { Modal } from "@/components";

interface WebSocketProviderProps {
  children: ReactNode;
}

/**
 * WebSocket 연결을 관리하는 Provider
 * 로그인된 사용자에게만 WebSocket 연결을 제공합니다.
 */
export function WebSocketProvider({ children }: WebSocketProviderProps) {
  const { isAuthenticated } = useAuthStore();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [requestData, setRequestData] = useState<any>(null);

  // 브라우저 알림 권한 요청
  useEffect(() => {
    if (isAuthenticated && Notification.permission === "default") {
      Notification.requestPermission().then((permission) => {
        console.log("알림 권한:", permission);
      });
    }
  }, [isAuthenticated]);

  // WebSocket으로 새로운 수용 요청 수신
  const handleNewRequest = useCallback((request: any) => {
    console.log("🚨 [전역] 새로운 수용 요청 수신:", request);

    // 모달 표시
    setRequestData(request);
    setIsModalOpen(true);

    // 커스텀 이벤트 발생 - 다른 컴포넌트에서 리스닝 가능
    window.dispatchEvent(
      new CustomEvent("newPatientRequest", { detail: request })
    );

    // 브라우저 알림 표시
    if (Notification.permission === "granted") {
      new Notification("새로운 수용 요청", {
        body: `환자 정보: ${request.patientInfo?.age}세 / ${request.patientInfo?.gender}`,
        icon: "/favicon.ico",
        badge: "/favicon.ico",
        tag: "patient-request",
        requireInteraction: true,
      });
    }
  }, []);

  // Hook은 항상 호출되어야 함 (조건문 밖에서)
  useHospitalWebSocket({
    onNewRequest: isAuthenticated ? handleNewRequest : undefined,
    onError: (error) => {
      console.error("❌ WebSocket 에러:", error);
    },
  });

  return (
    <>
      {children}

      {/* 수용 요청 알림 모달 */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        size="md"
      >
        <div className="p-6">
          <h2 className="text-2xl font-bold text-primary-500 mb-4">
            🚨 새로운 수용 요청
          </h2>

          {requestData && (
            <div className="space-y-3">
              <div className="bg-neutral-50 p-4 rounded-lg">
                <p className="text-sm text-neutral-500 mb-1">환자 정보</p>
                <p className="text-lg font-semibold">
                  {requestData.patientInfo?.age}세 / {requestData.patientInfo?.gender}
                </p>
              </div>

              <div className="bg-neutral-50 p-4 rounded-lg">
                <p className="text-sm text-neutral-500 mb-1">주호소</p>
                <p className="text-lg">{requestData.patientInfo?.chiefComplaint || '-'}</p>
              </div>

              <div className="bg-neutral-50 p-4 rounded-lg">
                <p className="text-sm text-neutral-500 mb-1">의식 상태</p>
                <p className="text-lg">{requestData.patientInfo?.mentalStatus || '-'}</p>
              </div>

              <div className="bg-neutral-50 p-4 rounded-lg">
                <p className="text-sm text-neutral-500 mb-1">응급신고 ID</p>
                <p className="text-lg">{requestData.emergencyReportId}</p>
              </div>
            </div>
          )}

          <div className="mt-6 flex gap-3">
            <button
              onClick={() => setIsModalOpen(false)}
              className="flex-1 px-4 py-3 bg-primary-500 text-white rounded-lg font-semibold hover:bg-primary-600 transition-colors"
            >
              확인
            </button>
          </div>
        </div>
      </Modal>
    </>
  );
}
