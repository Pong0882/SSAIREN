import { useState, useEffect, useCallback, useMemo, forwardRef } from "react";
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
import Sidebar from "@/components/layout/Sidebar";
import { useAuthStore } from "@/features/auth/store/authStore";
import {
  fetchPatientsApi,
  fetchPatientDetailApi,
  completePatientArrivalApi,
  type PatientDetailResponse,
} from "@/features/patients/api/patientApi";
import type { Patient } from "@/features/patients/types/patient.types";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

// DatePicker customInput용 공통 버튼
const DateButton = forwardRef<HTMLButtonElement, React.ComponentProps<"button">>(
  ({ children, className = "", ...props }, ref) => (
    <button
      ref={ref}
      type="button"
      className={`h-14 w-full px-5 text-base leading-none font-medium rounded-lg transition-colors flex items-center justify-between gap-4 min-w-[200px] ${className}`}
      {...props}
    >
      {children}
    </button>
  )
);
DateButton.displayName = "DateButton";

type RangeKey = "week" | "month" | "all" | "custom";

export default function PatientListPage() {
  const { user } = useAuthStore();
  const [activeTab, setActiveTab] = useState<"all" | "waiting">("all");
  const [dateRange, setDateRange] = useState<RangeKey>("all");
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [endDate, setEndDate] = useState<Date | null>(null);
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
        dateRange: dateRange === "custom" ? "all" : dateRange,
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

  // 유틸
  const fmt = (d: Date) => d.toISOString().split("T")[0];

  const calcPresetRange = (key: Exclude<RangeKey, "custom">) => {
    const end = new Date();
    const start = new Date(end);
    if (key === "week") start.setDate(end.getDate() - 7);
    else if (key === "month") start.setMonth(end.getMonth() - 1);
    else start.setFullYear(end.getFullYear() - 1); // all
    start.setHours(0, 0, 0, 0);
    end.setHours(23, 59, 59, 999);
    return { start, end };
  };

  const applyPreset = (key: Exclude<RangeKey, "custom">) => {
    const { start, end } = calcPresetRange(key);
    setDateRange(key);
    setStartDate(start);
    setEndDate(end);
    setCurrentPage(1);
  };

  const handleTabChange = (tab: "all" | "waiting") => {
    setActiveTab(tab);
    setCurrentPage(1);
  };

  const isCustomActive = dateRange === "custom" && !!startDate && !!endDate;

  // 최초 마운트 시 기본(all) 날짜 설정
  useEffect(() => {
    if (startDate && endDate) return;
    const { start, end } = calcPresetRange("all");
    setStartDate(start);
    setEndDate(end);
  }, []); // once

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

  // 통계 데이터 계산
  const statistics = useMemo(() => {
    const totalCount = totalPages * itemsPerPage;
    const waitingCount = patients.filter(p => p.status === "ACCEPTED").length;
    const completedCount = patients.filter(p => p.status === "ARRIVED").length;
    const maleCount = patients.filter(p => p.gender === "M").length;
    const femaleCount = patients.filter(p => p.gender === "F").length;

    return {
      total: totalCount,
      waiting: waitingCount,
      completed: completedCount,
      male: maleCount,
      female: femaleCount,
      maleRatio: patients.length > 0 ? Math.round((maleCount / patients.length) * 100) : 0,
      femaleRatio: patients.length > 0 ? Math.round((femaleCount / patients.length) * 100) : 0,
    };
  }, [patients, totalPages, itemsPerPage]);

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
    <div className="h-screen flex overflow-hidden">
      <Sidebar />

      {/* 메인 컨텐츠 */}
      <div className="flex-1 flex flex-col overflow-hidden bg-gray-50">
        {/* Header */}
        <Header />

      {/* Main Content */}
      <div className="flex-1 flex flex-col p-4 sm:p-6 lg:p-8 min-h-0">
        <div className="max-w-7xl mx-auto w-full flex flex-col h-full">
          {/* 통계 카드 섹션 */}
          <div className="grid grid-cols-4 gap-3 mb-4 flex-shrink-0">
            {/* 총 환자 수 */}
            <div className="bg-gradient-to-br from-blue-50 to-sky-100 rounded-lg p-3 border border-blue-200 shadow-sm">
              <div className="flex items-center justify-between mb-1">
                <div className="text-xs font-medium text-blue-700">총 환자 수</div>
                <div className="w-8 h-8 rounded-full bg-blue-500 bg-opacity-20 flex items-center justify-center">
                  <svg className="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                  </svg>
                </div>
              </div>
              <div className="text-2xl font-bold text-blue-900">{statistics.total}</div>
              <div className="text-[10px] text-blue-600 mt-0.5">전체 기록</div>
            </div>

            {/* 내원 대기 */}
            <div className="bg-gradient-to-br from-amber-50 to-yellow-100 rounded-lg p-3 border border-amber-200 shadow-sm">
              <div className="flex items-center justify-between mb-1">
                <div className="text-xs font-medium text-amber-700">내원 대기</div>
                <div className="w-8 h-8 rounded-full bg-amber-500 bg-opacity-20 flex items-center justify-center">
                  <svg className="w-4 h-4 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </div>
              <div className="text-2xl font-bold text-amber-900">{statistics.waiting}</div>
              <div className="text-[10px] text-amber-600 mt-0.5">현재 페이지 기준</div>
            </div>

            {/* 내원 완료 */}
            <div className="bg-gradient-to-br from-green-50 to-emerald-100 rounded-lg p-3 border border-green-200 shadow-sm">
              <div className="flex items-center justify-between mb-1">
                <div className="text-xs font-medium text-green-700">내원 완료</div>
                <div className="w-8 h-8 rounded-full bg-green-500 bg-opacity-20 flex items-center justify-center">
                  <svg className="w-4 h-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
              </div>
              <div className="text-2xl font-bold text-green-900">{statistics.completed}</div>
              <div className="text-[10px] text-green-600 mt-0.5">현재 페이지 기준</div>
            </div>

            {/* 성별 분포 */}
            <div className="bg-gradient-to-br from-purple-50 to-pink-100 rounded-lg p-3 border border-purple-200 shadow-sm">
              <div className="flex items-center justify-between mb-1">
                <div className="text-xs font-medium text-purple-700">성별 분포</div>
                <div className="w-8 h-8 rounded-full bg-purple-500 bg-opacity-20 flex items-center justify-center">
                  <svg className="w-4 h-4 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 3.055A9.001 9.001 0 1020.945 13H11V3.055z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20.488 9H15V3.512A9.025 9.025 0 0120.488 9z" />
                  </svg>
                </div>
              </div>
              <div className="flex items-baseline gap-2">
                <div className="text-base font-bold text-purple-900">남 {statistics.maleRatio}%</div>
                <div className="text-base font-bold text-pink-900">여 {statistics.femaleRatio}%</div>
              </div>
              <div className="flex gap-1 mt-1">
                <div
                  className="h-1.5 bg-gradient-to-r from-blue-400 to-blue-500 rounded-full"
                  style={{ width: `${statistics.maleRatio}%` }}
                />
                <div
                  className="h-1.5 bg-gradient-to-r from-pink-400 to-pink-500 rounded-full"
                  style={{ width: `${statistics.femaleRatio}%` }}
                />
              </div>
            </div>
          </div>

          {/* 탭 버튼 */}
          <div className="mb-4 flex-shrink-0 flex items-center justify-between gap-4">
            {/* 내원 상태 탭 (왼쪽) */}
            <div className="flex gap-3">
              <button
                className={`h-14 px-5 text-base leading-none font-medium rounded-lg transition-colors ${
                  activeTab === "all"
                    ? "bg-sky-500 text-white shadow-sm"
                    : "bg-white text-gray-700 hover:bg-gray-100 border border-gray-200"
                }`}
                onClick={() => handleTabChange("all")}
              >
                전체
              </button>
              <button
                className={`h-14 px-5 text-base leading-none font-medium rounded-lg transition-colors ${
                  activeTab === "waiting"
                    ? "bg-sky-500 text-white shadow-sm"
                    : "bg-white text-gray-700 hover:bg-gray-100 border border-gray-200"
                }`}
                onClick={() => handleTabChange("waiting")}
              >
                내원 대기
              </button>
            </div>

            {/* 날짜 범위 탭 (오른쪽) */}
            <div className="flex gap-3">
              <button
                className={`h-14 px-5 text-base leading-none font-medium rounded-lg transition-colors ${
                  dateRange === "week"
                    ? "bg-sky-500 text-white shadow-sm"
                    : "bg-white text-gray-700 hover:bg-gray-100 border border-gray-200"
                }`}
                onClick={() => applyPreset("week")}
              >
                최근 일주일
              </button>
              <button
                className={`h-14 px-5 text-base leading-none font-medium rounded-lg transition-colors ${
                  dateRange === "month"
                    ? "bg-sky-500 text-white shadow-sm"
                    : "bg-white text-gray-700 hover:bg-gray-100 border border-gray-200"
                }`}
                onClick={() => applyPreset("month")}
              >
                최근 한 달
              </button>
              <button
                className={`h-14 px-5 text-base leading-none font-medium rounded-lg transition-colors ${
                  dateRange === "all"
                    ? "bg-sky-500 text-white shadow-sm"
                    : "bg-white text-gray-700 hover:bg-gray-100 border border-gray-200"
                }`}
                onClick={() => applyPreset("all")}
              >
                전체 기간
              </button>

              {/* 시작 날짜 */}
              <DatePicker
                selected={startDate}
                onChange={(date: Date | null) => {
                  setDateRange("custom");
                  setStartDate(date);
                  setCurrentPage(1);
                }}
                selectsStart
                startDate={startDate}
                endDate={endDate}
                maxDate={new Date()}
                dateFormat="yyyy-MM-dd"
                wrapperClassName="self-stretch"
                withPortal
                customInput={
                  <DateButton
                    className={
                      isCustomActive
                        ? "bg-sky-500 text-white border border-sky-500 hover:bg-sky-500"
                        : "bg-white text-gray-900 border border-gray-300 hover:bg-gray-50"
                    }
                  >
                    <span className={isCustomActive ? "text-white" : "text-gray-700"}>
                      시작 날짜
                    </span>
                    <span className={isCustomActive ? "text-white" : "text-gray-900"}>
                      {startDate ? fmt(startDate) : ""}
                    </span>
                  </DateButton>
                }
              />

              {/* 종료 날짜 */}
              <DatePicker
                selected={endDate}
                onChange={(date: Date | null) => {
                  setDateRange("custom");
                  setEndDate(date);
                  setCurrentPage(1);
                }}
                selectsEnd
                startDate={startDate}
                endDate={endDate}
                minDate={startDate || undefined}
                maxDate={new Date()}
                dateFormat="yyyy-MM-dd"
                wrapperClassName="self-stretch"
                withPortal
                customInput={
                  <DateButton
                    className={
                      isCustomActive
                        ? "bg-sky-500 text-white border border-sky-500 hover:bg-sky-500"
                        : "bg-white text-gray-900 border border-gray-300 hover:bg-gray-50"
                    }
                  >
                    <span className={isCustomActive ? "text-white" : "text-gray-700"}>
                      종료 날짜
                    </span>
                    <span className={isCustomActive ? "text-white" : "text-gray-900"}>
                      {endDate ? fmt(endDate) : ""}
                    </span>
                  </DateButton>
                }
              />
            </div>
          </div>
          {/* 테이블 */}
          <div className="bg-white rounded-lg shadow-lg overflow-auto flex-1">
            {loading ? (
              <div className="flex items-center justify-center h-64">
                <p className="text-gray-600">로딩 중...</p>
              </div>
            ) : error ? (
              <div className="flex items-center justify-center h-64">
                <p className="text-red-600">{error}</p>
              </div>
            ) : patients.length === 0 ? (
              <div className="flex items-center justify-center h-64">
                <p className="text-gray-600">환자 데이터가 없습니다.</p>
              </div>
            ) : (
              <div className="overflow-auto h-full">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableCell header className="w-[8%] text-center">
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
                  {patients.map((patient) => {
                    // 급한 환자 판별 (PAIN 또는 UNRESPONSIVE)
                    const isUrgent = patient.mentalStatus === "PAIN" || patient.mentalStatus === "UNRESPONSIVE";

                    return (
                      <TableRow
                        key={patient.hospitalSelectionId}
                        variant={
                          patient.status === "ACCEPTED" ? "alert" : "default"
                        }
                        onClick={() => handlePatientClick(patient)}
                      >
                        <TableCell className="w-[8%] text-center">
                          <div className="flex items-center justify-center gap-2">
                            {isUrgent && (
                              <span className="flex h-2 w-2">
                                <span className="animate-ping absolute inline-flex h-2 w-2 rounded-full bg-red-400 opacity-75"></span>
                                <span className="relative inline-flex rounded-full h-2 w-2 bg-red-500"></span>
                              </span>
                            )}
                            {patient.hospitalSelectionId}
                          </div>
                        </TableCell>
                        <TableCell className="w-[8%]">
                          <span className={patient.gender === "M" ? "text-blue-600 font-medium" : "text-pink-600 font-medium"}>
                            {patient.gender === "M" ? "남" : "여"}
                          </span>
                        </TableCell>
                        <TableCell className="w-[8%]">{patient.age}</TableCell>
                        <TableCell className="w-[12%]">
                          {formatRecordTime(patient.recordTime)}
                        </TableCell>
                        <TableCell className="w-[30%]">
                          <div className="truncate" title={patient.chiefComplaint}>
                            {patient.chiefComplaint}
                          </div>
                        </TableCell>
                        <TableCell className="w-[12%]">
                          <span className={`inline-flex px-2 py-1 rounded-full text-xs font-medium ${
                            patient.mentalStatus === "ALERT"
                              ? "bg-green-100 text-green-800"
                              : patient.mentalStatus === "VERBAL"
                              ? "bg-yellow-100 text-yellow-800"
                              : patient.mentalStatus === "PAIN"
                              ? "bg-orange-100 text-orange-800"
                              : "bg-red-100 text-red-800"
                          }`}>
                            {patient.mentalStatus}
                          </span>
                        </TableCell>
                        <TableCell className="w-[12%]">
                          <span className={`inline-flex px-2 py-1 rounded-full text-xs font-medium ${
                            patient.status === "ACCEPTED"
                              ? "bg-amber-100 text-amber-800"
                              : "bg-green-100 text-green-800"
                          }`}>
                            {patient.status === "ACCEPTED"
                              ? "내원 대기"
                              : "내원 완료"}
                          </span>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
              </div>
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
            <p className="text-gray-600">로딩 중...</p>
          </div>
        ) : selectedPatient ? (
          <div className="px-10 py-1">
            {/* 헤더 */}
            <h2 className="text-2xl font-bold text-gray-900 mb-4">
              환자 정보
            </h2>

            {/* 기본 정보 (항상 표시) */}
            <div className="space-y-2">
              {/* 성별, 나이 */}
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block text-xs text-gray-700 mb-0.5">
                    성별 <span className="text-red-500">*</span>
                  </label>
                  <div className="bg-gray-100 px-3 py-1.5 rounded text-sm text-gray-800">
                    {selectedPatient.gender === "M" ? "남성" : "여성"}
                  </div>
                </div>
                <div>
                  <label className="block text-xs text-gray-700 mb-0.5">
                    나이 <span className="text-red-500">*</span>
                  </label>
                  <div className="bg-gray-100 px-3 py-1.5 rounded text-sm text-gray-800">
                    {selectedPatient.age}
                  </div>
                </div>
              </div>

              {/* 시간, 멘탈 */}
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block text-xs text-gray-700 mb-0.5">
                    시간 <span className="text-red-500">*</span>
                  </label>
                  <div className="bg-gray-100 px-3 py-1.5 rounded text-sm text-gray-800">
                    {selectedPatient.recordTime?.replace("T", " ") ||
                      selectedPatient.recordTime}
                  </div>
                </div>
                <div>
                  <label className="block text-xs text-gray-700 mb-0.5">
                    멘탈 <span className="text-red-500">*</span>
                  </label>
                  <div className="bg-gray-100 px-3 py-1.5 rounded text-sm text-gray-800">
                    {selectedPatient.mentalStatus}
                  </div>
                </div>
              </div>

              {/* 주호소 */}
              <div>
                <label className="block text-xs text-gray-700 mb-0.5">
                  주호소 <span className="text-red-500">*</span>
                </label>
                <div className="bg-gray-100 px-3 py-1.5 rounded text-sm text-gray-800">
                  {selectedPatient.chiefComplaint}
                </div>
              </div>
            </div>

            {/* 펼침/접기 버튼 */}
            {!isExpanded && (
              <div className="flex justify-center my-3">
                <button
                  onClick={() => setIsExpanded(!isExpanded)}
                  className="w-7 h-7 flex items-center justify-center rounded-full bg-gray-200 hover:bg-gray-300 transition-colors"
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
              <div className="space-y-2 border-t border-gray-200 pt-3 my-5 relative">
                {/* 접기 버튼을 구분선 위에 배치 */}
                <button
                  onClick={() => setIsExpanded(false)}
                  className="absolute -top-3.5 left-1/2 -translate-x-1/2 w-7 h-7 flex items-center justify-center rounded-full bg-gray-200 hover:bg-gray-300 transition-colors"
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
                    <label className="block text-xs text-gray-700 mb-0.5">
                      HR <span className="text-red-500">*</span>
                    </label>
                    <div className="bg-gray-100 px-3 py-1.5 rounded text-sm text-gray-800">
                      {selectedPatient.hr}
                    </div>
                  </div>
                  <div>
                    <label className="block text-xs text-gray-700 mb-0.5">
                      BP <span className="text-red-500">*</span>
                    </label>
                    <div className="bg-gray-100 px-3 py-1.5 rounded text-sm text-gray-800">
                      {selectedPatient.bp}
                    </div>
                  </div>
                  <div>
                    <label className="block text-xs text-gray-700 mb-0.5">
                      SpO2 <span className="text-red-500">*</span>
                    </label>
                    <div className="bg-gray-100 px-3 py-1.5 rounded text-sm text-gray-800">
                      {selectedPatient.spo2}
                    </div>
                  </div>
                </div>

                {/* RR, BT, 보호자 유무 */}
                <div className="grid grid-cols-3 gap-2">
                  <div>
                    <label className="block text-xs text-gray-700 mb-0.5">
                      RR <span className="text-red-500">*</span>
                    </label>
                    <div className="bg-gray-100 px-3 py-1.5 rounded text-sm text-gray-800">
                      {selectedPatient.rr}
                    </div>
                  </div>
                  <div>
                    <label className="block text-xs text-gray-700 mb-0.5">
                      BT <span className="text-red-500">*</span>
                    </label>
                    <div className="bg-gray-100 px-3 py-1.5 rounded text-sm text-gray-800">
                      {selectedPatient.bt}
                    </div>
                  </div>
                  <div>
                    <label className="block text-xs text-gray-700 mb-0.5">
                      보호자 유무 <span className="text-red-500">*</span>
                    </label>
                    <div className="bg-gray-100 px-3 py-1.5 rounded text-sm text-gray-800">
                      {selectedPatient.hasGuardian ? "유" : "무"}
                    </div>
                  </div>
                </div>

                {/* Hx */}
                <div>
                  <label className="block text-xs text-gray-700 mb-0.5">
                    Hx <span className="text-red-500">*</span>
                  </label>
                  <div className="bg-gray-100 px-3 py-1.5 rounded text-sm text-gray-800">
                    {selectedPatient.hx}
                  </div>
                </div>

                {/* 발병 시간, LNT */}
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="block text-xs text-gray-700 mb-0.5">
                      발병 시간 <span className="text-red-500">*</span>
                    </label>
                    <div className="bg-gray-100 px-3 py-1.5 rounded text-sm text-gray-800">
                      {selectedPatient.onsetTime?.replace("T", " ") ||
                        selectedPatient.onsetTime}
                    </div>
                  </div>
                  <div>
                    <label className="block text-xs text-gray-700 mb-0.5">
                      LNT <span className="text-red-500">*</span>
                    </label>
                    <div className="bg-gray-100 px-3 py-1.5 rounded text-sm text-gray-800">
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
                  className="px-3 py-2 bg-gray-500 text-white rounded-lg font-semibold hover:bg-gray-600 transition-colors"
                >
                  닫기
                </button>
                <button
                  onClick={handleCompleteVisit}
                  className="px-3 py-2 bg-sky-500 text-white rounded-lg font-semibold hover:bg-sky-600 transition-colors"
                >
                  내원완료
                </button>
              </div>
            ) : (
              // 내원 완료 상태: 닫기 버튼만 중앙에 표시
              <div className="flex justify-center mt-6">
                <button
                  onClick={handleCloseModal}
                  className="w-1/2 px-3 py-2 bg-gray-500 text-white rounded-lg font-semibold hover:bg-gray-600 transition-colors"
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
