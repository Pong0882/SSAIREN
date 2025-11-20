import { ReactNode, useCallback, useEffect, useState } from "react";
import { useAuthStore } from "@/features/auth/store/authStore";
import { useHospitalWebSocket } from "@/features/patients/hooks/useHospitalWebSocket";
import { getWebSocketClient } from "@/lib/websocketClient";
import { Modal } from "@/components";
import leftArrow from "@/assets/left-arrow.png";
import rightArrow from "@/assets/right-arrow.png";
import {
  acceptPatientApi,
  rejectPatientApi,
  callRequestApi,
} from "@/features/patients/api/patientApi";

interface WebSocketProviderProps {
  children: ReactNode;
}

/**
 * WebSocket 연결을 관리하는 Provider
 * 로그인된 사용자에게만 WebSocket 연결을 제공합니다.
 */
const STORAGE_KEY = "pendingPatientRequests";

export function WebSocketProvider({ children }: WebSocketProviderProps) {
  const { isAuthenticated } = useAuthStore();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [requestQueue, setRequestQueue] = useState<any[]>([]); // 요청 배열
  const [currentIndex, setCurrentIndex] = useState(0); // 현재 보고 있는 인덱스
  const [isExpanded, setIsExpanded] = useState(false); // 모달 펼침/접힘 상태
  const [showToast, setShowToast] = useState(false); // 토스트 알림 표시 여부

  // 페이지 로드 시 LocalStorage에서 미처리 요청 복구
  useEffect(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (!saved) return;
    try {
      const parsed = JSON.parse(saved);
      if (Array.isArray(parsed) && parsed.length > 0) {
        console.log("📦 LocalStorage에서 미처리 요청 복구:", parsed);
        setRequestQueue(parsed);
        setIsModalOpen(true);
      }
    } catch (e) {
      console.error("❌ LocalStorage 복구 실패:", e);
      localStorage.removeItem(STORAGE_KEY);
    }
  }, []);

  // requestQueue 변경 시 LocalStorage 동기화
  useEffect(() => {
    if (requestQueue.length > 0) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(requestQueue));
      console.log("💾 LocalStorage에 요청 저장:", requestQueue.length);
    } else {
      localStorage.removeItem(STORAGE_KEY);
      console.log("🗑️ LocalStorage 비움");
    }
  }, [requestQueue]);

  // 브라우저 알림 권한 요청
  useEffect(() => {
    if (isAuthenticated && Notification.permission === "default") {
      Notification.requestPermission().then((permission) => {
        console.log("알림 권한:", permission);
      });
    }
  }, [isAuthenticated]);

  // 로그아웃 시 모달 닫고 큐 비우기
  useEffect(() => {
    if (!isAuthenticated) {
      setRequestQueue([]);
      setCurrentIndex(0);
      setIsModalOpen(false);
      setIsExpanded(false);
      localStorage.removeItem(STORAGE_KEY);
      console.log("🚪 로그아웃으로 인한 환자 요청 큐 초기화");
    }
  }, [isAuthenticated]);

  // ✅ 공통: id로 현재 요청 제거 (타입과 무관하게 동일 id 모두 제거)
  const removeById = useCallback(
    (hospitalSelectionId: number | string) => {
      setRequestQueue((prev) => {
        const newQueue = prev.filter(
          (r) => r.hospitalSelectionId !== hospitalSelectionId
        );

        if (newQueue.length === 0) {
          // 큐가 비면 모달 닫기 및 상태 초기화
          setIsModalOpen(false);
          setCurrentIndex(0);
          setIsExpanded(false);

          // 테이블 갱신 이벤트
          setTimeout(() => {
            window.dispatchEvent(new CustomEvent("patientRequestHandled"));
            console.log("✅ 테이블 새로고침 이벤트 발생 (큐 비움)");
          }, 0);

          return [];
        }

        // 현재 인덱스 보정
        const newIndex = Math.min(currentIndex, newQueue.length - 1);
        if (newIndex !== currentIndex) setCurrentIndex(newIndex);

        // 상태 업데이트 후 테이블 갱신 이벤트
        setTimeout(() => {
          window.dispatchEvent(new CustomEvent("patientRequestHandled"));
          console.log("✅ 테이블 새로고침 이벤트 발생");
        }, 0);

        return newQueue;
      });
    },
    [currentIndex]
  );

  // WebSocket으로 새로운 수용 요청 수신
  // ✅ 자기 자신이 보낸 응답/알림은 무시 (origin 필드) + request 타입만 큐에 쌓기 + id 중복차단
  const handleNewRequest = useCallback((request: any) => {
    console.log("🚨 [전역] WebSocket 메시지 수신:", request);

    // 내가 방금 보낸 메시지는 무시
    if (request?.origin === "hospital-web") {
      console.log("↩️ 내가 보낸 메시지 무시(origin == hospital-web)");
      return;
    }

    // type이 명시적으로 "request" 또는 "REQUEST"일 때만 큐에 쌓기
    // response, notice 등은 무시 (type이 없는 경우도 무시)
    const incomingType = request?.type?.toUpperCase();
    if (incomingType !== "REQUEST") {
      console.log("↩️ REQUEST 타입이 아니므로 무시 (type:", request?.type, ")");
      return;
    }

    console.log("✅ 환자 수용 요청 → localStorage 큐에 추가");

    setRequestQueue((prev) => {
      const isFirst = prev.length === 0;

      // hospitalSelectionId 기준 중복 차단
      const exists = prev.some(
        (r) => r.hospitalSelectionId === request?.hospitalSelectionId
      );
      if (exists) {
        console.log("↩️ 중복 요청 무시:", request?.hospitalSelectionId);
        return prev;
      }

      if (!isFirst) {
        setShowToast(true);
        setTimeout(() => setShowToast(false), 3000);
      }

      return [...prev, request];
    });

    setIsModalOpen(true);

    // 커스텀 이벤트 발생
    window.dispatchEvent(
      new CustomEvent("newPatientRequest", { detail: request })
    );

    // 브라우저 알림
    if (Notification.permission === "granted") {
      new Notification("새로운 수용 요청", {
        body: `환자 정보: ${request?.patientInfo?.age}세 / ${request?.patientInfo?.gender}`,
        icon: "/favicon.ico",
        badge: "/favicon.ico",
        tag: `patient-request-${request?.hospitalSelectionId}`,
        requireInteraction: true,
      });
    }
  }, []);

  // WebSocket으로 완료 알림 수신 (키 방어적으로 파싱 + id로 삭제)
  const handleCompleted = useCallback(
    (message: any) => {
      const parsedId =
        message?.hospitalSelectionId ?? message?.selectionId ?? message?.id;
      console.log(
        "❌ [전역] 요청 완료 알림 수신:",
        message,
        "→ parsedId:",
        parsedId
      );

      if (parsedId == null) return;

      setRequestQueue((prev) => {
        const newQueue = prev.filter(
          (req) => req.hospitalSelectionId !== parsedId
        );

        if (newQueue.length === 0) {
          setIsModalOpen(false);
          setCurrentIndex(0);
          setIsExpanded(false);
          return [];
        }

        if (currentIndex >= newQueue.length) {
          setCurrentIndex(newQueue.length - 1);
        }

        return newQueue;
      });
    },
    [currentIndex]
  );

  // Hook은 항상 호출되어야 함
  useHospitalWebSocket({
    onNewRequest: isAuthenticated ? handleNewRequest : undefined,
    onCompleted: isAuthenticated ? handleCompleted : undefined,
    onError: (error) => {
      console.error("❌ WebSocket 에러:", error);
    },
  });

  // 이전/다음
  const handlePrevious = () => setCurrentIndex((prev) => Math.max(0, prev - 1));
  const handleNext = () =>
    setCurrentIndex((prev) => Math.min(requestQueue.length - 1, prev + 1));

  // 전체 닫기
  const handleCloseAll = () => {
    setRequestQueue([]);
    setCurrentIndex(0);
    setIsModalOpen(false);
    setIsExpanded(false);

    setTimeout(() => {
      window.dispatchEvent(new CustomEvent("patientRequestHandled"));
      console.log("✅ 테이블 새로고침 이벤트 발생 (전체 닫기)");
    }, 0);
  };

  // 수용가능
  const handleAccept = async () => {
    const id = currentRequest?.hospitalSelectionId;
    if (!id) {
      console.error("❌ hospitalSelectionId가 없습니다.");
      return;
    }

    try {
      console.log("✅ 수용가능 버튼 클릭:", id);
      const result = await acceptPatientApi(id);
      console.log("✅ 수용 성공:", result);

      if (currentRequest?.paramedicId) {
        const wsClient = getWebSocketClient();
        const { user } = useAuthStore.getState();

        // 구급대원 채널로 응답 전송
        const replyChannel = `/topic/paramedic.${currentRequest.paramedicId}`;

        const responseMessage = {
          type: "response",
          status: "ACCEPTED",
          hospitalSelectionId: id,
          emergencyReportId: currentRequest.emergencyReportId,
          hospitalName: user?.officialName || "병원",
          origin: "hospital-web", // ✅ 내가 보낸 응답 표시
        };

        console.log("📤 수용 응답 WebSocket 전송 시작");
        console.log("  → 구급대원 ID:", currentRequest.paramedicId);
        console.log("  → 응답 채널:", replyChannel);
        console.log("  → 메시지:", responseMessage);

        wsClient.send(replyChannel, responseMessage);

        console.log("✅ 구급대원 채널로 WebSocket 전송 완료!");
      } else {
        console.warn(
          "⚠️ paramedicId가 없어서 WebSocket 응답을 보낼 수 없습니다!"
        );
        console.warn("  → currentRequest:", currentRequest);
      }

      removeById(id);

      if (Notification.permission === "granted") {
        new Notification("수용 완료", {
          body: "환자 수용이 완료되었습니다.",
          icon: "/favicon.ico",
        });
      }
    } catch (error) {
      console.error("❌ 수용 실패:", error);
      alert("수용 처리에 실패했습니다. 다시 시도해주세요.");
    }
  };

  // 거절
  const handleReject = async () => {
    const id = currentRequest?.hospitalSelectionId;
    if (!id) {
      console.error("❌ hospitalSelectionId가 없습니다.");
      return;
    }

    try {
      console.log("❌ 거절 버튼 클릭:", id);
      const result = await rejectPatientApi(id);
      console.log("❌ 거절 성공:", result);

      if (currentRequest?.paramedicId) {
        const wsClient = getWebSocketClient();
        const { user } = useAuthStore.getState();

        const replyChannel = `/topic/paramedic.${currentRequest.paramedicId}`;

        const responseMessage = {
          type: "response",
          status: "REJECTED",
          hospitalSelectionId: id,
          emergencyReportId: currentRequest.emergencyReportId,
          hospitalName: user?.officialName || "병원",
          origin: "hospital-web",
        };

        console.log("📤 거절 응답 WebSocket 전송:", replyChannel);
        wsClient.send(replyChannel, responseMessage);
      }

      removeById(id);

      if (Notification.permission === "granted") {
        new Notification("거절 완료", {
          body: "환자 거절이 완료되었습니다.",
          icon: "/favicon.ico",
        });
      }
    } catch (error) {
      console.error("❌ 거절 실패:", error);
      alert("거절 처리에 실패했습니다. 다시 시도해주세요.");
    }
  };

  // 전화요망
  const handleCallRequest = async () => {
    const id = currentRequest?.hospitalSelectionId;
    if (!id) {
      console.error("❌ hospitalSelectionId가 없습니다.");
      return;
    }

    try {
      console.log("📞 전화요망 버튼 클릭:", id);
      const result = await callRequestApi(id);
      console.log("📞 전화요망 성공:", result);

      if (currentRequest?.sourceDestination) {
        const wsClient = getWebSocketClient();
        const { user } = useAuthStore.getState();

        wsClient.send(currentRequest.sourceDestination, {
          type: "response",
          status: "CALL_REQUESTED",
          hospitalSelectionId: id,
          emergencyReportId: currentRequest.emergencyReportId,
          hospitalName: user?.officialName || "병원",
          origin: "hospital-web", // ✅ 내가 보낸 응답 표시
        });
      }

      removeById(id);

      if (Notification.permission === "granted") {
        new Notification("전화요망 완료", {
          body: "전화 요청이 완료되었습니다.",
          icon: "/favicon.ico",
        });
      }
    } catch (error) {
      console.error("❌ 전화요망 실패:", error);
      alert("전화요망 처리에 실패했습니다. 다시 시도해주세요.");
    }
  };

  const currentRequest = requestQueue[currentIndex];

  return (
    <>
      {children}

      {/* 새 요청 토스트 알림 */}
      {showToast && (
        <div className="fixed top-4 left-1/2 -translate-x-1/2 z-[60] animate-slideDown">
          <div className="bg-primary-500 text-white px-6 py-3 rounded-lg shadow-2xl backdrop-blur-md border border-white/30 flex items-center gap-3">
            <svg
              className="w-6 h-6"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
              />
            </svg>
            <span className="font-semibold text-lg">
              새로운 수용 요청이 도착했습니다!
            </span>
          </div>
        </div>
      )}

      {/* 수용 요청 알림 모달 - 캐러셀 형태 */}
      <Modal
        isOpen={isModalOpen}
        onClose={handleCloseAll}
        size="md"
        showCloseButton={false}
        closeOnOverlayClick={false}
        closeOnEscape={false}
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
                {/* 기본 정보 */}
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
                        {currentRequest.patientInfo?.recordTime?.replace(
                          "T",
                          " "
                        ) || "-"}
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
                    {/* 접기 버튼 */}
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
                          {currentRequest.patientInfo?.onsetTime?.replace(
                            "T",
                            " "
                          ) ||
                            currentRequest.patientInfo?.onsetTime ||
                            "-"}
                        </div>
                      </div>
                      <div>
                        <label className="block text-xs text-neutral-700 mb-0.5">
                          LNT <span className="text-danger-500">*</span>
                        </label>
                        <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                          {currentRequest.patientInfo?.lnt?.replace("T", " ") ||
                            currentRequest.patientInfo?.lnt ||
                            "-"}
                        </div>
                      </div>
                    </div>
                  </div>
                )}

                {/* 하단 버튼 */}
                <div className="grid grid-cols-3 gap-3 mt-6">
                  <button
                    onClick={handleAccept}
                    className="px-3 py-2 bg-primary-500 text-white rounded-lg font-semibold hover:bg-blue-600 transition-colors"
                  >
                    수용가능
                  </button>
                  <button
                    onClick={handleReject}
                    className="px-3 py-2 bg-neutral-500 text-white rounded-lg font-semibold hover:bg-neutral-600 transition-colors"
                  >
                    거절
                  </button>
                  <button
                    onClick={handleCallRequest}
                    className="px-3 py-2 bg-secondary-500 text-white rounded-lg font-semibold hover:bg-orange-600 transition-colors"
                  >
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
