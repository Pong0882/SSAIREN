import { useState, useEffect, useCallback } from "react";
import {
  Header,
  Tabs,
  TabButton,
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableCell,
  Pagination,
  Modal,
} from "@/components";
import { useAuthStore } from "@/features/auth/store/authStore";
import {
  fetchPatientsApi,
  fetchPatientDetailApi,
  completePatientArrivalApi,
  type PatientDetailResponse,
} from "@/features/patients/api/patientApi";
import type { Patient } from "@/features/patients/types/patient.types";

export default function PatientListPage() {
  const { user } = useAuthStore();
  const [activeTab, setActiveTab] = useState<"all" | "waiting">("all");
  const [dateRange, setDateRange] = useState<"all" | "week" | "month">("all");
  const [currentPage, setCurrentPage] = useState(1);
  const [patients, setPatients] = useState<Patient[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const itemsPerPage = 10;

  // 상세 정보 모달 상태
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [selectedPatient, setSelectedPatient] = useState<
    PatientDetailResponse["data"] | null
  >(null);
  const [selectedPatientStatus, setSelectedPatientStatus] =
    useState<string>("");
  const [detailLoading, setDetailLoading] = useState(false);
  const [isExpanded, setIsExpanded] = useState(false);

  // API 호출 함수
  const fetchPatients = useCallback(async () => {
    if (!user?.id) {
      setError("로그인 정보가 없습니다.");
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const result = await fetchPatientsApi({
        hospitalId: user.id,
        page: currentPage,
        size: itemsPerPage,
        status: activeTab === "all" ? "ALL" : "ACCEPTED",
        dateRange: dateRange,
      });

      console.log("result ", result);

      setPatients(result.patients);
      setTotalPages(result.totalPages);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "환자 목록을 불러오는데 실패했습니다."
      );
      console.error("환자 목록 조회 실패:", err);
    } finally {
      setLoading(false);
    }
  }, [user?.id, currentPage, itemsPerPage, activeTab, dateRange]);

  // 페이지 로드 시 & 탭/페이지 변경 시 데이터 가져오기
  useEffect(() => {
    fetchPatients();
  }, [fetchPatients]);

  // WebSocket 메시지 수신 시 환자 목록 새로고침
  useEffect(() => {
    const handleNewPatientRequest = (event: Event) => {
      const customEvent = event as CustomEvent;
      console.log("🔔 PatientListPage: 새로운 요청 감지", customEvent.detail);
      fetchPatients();
    };

    const handlePatientRequestHandled = () => {
      console.log("✅ PatientListPage: 요청 처리 완료, 테이블 새로고침");
      fetchPatients();
    };

    window.addEventListener("newPatientRequest", handleNewPatientRequest);
    window.addEventListener(
      "patientRequestHandled",
      handlePatientRequestHandled
    );

    return () => {
      window.removeEventListener("newPatientRequest", handleNewPatientRequest);
      window.removeEventListener(
        "patientRequestHandled",
        handlePatientRequestHandled
      );
    };
  }, [fetchPatients]);

  const handleTabChange = (tab: "all" | "waiting") => {
    setActiveTab(tab);
    setCurrentPage(1); // 탭 변경 시 첫 페이지로
  };

  const handleDateRangeChange = (range: "all" | "week" | "month") => {
    setDateRange(range);
    setCurrentPage(1); // 날짜 범위 변경 시 첫 페이지로
  };

  // 환자 행 클릭 핸들러
  const handlePatientClick = async (patient: Patient) => {
    if (!user?.id) return;

    setDetailLoading(true);
    setIsDetailModalOpen(true);
    setIsExpanded(false);
    setSelectedPatientStatus(patient.status); // 환자 상태 저장

    try {
      const response = await fetchPatientDetailApi(
        user.id,
        patient.emergencyReportId
      );
      setSelectedPatient(response.data);
    } catch (err) {
      console.error("환자 상세 정보 조회 실패:", err);
      alert("환자 상세 정보를 불러오는데 실패했습니다.");
      setIsDetailModalOpen(false);
    } finally {
      setDetailLoading(false);
    }
  };

  // 모달 닫기
  const handleCloseModal = () => {
    setIsDetailModalOpen(false);
    setSelectedPatient(null);
    setSelectedPatientStatus("");
    setIsExpanded(false);
    // 모달이 닫힐 때 테이블 새로고침
    fetchPatients();
  };

  // 내원완료 처리
  const handleCompleteVisit = async () => {
    if (!selectedPatient || !user?.id) return;

    try {
      console.log("🏥 내원완료 버튼 클릭:", selectedPatient.emergencyReportId);

      const result = await completePatientArrivalApi(
        user.id,
        selectedPatient.emergencyReportId
      );

      console.log("🏥 내원완료 성공:", result);

      // 성공 시 모달 닫기 및 목록 새로고침
      handleCloseModal();
      fetchPatients();

      alert("내원완료 처리되었습니다.");
    } catch (error) {
      console.error("❌ 내원완료 실패:", error);
      alert("내원완료 처리에 실패했습니다. 다시 시도해주세요.");
    }
  };

  // recordTime을 문자열로 포맷하는 헬퍼 함수
  const formatRecordTime = (recordTime: Patient["recordTime"] | string) => {
    // 문자열인 경우
    if (typeof recordTime === "string") {
      // "2025-11-05T09:20:00" 형식인 경우 "T"를 공백으로 변경
      return recordTime.replace("T", " ");
    }

    // 객체인 경우
    if (recordTime && typeof recordTime === "object" && "hour" in recordTime) {
      const { hour, minute, second } = recordTime;
      return `${String(hour).padStart(2, "0")}:${String(minute).padStart(
        2,
        "0"
      )}:${String(second).padStart(2, "0")}`;
    }

    return "-";
  };

  return (
    <div className="h-screen bg-neutral-900 flex flex-col overflow-hidden">
      {/* Header */}
      <Header />

      {/* Main Content */}
      <div className="flex-1 flex flex-col p-4 sm:p-6 lg:p-8 min-h-0">
        <div className="max-w-7xl mx-auto w-full flex flex-col h-full">
          {/* 탭 버튼 */}
          <div className="mb-4 sm:mb-6 flex-shrink-0 flex items-center justify-between gap-4">
            {/* 내원 상태 탭 (왼쪽) */}
            <Tabs>
              <TabButton
                active={activeTab === "all"}
                onClick={() => handleTabChange("all")}
              >
                전체
              </TabButton>
              <TabButton
                active={activeTab === "waiting"}
                onClick={() => handleTabChange("waiting")}
              >
                내원 대기
              </TabButton>
            </Tabs>

            {/* 날짜 범위 탭 (오른쪽) - 작은 버튼 */}
            <div className="flex gap-2">
              <button
                className={`px-3 py-1.5 text-sm font-medium rounded-lg transition-colors ${
                  dateRange === "all"
                    ? "bg-secondary-500 text-white"
                    : "bg-white text-neutral-700 hover:bg-neutral-100"
                }`}
                onClick={() => handleDateRangeChange("all")}
              >
                전체
              </button>
              <button
                className={`px-3 py-1.5 text-sm font-medium rounded-lg transition-colors ${
                  dateRange === "week"
                    ? "bg-secondary-500 text-white"
                    : "bg-white text-neutral-700 hover:bg-neutral-100"
                }`}
                onClick={() => handleDateRangeChange("week")}
              >
                최근 일주일
              </button>
              <button
                className={`px-3 py-1.5 text-sm font-medium rounded-lg transition-colors ${
                  dateRange === "month"
                    ? "bg-secondary-500 text-white"
                    : "bg-white text-neutral-700 hover:bg-neutral-100"
                }`}
                onClick={() => handleDateRangeChange("month")}
              >
                최근 한 달
              </button>
            </div>
          </div>
          {/* 테이블 */}
          <div className="bg-white rounded-lg shadow-lg overflow-hidden flex-1">
            {loading ? (
              <div className="flex items-center justify-center h-64">
                <p className="text-neutral-500">로딩 중...</p>
              </div>
            ) : error ? (
              <div className="flex items-center justify-center h-64">
                <p className="text-danger-500">{error}</p>
              </div>
            ) : patients.length === 0 ? (
              <div className="flex items-center justify-center h-64">
                <p className="text-neutral-500">환자 데이터가 없습니다.</p>
              </div>
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableCell header className="w-[8%]">
                      No
                    </TableCell>
                    <TableCell header className="w-[8%]">
                      성별
                    </TableCell>
                    <TableCell header className="w-[8%]">
                      나이
                    </TableCell>
                    <TableCell header className="w-[12%]">
                      시간
                    </TableCell>
                    <TableCell header className="w-[30%]">
                      주호소
                    </TableCell>
                    <TableCell header className="w-[12%]">
                      멘탈
                    </TableCell>
                    <TableCell header className="w-[12%]">
                      내원 여부
                    </TableCell>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {patients.map((patient) => (
                    <TableRow
                      key={patient.hospitalSelectionId}
                      variant={
                        patient.status === "ACCEPTED" ? "alert" : "default"
                      }
                      onClick={() => handlePatientClick(patient)}
                    >
                      <TableCell className="w-[8%]">
                        {patient.hospitalSelectionId}
                      </TableCell>
                      <TableCell className="w-[8%]">{patient.gender}</TableCell>
                      <TableCell className="w-[8%]">{patient.age}</TableCell>
                      <TableCell className="w-[12%]">
                        {formatRecordTime(patient.recordTime)}
                      </TableCell>
                      <TableCell className="w-[30%]">
                        {patient.chiefComplaint}
                      </TableCell>
                      <TableCell className="w-[12%]">
                        {patient.mentalStatus}
                      </TableCell>
                      <TableCell className="w-[12%]">
                        {patient.status === "ACCEPTED"
                          ? "내원 대기"
                          : "내원 완료"}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            )}
          </div>

          {/* 페이지네이션 */}
          <div className="mt-3 flex-shrink-0">
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={setCurrentPage}
            />
          </div>
        </div>
      </div>

      {/* 환자 상세 정보 모달 */}
      <Modal
        isOpen={isDetailModalOpen}
        onClose={handleCloseModal}
        size="md"
        showCloseButton={false}
        closeOnOverlayClick={true}
        closeOnEscape={true}
      >
        {detailLoading ? (
          <div className="flex items-center justify-center py-8">
            <p className="text-neutral-500">로딩 중...</p>
          </div>
        ) : selectedPatient ? (
          <div className="px-10 py-1">
            {/* 헤더 */}
            <h2 className="text-2xl font-bold text-neutral-800 mb-4">
              환자 정보
            </h2>

            {/* 기본 정보 (항상 표시) */}
            <div className="space-y-2">
              {/* 성별, 나이 */}
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block text-xs text-neutral-700 mb-0.5">
                    성별 <span className="text-danger-500">*</span>
                  </label>
                  <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                    {selectedPatient.gender === "M" ? "남성" : "여성"}
                  </div>
                </div>
                <div>
                  <label className="block text-xs text-neutral-700 mb-0.5">
                    나이 <span className="text-danger-500">*</span>
                  </label>
                  <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                    {selectedPatient.age}
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
                    {selectedPatient.recordTime?.replace("T", " ") ||
                      selectedPatient.recordTime}
                  </div>
                </div>
                <div>
                  <label className="block text-xs text-neutral-700 mb-0.5">
                    멘탈 <span className="text-danger-500">*</span>
                  </label>
                  <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                    {selectedPatient.mentalStatus}
                  </div>
                </div>
              </div>

              {/* 주호소 */}
              <div>
                <label className="block text-xs text-neutral-700 mb-0.5">
                  주호소 <span className="text-danger-500">*</span>
                </label>
                <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                  {selectedPatient.chiefComplaint}
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
                      {selectedPatient.hr}
                    </div>
                  </div>
                  <div>
                    <label className="block text-xs text-neutral-700 mb-0.5">
                      BP <span className="text-danger-500">*</span>
                    </label>
                    <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                      {selectedPatient.bp}
                    </div>
                  </div>
                  <div>
                    <label className="block text-xs text-neutral-700 mb-0.5">
                      SpO2 <span className="text-danger-500">*</span>
                    </label>
                    <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                      {selectedPatient.spo2}
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
                      {selectedPatient.rr}
                    </div>
                  </div>
                  <div>
                    <label className="block text-xs text-neutral-700 mb-0.5">
                      BT <span className="text-danger-500">*</span>
                    </label>
                    <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                      {selectedPatient.bt}
                    </div>
                  </div>
                  <div>
                    <label className="block text-xs text-neutral-700 mb-0.5">
                      보호자 유무 <span className="text-danger-500">*</span>
                    </label>
                    <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                      {selectedPatient.hasGuardian ? "유" : "무"}
                    </div>
                  </div>
                </div>

                {/* Hx */}
                <div>
                  <label className="block text-xs text-neutral-700 mb-0.5">
                    Hx <span className="text-danger-500">*</span>
                  </label>
                  <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                    {selectedPatient.hx}
                  </div>
                </div>

                {/* 발병 시간, LNT */}
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="block text-xs text-neutral-700 mb-0.5">
                      발병 시간 <span className="text-danger-500">*</span>
                    </label>
                    <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                      {selectedPatient.onsetTime?.replace("T", " ") ||
                        selectedPatient.onsetTime}
                    </div>
                  </div>
                  <div>
                    <label className="block text-xs text-neutral-700 mb-0.5">
                      LNT <span className="text-danger-500">*</span>
                    </label>
                    <div className="bg-neutral-100 px-3 py-1.5 rounded text-sm text-neutral-800">
                      {selectedPatient.lnt?.replace("T", " ") ||
                        selectedPatient.lnt}
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* 하단 버튼 */}
            {selectedPatientStatus === "ACCEPTED" ? (
              // 내원 대기 상태: 닫기 + 내원완료 버튼
              <div className="grid grid-cols-2 gap-3 mt-6">
                <button
                  onClick={handleCloseModal}
                  className="px-3 py-2 bg-neutral-500 text-white rounded-lg font-semibold hover:bg-neutral-600 transition-colors"
                >
                  닫기
                </button>
                <button
                  onClick={handleCompleteVisit}
                  className="px-3 py-2 bg-primary-500 text-white rounded-lg font-semibold hover:bg-blue-600 transition-colors"
                >
                  내원완료
                </button>
              </div>
            ) : (
              // 내원 완료 상태: 닫기 버튼만 중앙에 표시
              <div className="flex justify-center mt-6">
                <button
                  onClick={handleCloseModal}
                  className="w-1/2 px-3 py-2 bg-neutral-500 text-white rounded-lg font-semibold hover:bg-neutral-600 transition-colors"
                >
                  닫기
                </button>
              </div>
            )}
          </div>
        ) : null}
      </Modal>
    </div>
  );
}
