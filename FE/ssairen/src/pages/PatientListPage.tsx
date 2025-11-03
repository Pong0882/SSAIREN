import { useState } from "react";
import {
  Tabs,
  TabButton,
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableCell,
  Pagination,
} from "@/components";

interface RecordTime {
  hour: number;
  minute: number;
  second: number;
  nano: number;
}

interface Patient {
  hospitalSelectionId: number;
  emergencyReportId: number;
  gender: string;
  age: number;
  recordTime: string;
  chiefComplaint: string;
  mentalStatus: string;
  status: "PENDING" | "COMPLETED";
}

const mockPatients: Patient[] = [
  {
    hospitalSelectionId: 1,
    emergencyReportId: 1001,
    gender: "F",
    age: 76,
    recordTime: "17:53:08.464",
    chiefComplaint: "주호소 증상",
    mentalStatus: "alert",
    status: "PENDING",
  },
  {
    hospitalSelectionId: 2,
    emergencyReportId: 1002,
    gender: "M",
    age: 55,
    recordTime: "17:53:08.464",
    chiefComplaint: "주호소 증상",
    mentalStatus: "semicoma",
    status: "COMPLETED",
  },
  {
    hospitalSelectionId: 3,
    emergencyReportId: 1003,
    gender: "M",
    age: 47,
    recordTime: "17:53:08.464",
    chiefComplaint: "주호소 증상",
    mentalStatus: "stupor",
    status: "COMPLETED",
  },
  {
    hospitalSelectionId: 4,
    emergencyReportId: 1004,
    gender: "F",
    age: 62,
    recordTime: "17:53:08.464",
    chiefComplaint: "주호소 증상",
    mentalStatus: "semicoma",
    status: "COMPLETED",
  },
  {
    hospitalSelectionId: 5,
    emergencyReportId: 1005,
    gender: "M",
    age: 41,
    recordTime: "17:53:08.464",
    chiefComplaint: "주호소 증상",
    mentalStatus: "stupor",
    status: "COMPLETED",
  },
  {
    hospitalSelectionId: 6,
    emergencyReportId: 1006,
    gender: "M",
    age: 58,
    recordTime: "17:53:08.464",
    chiefComplaint: "주호소 증상",
    mentalStatus: "semicoma",
    status: "COMPLETED",
  },
  {
    hospitalSelectionId: 7,
    emergencyReportId: 1007,
    gender: "F",
    age: 69,
    recordTime: "17:53:08.464",
    chiefComplaint: "주호소 증상",
    mentalStatus: "alert",
    status: "PENDING",
  },
  {
    hospitalSelectionId: 8,
    emergencyReportId: 1008,
    gender: "M",
    age: 53,
    recordTime: "17:53:08.464",
    chiefComplaint: "주호소 증상",
    mentalStatus: "stupor",
    status: "COMPLETED",
  },
  {
    hospitalSelectionId: 9,
    emergencyReportId: 1009,
    gender: "F",
    age: 45,
    recordTime: "17:53:08.464",
    chiefComplaint: "주호소 증상",
    mentalStatus: "semicoma",
    status: "COMPLETED",
  },
  {
    hospitalSelectionId: 10,
    emergencyReportId: 1010,
    gender: "M",
    age: 71,
    recordTime: "17:53:08.464",
    chiefComplaint: "주호소 증상",
    mentalStatus: "stupor",
    status: "COMPLETED",
  },
  {
    hospitalSelectionId: 11,
    emergencyReportId: 1011,
    gender: "F",
    age: 38,
    recordTime: "17:53:08.464",
    chiefComplaint: "주호소 증상",
    mentalStatus: "semicoma",
    status: "COMPLETED",
  },
  {
    hospitalSelectionId: 12,
    emergencyReportId: 1012,
    gender: "M",
    age: 64,
    recordTime: "17:53:08.464",
    chiefComplaint: "주호소 증상",
    mentalStatus: "stupor",
    status: "COMPLETED",
  },
  {
    hospitalSelectionId: 13,
    emergencyReportId: 1013,
    gender: "F",
    age: 51,
    recordTime: "17:53:08.464",
    chiefComplaint: "주호소 증상",
    mentalStatus: "alert",
    status: "PENDING",
  },
  {
    hospitalSelectionId: 14,
    emergencyReportId: 1014,
    gender: "M",
    age: 49,
    recordTime: "17:53:08.464",
    chiefComplaint: "주호소 증상",
    mentalStatus: "semicoma",
    status: "COMPLETED",
  },
  {
    hospitalSelectionId: 15,
    emergencyReportId: 1015,
    gender: "F",
    age: 57,
    recordTime: "17:53:08.464",
    chiefComplaint: "주호소 증상",
    mentalStatus: "stupor",
    status: "COMPLETED",
  },
];

export default function PatientListPage() {
  const [activeTab, setActiveTab] = useState<"all" | "waiting">("all");
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  const filteredPatients =
    activeTab === "all"
      ? mockPatients
      : mockPatients.filter((p) => p.status === "PENDING");

  const totalPages = Math.ceil(filteredPatients.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const paginatedPatients = filteredPatients.slice(startIndex, endIndex);

  const handleTabChange = (tab: "all" | "waiting") => {
    setActiveTab(tab);
    setCurrentPage(1); // 탭 변경 시 첫 페이지로
  };

  return (
    <div className="h-screen bg-neutral-900 p-8 overflow-hidden">
      <div className="max-w-7xl mx-auto flex flex-col">
        {/* 탭 버튼 */}
        <div className="mb-6">
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
          <Table>
            <TableHeader>
              <TableRow>
                <TableCell header>No</TableCell>
                <TableCell header>성별</TableCell>
                <TableCell header>나이</TableCell>
                <TableCell header>시간</TableCell>
                <TableCell header>주호소</TableCell>
                <TableCell header>멘탈</TableCell>
                <TableCell header>내원 여부</TableCell>
              </TableRow>
            </TableHeader>
            <TableBody>
              {paginatedPatients.map((patient, index) => (
                <TableRow
                  key={index}
                  variant={patient.status === "PENDING" ? "alert" : "default"}
                  onClick={() => console.log("Patient clicked:", patient)}
                >
                  <TableCell>{patient.hospitalSelectionId}</TableCell>
                  <TableCell>{patient.gender}</TableCell>
                  <TableCell>{patient.age}</TableCell>
                  <TableCell>{patient.recordTime.split(".")[0]}</TableCell>
                  <TableCell>{patient.chiefComplaint}</TableCell>
                  <TableCell>{patient.mentalStatus}</TableCell>
                  <TableCell>
                    {patient.status === "PENDING" ? "내원 대기" : "내원 완료"}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>

        {/* 페이지네이션 */}
        <div className="mt-4">
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
          />
        </div>
      </div>
    </div>
  );
}
