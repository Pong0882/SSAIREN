import { useState, useEffect, forwardRef, useMemo } from "react";
import Sidebar from "@/components/layout/Sidebar";
import { Header } from "@/components";
import { useAuthStore } from "@/features/auth/store/authStore";
import { fetchPatientStatisticsApi } from "@/features/statistics/api/statisticsApi";
import type { PatientStatisticsResponse } from "@/features/statistics/types/statistics.types";
import {
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

type RangeKey = "week" | "month" | "all" | "custom";

export default function PatientStatsPage() {
  const { user } = useAuthStore();
  const [data, setData] = useState<PatientStatisticsResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [dateRange, setDateRange] = useState<RangeKey>("month");
  const [startDate, setStartDate] = useState<Date | null>(null);
  const [endDate, setEndDate] = useState<Date | null>(null);

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

  const fmt = (d: Date) => d.toISOString().split("T")[0];

  const calcPresetRange = (key: Exclude<RangeKey, "custom">) => {
    const today = new Date();
    let start: Date;
    let end: Date;

    if (key === "week") {
      end = new Date(today);
      start = new Date(today);
      start.setDate(today.getDate() - 7);
    } else if (key === "month") {
      end = new Date(today);
      start = new Date(today);
      start.setMonth(today.getMonth() - 1);
    } else {
      // all: 1년 전 ~ 현재 달 말일
      start = new Date(today);
      start.setFullYear(today.getFullYear() - 1);
      end = new Date(today.getFullYear(), today.getMonth() + 1, 0); // 현재 달 말일
    }

    start.setHours(0, 0, 0, 0);
    end.setHours(23, 59, 59, 999);
    return { start, end };
  };

  const applyPreset = (key: Exclude<RangeKey, "custom">) => {
    const { start, end } = calcPresetRange(key);
    setDateRange(key);
    setStartDate(start);
    setEndDate(end);
  };

  useEffect(() => {
    if (startDate && endDate) return;
    const { start, end } = calcPresetRange("month");
    setStartDate(start);
    setEndDate(end);
  }, []);

  useEffect(() => {
    if (!user?.id || !startDate || !endDate) return;

    (async () => {
      setLoading(true);
      setError(null);
      try {
        const result = await fetchPatientStatisticsApi({
          hospitalId: user.id,
          startDate: fmt(startDate),
          endDate: fmt(endDate),
        });
        setData(result);
      } catch (err) {
        setError(err instanceof Error ? err.message : "통계 조회에 실패했습니다.");
      } finally {
        setLoading(false);
      }
    })();
  }, [user?.id, startDate, endDate]);

  const isCustomActive = dateRange === "custom" && !!startDate && !!endDate;

  // 성별 차트 데이터 (전체 표시)
  const genderData = data
    ? [
        { name: "남성", value: data.byGender.M || 0 },
        { name: "여성", value: data.byGender.F || 0 },
      ]
    : [];

  // 연령대 차트 데이터 (전체 표시)
  const ageGroupData = data
    ? [
        { name: "0-9", count: data.byAgeGroup["0-9"] || 0 },
        { name: "10-19", count: data.byAgeGroup["10-19"] || 0 },
        { name: "20-29", count: data.byAgeGroup["20-29"] || 0 },
        { name: "30-39", count: data.byAgeGroup["30-39"] || 0 },
        { name: "40-49", count: data.byAgeGroup["40-49"] || 0 },
        { name: "50-59", count: data.byAgeGroup["50-59"] || 0 },
        { name: "60-69", count: data.byAgeGroup["60-69"] || 0 },
        { name: "70-79", count: data.byAgeGroup["70-79"] || 0 },
        { name: "80+", count: data.byAgeGroup["80+"] || 0 },
      ]
    : [];

  // 의식 상태 차트 데이터 (전체 표시)
  const mentalStatusData = data
    ? [
        { name: "의식 명료", value: data.byMentalStatus.ALERT || 0 },
        { name: "언어 반응", value: data.byMentalStatus.VERBAL || 0 },
        { name: "통증 반응", value: data.byMentalStatus.PAIN || 0 },
        { name: "무반응", value: data.byMentalStatus.UNRESPONSIVE || 0 },
      ]
    : [];

  const GENDER_COLORS = ["#0ea5e9", "#f97316"];
  const MENTAL_STATUS_COLORS = ["#10b981", "#f59e0b", "#ef4444", "#6b7280"];

  return (
    <div className="h-screen flex overflow-hidden">
      <Sidebar />

      <div className="flex-1 flex flex-col overflow-hidden bg-gray-50">
        <Header />

        <div className="flex-1 flex flex-col p-4 sm:p-6 lg:p-8 min-h-0">
          <div className="max-w-full h-full flex flex-col gap-4">
            {/* 상단: 필터 행 */}
            <div className="flex gap-3 items-stretch justify-between">
              {/* 총 수용 건수 */}
              <div className="relative h-14 bg-white rounded-lg shadow-sm border border-gray-200 px-5 flex items-center justify-between min-w-[280px]">
                <div className="flex items-center gap-3">
                  <span className="text-sm leading-none text-gray-600 whitespace-nowrap">
                    총 수용 건수
                  </span>
                  <span className="text-xl leading-none font-bold text-sky-500 whitespace-nowrap">
                    {data ? `${data.totalCount}건` : "-"}
                  </span>
                </div>

                {/* 우측: 상세 팝오버 트리거 */}
                {data && (
                  <PopoverSummary
                    startDate={data.startDate}
                    endDate={data.endDate}
                    byGender={data.byGender}
                    byAgeGroup={data.byAgeGroup}
                    byMentalStatus={data.byMentalStatus}
                    totalCount={data.totalCount}
                  />
                )}
              </div>

              {/* 날짜 필터 버튼들 */}
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

            {/* 로딩/에러 상태 */}
            {loading && (
              <div className="flex-1 bg-white rounded-lg shadow-lg flex items-center justify-center">
                <p className="text-gray-600">데이터를 불러오는 중...</p>
              </div>
            )}
            {error && (
              <div className="flex-1 bg-white rounded-lg shadow-lg flex items-center justify-center">
                <p className="text-red-600">{error}</p>
              </div>
            )}

            {/* 하단: 차트 3개 */}
            {!loading && !error && data && (
              <div className="flex-1 grid grid-cols-1 lg:grid-cols-3 gap-4 min-h-0">
                {/* 성별 분포 파이 차트 */}
                <div className="bg-white rounded-lg shadow-lg p-6 flex flex-col min-h-0">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">성별 분포</h3>
                  <div className="flex-1 min-h-0">
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie
                          data={genderData}
                          cx="50%"
                          cy="50%"
                          labelLine={false}
                          label={(entry: any) => (entry.value > 0 ? `${entry.value}명` : "")}
                          outerRadius={80}
                          fill="#8884d8"
                          dataKey="value"
                          animationBegin={0}
                        >
                          {genderData.map((_entry, index) => (
                            <Cell
                              key={`cell-${index}`}
                              fill={GENDER_COLORS[index % GENDER_COLORS.length]}
                            />
                          ))}
                        </Pie>
                        <Tooltip />
                        <Legend />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                </div>

                {/* 연령대 분포 바 차트 */}
                <div className="bg-white rounded-lg shadow-lg p-6 flex flex-col min-h-0">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">연령대 분포</h3>
                  <div className="flex-1 min-h-0">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={ageGroupData} margin={{ top: 8, right: 12, bottom: 4, left: 8 }}>
                        <CartesianGrid vertical={false} stroke="#E5E7EB" strokeOpacity={0.35} />
                        <XAxis
                          dataKey="name"
                          tick={{ fill: "#6B7280", fontSize: 12 }}
                          axisLine={{ stroke: "#E5E7EB" }}
                          tickLine={false}
                        />
                        <YAxis
                          allowDecimals={false}
                          tick={{ fill: "#6B7280", fontSize: 12 }}
                          axisLine={{ stroke: "#E5E7EB" }}
                          tickLine={false}
                        />
                        <Tooltip
                          formatter={(v: number) => [`${v}명`, "수용 건수"]}
                          labelStyle={{ color: "#111827" }}
                          contentStyle={{ borderRadius: 10, borderColor: "#E5E7EB" }}
                        />
                        <Bar dataKey="count" fill="#0ea5e9" radius={[6, 6, 0, 0]} maxBarSize={40} animationBegin={0} />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>

                {/* 의식 상태 분포 도넛 차트 */}
                <div className="bg-white rounded-lg shadow-lg p-6 flex flex-col min-h-0">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">의식 상태 분포</h3>
                  <div className="flex-1 min-h-0">
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie
                          data={mentalStatusData}
                          cx="50%"
                          cy="50%"
                          labelLine={false}
                          label={(entry: any) => (entry.value > 0 ? `${entry.value}명` : "")}
                          outerRadius={80}
                          innerRadius={40}
                          fill="#8884d8"
                          dataKey="value"
                          animationBegin={0}
                        >
                          {mentalStatusData.map((_entry, index) => (
                            <Cell
                              key={`cell-${index}`}
                              fill={MENTAL_STATUS_COLORS[index % MENTAL_STATUS_COLORS.length]}
                            />
                          ))}
                        </Pie>
                        <Tooltip />
                        <Legend />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function PopoverSummary({
  startDate,
  endDate,
  byGender,
  byAgeGroup,
  byMentalStatus,
  totalCount,
}: {
  startDate: string;
  endDate: string;
  byGender: Record<string, number>;
  byAgeGroup: Record<string, number>;
  byMentalStatus: Record<string, number>;
  totalCount: number;
}) {
  const [open, setOpen] = useState(false);

  const mentalStatusNames: Record<string, string> = {
    ALERT: "의식 명료",
    VERBAL: "언어 반응",
    PAIN: "통증 반응",
    UNRESPONSIVE: "무반응",
  };

  // 성별 비율 및 데이터
  const genderData = useMemo(() => {
    const maleCount = byGender.M || 0;
    const femaleCount = byGender.F || 0;
    const total = maleCount + femaleCount;
    return {
      male: maleCount,
      female: femaleCount,
      maleRatio: total > 0 ? Math.round((maleCount / total) * 100) : 0,
      femaleRatio: total > 0 ? Math.round((femaleCount / total) * 100) : 0,
    };
  }, [byGender]);

  // 주요 연령대 (Top 1)
  const topAgeGroup = useMemo(() => {
    const entries = Object.entries(byAgeGroup || {});
    if (!entries.length) return { age: "-", count: 0, ratio: 0 };
    const [age, count] = entries.reduce((a, b) => (Number(a[1]) >= Number(b[1]) ? a : b));
    const ratio = totalCount > 0 ? Math.round((Number(count) / totalCount) * 100) : 0;
    return { age, count: Number(count), ratio };
  }, [byAgeGroup, totalCount]);

  // 주요 의식 상태 (Top 1) - 현재 미사용
  // const topMentalStatus = useMemo(() => {
  //   const entries = Object.entries(byMentalStatus || {});
  //   if (!entries.length) return { status: "-", statusKr: "-", count: 0, ratio: 0 };
  //   const [status, count] = entries.reduce((a, b) => (Number(a[1]) >= Number(b[1]) ? a : b));
  //   const ratio = totalCount > 0 ? Math.round((Number(count) / totalCount) * 100) : 0;
  //   return {
  //     status,
  //     statusKr: mentalStatusNames[status] || status,
  //     count: Number(count),
  //     ratio,
  //   };
  // }, [byMentalStatus, totalCount]);

  // 연령대 분포 미니 바 데이터
  const ageDistribution = useMemo(() => {
    const ageOrder = ["0-9", "10-19", "20-29", "30-39", "40-49", "50-59", "60-69", "70-79", "80+"];
    const data = ageOrder.map((age) => ({
      age,
      count: Number(byAgeGroup?.[age] || 0),
    }));
    const maxCount = Math.max(...data.map((d) => d.count), 1);
    return data.map((item) => ({
      ...item,
      percentage: (item.count / maxCount) * 100,
    }));
  }, [byAgeGroup]);

  // 의식 상태 분포 미니 바 데이터
  const mentalStatusDistribution = useMemo(() => {
    const statusOrder = ["ALERT", "VERBAL", "PAIN", "UNRESPONSIVE"];
    const data = statusOrder.map((status) => ({
      status,
      statusKr: mentalStatusNames[status],
      count: Number(byMentalStatus?.[status] || 0),
    }));
    const maxCount = Math.max(...data.map((d) => d.count), 1);
    return data.map((item) => ({
      ...item,
      percentage: (item.count / maxCount) * 100,
    }));
  }, [byMentalStatus]);

  return (
    <>
      <button
        onClick={() => setOpen((o) => !o)}
        className="text-xs leading-none px-3 py-2 rounded-md border border-gray-200 hover:bg-gray-50 text-gray-600"
        aria-expanded={open}
      >
        상세 보기
      </button>

      {open && (
        <div
          className="absolute left-0 top-14 z-20 w-[500px] bg-white border border-gray-200 rounded-xl shadow-xl p-4 max-h-[80vh] overflow-y-auto"
          role="dialog"
        >
          {/* 헤더 */}
          <div className="flex items-start justify-between mb-3">
            <div>
              <div className="text-xs text-gray-500">선택 기간</div>
              <div className="text-xs font-medium text-gray-900">
                {startDate} ~ {endDate}
              </div>
            </div>
            <button
              className="text-gray-400 hover:text-gray-600"
              onClick={() => setOpen(false)}
              aria-label="닫기"
            >
              ✕
            </button>
          </div>

          {/* 메인 통계 카드 */}
          <div className="grid grid-cols-2 gap-2 mb-3">
            <div className="bg-gradient-to-br from-sky-50 to-blue-50 rounded-lg p-2.5 border border-sky-100">
              <div className="text-[10px] text-sky-600 font-medium">총 수용 건수</div>
              <div className="text-lg font-bold text-sky-700">{totalCount}건</div>
              <div className="text-[10px] text-sky-500">
                남 {genderData.male} / 여 {genderData.female}
              </div>
            </div>
            <div className="bg-gradient-to-br from-purple-50 to-pink-50 rounded-lg p-2.5 border border-purple-100">
              <div className="text-[10px] text-purple-600 font-medium">주요 연령대</div>
              <div className="text-lg font-bold text-purple-700">{topAgeGroup.age}세</div>
              <div className="text-[10px] text-purple-500">
                {topAgeGroup.count}건 ({topAgeGroup.ratio}%)
              </div>
            </div>
          </div>

          {/* 2열 그리드: 성별 분포 + 의식 상태 */}
          <div className="grid grid-cols-2 gap-3 mb-3">
            {/* 성별 분포 */}
            <div>
              <div className="text-xs text-gray-600 font-medium mb-1.5">성별 분포</div>
              <div className="space-y-1.5">
                <div className="flex items-center gap-1.5">
                  <div className="w-8 text-[9px] text-gray-600">남</div>
                  <div className="flex-1 h-4 bg-gray-100 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-gradient-to-r from-sky-400 to-blue-500 rounded-full"
                      style={{ width: `${genderData.maleRatio}%` }}
                      title={`${genderData.male}명 (${genderData.maleRatio}%)`}
                    />
                  </div>
                </div>
                <div className="flex items-center gap-1.5">
                  <div className="w-8 text-[9px] text-gray-600">여</div>
                  <div className="flex-1 h-4 bg-gray-100 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-gradient-to-r from-orange-400 to-pink-500 rounded-full"
                      style={{ width: `${genderData.femaleRatio}%` }}
                      title={`${genderData.female}명 (${genderData.femaleRatio}%)`}
                    />
                  </div>
                </div>
              </div>
            </div>

            {/* 의식 상태 분포 */}
            <div>
              <div className="text-xs text-gray-600 font-medium mb-1.5">의식 상태 분포</div>
              <div className="space-y-1.5">
                {mentalStatusDistribution.map((item, idx) => (
                  <div key={idx} className="flex items-center gap-1.5">
                    <div className="w-12 text-[9px] text-gray-600 truncate" title={item.statusKr}>
                      {item.statusKr}
                    </div>
                    <div className="flex-1 h-4 bg-gray-100 rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full ${
                          item.status === "ALERT"
                            ? "bg-gradient-to-r from-green-400 to-emerald-500"
                            : item.status === "VERBAL"
                            ? "bg-gradient-to-r from-yellow-400 to-orange-500"
                            : item.status === "PAIN"
                            ? "bg-gradient-to-r from-orange-400 to-red-500"
                            : "bg-gradient-to-r from-gray-400 to-gray-600"
                        }`}
                        style={{ width: `${item.percentage}%` }}
                        title={`${item.count}건`}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* 연령대 분포 */}
          <div>
            <div className="text-xs text-gray-600 font-medium mb-1.5">연령대 분포</div>
            <div className="grid grid-cols-3 gap-1.5">
              {ageDistribution.map((item, idx) => (
                <div key={idx} className="flex items-center gap-1">
                  <div className="w-10 text-[9px] text-gray-600">{item.age}</div>
                  <div className="flex-1 h-4 bg-gray-100 rounded-full overflow-hidden">
                    <div
                      className="h-full bg-gradient-to-r from-purple-400 to-indigo-500 rounded-full"
                      style={{ width: `${item.percentage}%` }}
                      title={`${item.count}건`}
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </>
  );
}

