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

interface Patient {
  no: number;
  gender: "M" | "F";
  age: number;
  time: string;
  symptoms: string;
  diagnosis: string;
  visited: string;
  alert?: boolean;
}

const mockPatients: Patient[] = [
  {
    no: 1,
    gender: "F",
    age: 76,
    time: "10:01",
    symptoms: "주호소 증상",
    diagnosis: "alert",
    visited: "내원 대기",
    alert: true,
  },
  {
    no: 2,
    gender: "M",
    age: 55,
    time: "10:07",
    symptoms: "주호소 증상",
    diagnosis: "semicoma",
    visited: "내원 완료",
  },
  {
    no: 3,
    gender: "M",
    age: 47,
    time: "10:20",
    symptoms: "주호소 증상",
    diagnosis: "stupor",
    visited: "내원 완료",
  },
  {
    no: 4,
    gender: "F",
    age: 62,
    time: "10:25",
    symptoms: "주호소 증상",
    diagnosis: "semicoma",
    visited: "내원 완료",
  },
  {
    no: 5,
    gender: "M",
    age: 41,
    time: "10:30",
    symptoms: "주호소 증상",
    diagnosis: "stupor",
    visited: "내원 완료",
  },
  {
    no: 6,
    gender: "M",
    age: 58,
    time: "10:35",
    symptoms: "주호소 증상",
    diagnosis: "semicoma",
    visited: "내원 완료",
  },
  {
    no: 7,
    gender: "F",
    age: 69,
    time: "10:40",
    symptoms: "주호소 증상",
    diagnosis: "alert",
    visited: "내원 대기",
    alert: true,
  },
  {
    no: 8,
    gender: "M",
    age: 53,
    time: "10:45",
    symptoms: "주호소 증상",
    diagnosis: "stupor",
    visited: "내원 완료",
  },
  {
    no: 9,
    gender: "F",
    age: 45,
    time: "10:50",
    symptoms: "주호소 증상",
    diagnosis: "semicoma",
    visited: "내원 완료",
  },
  {
    no: 10,
    gender: "M",
    age: 71,
    time: "10:55",
    symptoms: "주호소 증상",
    diagnosis: "stupor",
    visited: "내원 완료",
  },
  {
    no: 11,
    gender: "F",
    age: 38,
    time: "11:00",
    symptoms: "주호소 증상",
    diagnosis: "semicoma",
    visited: "내원 완료",
  },
  {
    no: 12,
    gender: "M",
    age: 64,
    time: "11:05",
    symptoms: "주호소 증상",
    diagnosis: "stupor",
    visited: "내원 완료",
  },
  {
    no: 13,
    gender: "F",
    age: 51,
    time: "11:10",
    symptoms: "주호소 증상",
    diagnosis: "alert",
    visited: "내원 대기",
    alert: true,
  },
  {
    no: 14,
    gender: "M",
    age: 49,
    time: "11:15",
    symptoms: "주호소 증상",
    diagnosis: "semicoma",
    visited: "내원 완료",
  },
  {
    no: 15,
    gender: "F",
    age: 57,
    time: "11:20",
    symptoms: "주호소 증상",
    diagnosis: "stupor",
    visited: "내원 완료",
  },
];

export default function PatientListPage() {
  const [activeTab, setActiveTab] = useState<"all" | "waiting">("all");
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  const filteredPatients =
    activeTab === "all"
      ? mockPatients
      : mockPatients.filter((p) => p.visited === "내원 대기");

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
                  variant={patient.alert ? "alert" : "default"}
                  onClick={() => console.log("Patient clicked:", patient)}
                >
                  <TableCell>{patient.no}</TableCell>
                  <TableCell>{patient.gender}</TableCell>
                  <TableCell>{patient.age}</TableCell>
                  <TableCell>{patient.time}</TableCell>
                  <TableCell>{patient.symptoms}</TableCell>
                  <TableCell>{patient.diagnosis}</TableCell>
                  <TableCell>{patient.visited}</TableCell>
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
