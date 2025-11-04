import { ReactNode, useCallback, useEffect, useState } from "react";
import { useAuthStore } from "@/features/auth/store/authStore";
import { useHospitalWebSocket } from "@/features/patients/hooks/useHospitalWebSocket";
import { Modal } from "@/components";
import leftArrow from "@/assets/left-arrow.png";
import rightArrow from "@/assets/right-arrow.png";

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
  const [requestQueue, setRequestQueue] = useState<any[]>([]); // 요청 배열
  const [currentIndex, setCurrentIndex] = useState(0); // 현재 보고 있는 인덱스
  const [isExpanded, setIsExpanded] = useState(false); // 모달 펼침/접힘 상태

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

    // 요청 배열에 추가 (늦게 온 요청이 뒤에 추가됨)
    setRequestQueue((prev) => [...prev, request]);
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

  // 이전 요청으로 이동
  const handlePrevious = () => {
    setCurrentIndex((prev) => Math.max(0, prev - 1));
  };

  // 다음 요청으로 이동
  const handleNext = () => {
    setCurrentIndex((prev) => Math.min(requestQueue.length - 1, prev + 1));
  };

  // 현재 요청 삭제
  const handleCloseCurrentRequest = () => {
    setRequestQueue((prev) => {
      const newQueue = prev.filter((_, index) => index !== currentIndex);

      // 삭제 후 배열이 비었으면 모달 닫기
      if (newQueue.length === 0) {
        setIsModalOpen(false);
        setCurrentIndex(0);
        return [];
      }

      // 마지막 요청을 삭제한 경우 인덱스 조정
      if (currentIndex >= newQueue.length) {
        setCurrentIndex(newQueue.length - 1);
      }

      return newQueue;
    });
  };

  // 모든 요청 닫기
  const handleCloseAll = () => {
    setRequestQueue([]);
    setCurrentIndex(0);
    setIsModalOpen(false);
    setIsExpanded(false);
  };

  const currentRequest = requestQueue[currentIndex];

  return (
    <>
      {children}

      {/* 수용 요청 알림 모달 - 캐러셀 형태 */}
      <Modal
        isOpen={isModalOpen}
        onClose={handleCloseAll}
        size="md"
        showCloseButton={false}
      >
        <div className="relative">
          {/* 좌측 화살표 */}
          <button
            onClick={handlePrevious}
            disabled={currentIndex === 0}
            className="absolute left-0.5 top-1/2 -translate-y-1/2 z-10 disabled:opacity-30 disabled:cursor-not-allowed transition-opacity"
          >
            <img src={leftArrow} alt="이전" className="w-5 h-5" />
          </button>

          {/* 우측 화살표 */}
          <button
            onClick={handleNext}
            disabled={currentIndex === requestQueue.length - 1}
            className="absolute right-0.5 top-1/2 -translate-y-1/2 z-10 disabled:opacity-30 disabled:cursor-not-allowed transition-opacity"
          >
            <img src={rightArrow} alt="다음" className="w-5 h-5" />
          </button>

          <div className="px-10 py-1">
            {/* 헤더 */}
            <h2 className="text-2xl font-bold text-neutral-800 mb-4">
              응급 환자
            </h2>

            {currentRequest && (
              <>
                {/* 기본 정보 (항상 표시) */}
                <div className="space-y-2">
                  {/* 성별, 나이 */}
                  <div className="grid grid-cols-2 gap-2">
                    <div>
                      <label className="block text-xs text-neutral-700 mb-0.5">
                        성별 <span className="text-danger-500">*</span>
                      </label>
                      <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                        {currentRequest.patientInfo?.gender || "-"}
                      </div>
                    </div>
                    <div>
                      <label className="block text-xs text-neutral-700 mb-0.5">
                        나이 <span className="text-danger-500">*</span>
                      </label>
                      <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                        {currentRequest.patientInfo?.age || "-"}
                      </div>
                    </div>
                  </div>

                  {/* 시간, 멘탈 */}
                  <div className="grid grid-cols-2 gap-2">
                    <div>
                      <label className="block text-xs text-neutral-700 mb-0.5">
                        시간 <span className="text-danger-500">*</span>
                      </label>
                      <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                        {currentRequest.patientInfo?.recordTime
                          ?.split("T")[1]
                          ?.substring(0, 5) ||
                          currentRequest.patientInfo?.recordTime ||
                          "-"}
                      </div>
                    </div>
                    <div>
                      <label className="block text-xs text-neutral-700 mb-0.5">
                        멘탈 <span className="text-danger-500">*</span>
                      </label>
                      <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                        {currentRequest.patientInfo?.mentalStatus || "-"}
                      </div>
                    </div>
                  </div>

                  {/* 주호소 */}
                  <div>
                    <label className="block text-xs text-neutral-700 mb-0.5">
                      주호소 <span className="text-danger-500">*</span>
                    </label>
                    <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                      {currentRequest.patientInfo?.chiefComplaint ||
                        "환자 주호소 내용"}
                    </div>
                  </div>
                </div>

                {/* 펼침/접기 버튼 */}
                {!isExpanded && (
                  <div className="flex justify-center my-3">
                    <button
                      onClick={() => setIsExpanded(!isExpanded)}
                      className="w-7 h-7 flex items-center justify-center rounded-full bg-neutral-200 hover:bg-neutral-300 transition-colors"
                    >
                      <svg
                        className="w-4 h-4"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M19 9l-7 7-7-7"
                        />
                      </svg>
                    </button>
                  </div>
                )}

                {/* 펼쳐졌을 때 추가 정보 */}
                {isExpanded && (
                  <div className="space-y-2 border-t border-neutral-200 pt-3 my-5 relative">
                    {/* 접기 버튼을 구분선 위에 배치 */}
                    <button
                      onClick={() => setIsExpanded(false)}
                      className="absolute -top-3.5 left-1/2 -translate-x-1/2 w-7 h-7 flex items-center justify-center rounded-full bg-neutral-200 hover:bg-neutral-300 transition-colors"
                    >
                      <svg
                        className="w-4 h-4"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M5 15l7-7 7 7"
                        />
                      </svg>
                    </button>

                    {/* HR, BP, SpO2 */}
                    <div className="grid grid-cols-3 gap-2">
                      <div>
                        <label className="block text-xs text-neutral-700 mb-0.5">
                          HR <span className="text-danger-500">*</span>
                        </label>
                        <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                          {currentRequest.patientInfo?.hr || "000"}
                        </div>
                      </div>
                      <div>
                        <label className="block text-xs text-neutral-700 mb-0.5">
                          BP <span className="text-danger-500">*</span>
                        </label>
                        <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                          {currentRequest.patientInfo?.bp || "000"}
                        </div>
                      </div>
                      <div>
                        <label className="block text-xs text-neutral-700 mb-0.5">
                          SpO2 <span className="text-danger-500">*</span>
                        </label>
                        <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                          {currentRequest.patientInfo?.spo2 || "000"}
                        </div>
                      </div>
                    </div>

                    {/* RR, BT, 보호자 유무 */}
                    <div className="grid grid-cols-3 gap-2">
                      <div>
                        <label className="block text-xs text-neutral-700 mb-0.5">
                          RR <span className="text-danger-500">*</span>
                        </label>
                        <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                          {currentRequest.patientInfo?.rr || "000"}
                        </div>
                      </div>
                      <div>
                        <label className="block text-xs text-neutral-700 mb-0.5">
                          BT <span className="text-danger-500">*</span>
                        </label>
                        <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                          {currentRequest.patientInfo?.bt || "000"}
                        </div>
                      </div>
                      <div>
                        <label className="block text-xs text-neutral-700 mb-0.5">
                          보호자 유무 <span className="text-danger-500">*</span>
                        </label>
                        <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                          {currentRequest.patientInfo?.hasGuardian
                            ? "유"
                            : "무"}
                        </div>
                      </div>
                    </div>

                    {/* Hx */}
                    <div>
                      <label className="block text-xs text-neutral-700 mb-0.5">
                        Hx <span className="text-danger-500">*</span>
                      </label>
                      <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                        {currentRequest.patientInfo?.hx || "환자 과거력 내용"}
                      </div>
                    </div>

                    {/* 발병 시간, LNT */}
                    <div className="grid grid-cols-2 gap-2">
                      <div>
                        <label className="block text-xs text-neutral-700 mb-0.5">
                          발병 시간 <span className="text-danger-500">*</span>
                        </label>
                        <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                          {currentRequest.patientInfo?.onsetTime || "09:55"}
                        </div>
                      </div>
                      <div>
                        <label className="block text-xs text-neutral-700 mb-0.5">
                          LNT <span className="text-danger-500">*</span>
                        </label>
                        <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                          {currentRequest.patientInfo?.lnt || "09:55"}
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* 하단 버튼 */}
                <div className="grid grid-cols-3 gap-3 mt-6">
                  <button className="px-3 py-2 bg-primary-500 text-white rounded-lg font-semibold hover:bg-blue-600 transition-colors">
                    수용가능
                  </button>
                  <button
                    onClick={handleCloseCurrentRequest}
                    className="px-3 py-2 bg-neutral-500 text-white rounded-lg font-semibold hover:bg-neutral-600 transition-colors"
                  >
                    거절
                  </button>
                  <button className="px-3 py-2 bg-secondary-500 text-white rounded-lg font-semibold hover:bg-orange-600 transition-colors">
                    전화요망
                  </button>
                </div>

                {/* 인디케이터 */}
                <div className="text-center mt-4">
                  <span className="text-sm font-semibold text-primary-600 bg-primary-50 px-3 py-1 rounded-full">
                    {currentIndex + 1} / {requestQueue.length}
                  </span>
                </div>
              </>
            )}
          </div>
        </div>
      </Modal>
    </>
  );
}
