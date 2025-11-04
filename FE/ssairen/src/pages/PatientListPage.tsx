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
} from "@/components";
import { useAuthStore } from "@/features/auth/store/authStore";
import { fetchPatientsApi } from "@/features/patients/api/patientApi";
import type { Patient } from "@/features/patients/types/patient.types";

export default function PatientListPage() {
  const { user } = useAuthStore();
  const [activeTab, setActiveTab] = useState<"all" | "waiting">("all");
  const [currentPage, setCurrentPage] = useState(1);
  const [patients, setPatients] = useState<Patient[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const itemsPerPage = 10;

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
      });

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
  }, [user?.id, currentPage, itemsPerPage, activeTab]);

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

    window.addEventListener("newPatientRequest", handleNewPatientRequest);

    return () => {
      window.removeEventListener("newPatientRequest", handleNewPatientRequest);
    };
  }, [fetchPatients]);

  const handleTabChange = (tab: "all" | "waiting") => {
    setActiveTab(tab);
    setCurrentPage(1); // 탭 변경 시 첫 페이지로
  };

  // recordTime을 문자열로 포맷하는 헬퍼 함수
  const formatRecordTime = (recordTime: Patient["recordTime"]) => {
    if (typeof recordTime === "string") return recordTime;

    const { hour, minute, second } = recordTime;
    return `${String(hour).padStart(2, "0")}:${String(minute).padStart(
      2,
      "0"
    )}:${String(second).padStart(2, "0")}`;
  };

  return (
    <div className="h-screen bg-neutral-900 flex flex-col overflow-hidden">
      {/* Header */}
      <Header />

      {/* Main Content */}
      <div className="flex-1 flex flex-col p-4 sm:p-6 lg:p-8 min-h-0">
        <div className="max-w-7xl mx-auto w-full flex flex-col h-full">
          {/* 탭 버튼 */}
          <div className="mb-4 sm:mb-6 flex-shrink-0">
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
                    <TableCell header className="w-[8%]">No</TableCell>
                    <TableCell header className="w-[8%]">성별</TableCell>
                    <TableCell header className="w-[8%]">나이</TableCell>
                    <TableCell header className="w-[12%]">시간</TableCell>
                    <TableCell header className="w-[30%]">주호소</TableCell>
                    <TableCell header className="w-[12%]">멘탈</TableCell>
                    <TableCell header className="w-[12%]">내원 여부</TableCell>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {patients.map((patient) => (
                    <TableRow
                      key={patient.hospitalSelectionId}
                      variant={
                        patient.status === "PENDING" ? "alert" : "default"
                      }
                      onClick={() => console.log("Patient clicked:", patient)}
                    >
                      <TableCell className="w-[8%]">{patient.hospitalSelectionId}</TableCell>
                      <TableCell className="w-[8%]">{patient.gender}</TableCell>
                      <TableCell className="w-[8%]">{patient.age}</TableCell>
                      <TableCell className="w-[12%]">
                        {formatRecordTime(patient.recordTime)}
                      </TableCell>
                      <TableCell className="w-[30%]">{patient.chiefComplaint}</TableCell>
                      <TableCell className="w-[12%]">{patient.mentalStatus}</TableCell>
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
    </div>
  );
}
