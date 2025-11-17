import { useState, useEffect, forwardRef, useMemo } from "react";
import Sidebar from "@/components/layout/Sidebar";
import { Header } from "@/components";
import { useAuthStore } from "@/features/auth/store/authStore";
import { fetchTimeStatisticsApi } from "@/features/statistics/api/statisticsApi";
import type { TimeStatisticsResponse } from "@/features/statistics/types/statistics.types";
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";
import DatePicker from "react-datepicker";
import "react-datepicker/dist/react-datepicker.css";

type RangeKey = "week" | "month" | "all" | "custom";

export default function TimeAnalysisPage() {
  const { user } = useAuthStore();
  const [data, setData] = useState<TimeStatisticsResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 현재 “선택된” 범위(프리셋/커스텀 구분용)
  const [dateRange, setDateRange] = useState<RangeKey>("month");

  // 화면과 API가 참조하는 실제 범위
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

  // 유틸
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

  // 프리셋 버튼 클릭 시: 실제 Date 상태도 채워 넣기
  const applyPreset = (key: Exclude<RangeKey, "custom">) => {
    const { start, end } = calcPresetRange(key);
    setDateRange(key);
    setStartDate(start);
    setEndDate(end);
  };

  // 최초 마운트 시 기본(month) 채움
  useEffect(() => {
    if (startDate && endDate) return;
    const { start, end } = calcPresetRange("month");
    setStartDate(start);
    setEndDate(end);
  }, []); // once

  // 데이터 로드: startDate/endDate가 모두 있을 때만
  useEffect(() => {
    if (!user?.id || !startDate || !endDate) return;

    (async () => {
      setLoading(true);
      setError(null);
      try {
        const result = await fetchTimeStatisticsApi({
          hospitalId: user.id,
          startDate: fmt(startDate),
          endDate: fmt(endDate),
        });
        setData(result);
        // console.log("📊 시간 통계 조회 성공:", result);
      } catch (err) {
        setError(err instanceof Error ? err.message : "통계 조회에 실패했습니다.");
      } finally {
        setLoading(false);
      }
    })();
  }, [user?.id, startDate, endDate]);

  // 커스텀 활성(파란색) 여부: 사용자가 DatePicker로 직접 선택 & 둘 다 존재
  const isCustomActive = dateRange === "custom" && !!startDate && !!endDate;

  // 요일별 차트 데이터
  const dayOfWeekData = data
    ? [
        { name: "월", count: data.byDayOfWeek.MONDAY || 0 },
        { name: "화", count: data.byDayOfWeek.TUESDAY || 0 },
        { name: "수", count: data.byDayOfWeek.WEDNESDAY || 0 },
        { name: "목", count: data.byDayOfWeek.THURSDAY || 0 },
        { name: "금", count: data.byDayOfWeek.FRIDAY || 0 },
        { name: "토", count: data.byDayOfWeek.SATURDAY || 0 },
        { name: "일", count: data.byDayOfWeek.SUNDAY || 0 },
      ]
    : [];

  // 시간대별 차트 데이터 (0~23시)
  const hourlyData = data
    ? Array.from({ length: 24 }, (_, hour) => ({
        name: `${hour}시`,
        count: Number(data.byHour[hour.toString()]) || 0,
      }))
    : [];

  return (
    <div className="h-screen flex overflow-hidden">
      <Sidebar />

      {/* 메인 컨텐츠 */}
      <div className="flex-1 flex flex-col overflow-hidden bg-gray-50">
        <Header />

        {/* Main Content */}
        <div className="flex-1 flex flex-col p-4 sm:p-6 lg:p-8 min-h-0">
          <div className="max-w-full h-full flex flex-col gap-4">
            {/* 상단: 필터 행 */}
            <div className="flex gap-3 items-stretch justify-between">
              {/* 총 수용 건수 */}
              <div className="relative h-14 bg-white rounded-lg shadow-sm border border-gray-200 px-5 flex items-center justify-between min-w-[280px]">
                {/* 좌측: 메인 수치 */}
                <div className="flex items-center gap-3">
                  <span className="text-sm leading-none text-gray-600 whitespace-nowrap">총 수용 건수</span>
                  <span className="text-xl leading-none font-bold text-sky-500 whitespace-nowrap">
                    {data ? `${data.totalCount}건` : "-"}
                  </span>
                </div>

                {/* 우측: 상세 팝오버 트리거 */}
                {data && (
                  <PopoverSummary
                    startDate={data.startDate}
                    endDate={data.endDate}
                    byHour={data.byHour}
                    byDayOfWeek={data.byDayOfWeek}
                    totalCount={data.totalCount}
                  />
                )}
              </div>

              {/* 날짜 필터 버튼들 */}
              <div className="flex gap-3">
              {/* 프리셋 */}
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

            {/* 하단: 요일별/시간대별 차트 */}
            {!loading && !error && data && (
              <div className="flex-1 grid grid-cols-1 lg:grid-cols-2 gap-4 min-h-0">
                {/* 요일별 바차트 */}
                <div className="bg-white rounded-lg shadow-lg p-6 flex flex-col min-h-0">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">요일별 환자 수용 건수</h3>
                  <div className="flex-1 min-h-0">
                    <ResponsiveContainer width="100%" height="100%">
                      <BarChart data={dayOfWeekData} margin={{ top: 8, right: 12, bottom: 4, left: 8 }}>
                        {/* 가로 격자선만 아주 옅게 */}
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
                          formatter={(v: number) => [`${v}건`, "수용 건수"]}
                          labelStyle={{ color: "#111827" }}
                          contentStyle={{ borderRadius: 10, borderColor: "#E5E7EB" }}
                        />
                        <Bar dataKey="count" name="수용 건수" fill="#0ea5e9" radius={[6, 6, 0, 0]} maxBarSize={40} animationBegin={0}>
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>

                {/* 시간대별 라인차트 */}
                <div className="bg-white rounded-lg shadow-lg p-6 flex flex-col min-h-0">
                  <h3 className="text-lg font-semibold text-gray-900 mb-4">시간대별 환자 수용 건수</h3>
                  <div className="flex-1 min-h-0">
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart data={hourlyData} margin={{ top: 8, right: 12, bottom: 4, left: 8 }}>
                        {/* 가로 격자선만 아주 옅게 */}
                        <CartesianGrid vertical={false} stroke="#E5E7EB" strokeOpacity={0.35} />
                        <XAxis
                          dataKey="name"
                          tick={{ fill: "#6B7280", fontSize: 12 }}
                          axisLine={{ stroke: "#E5E7EB" }}
                          tickLine={false}
                          interval={1}
                        />
                        <YAxis
                          allowDecimals={false}
                          tick={{ fill: "#6B7280", fontSize: 12 }}
                          axisLine={{ stroke: "#E5E7EB" }}
                          tickLine={false}
                          domain={[0, "dataMax + 1"]}
                        />
                        <Tooltip
                          formatter={(v: number) => [`${v}건`, "수용 건수"]}
                          labelFormatter={(label) => `시간대: ${label}`}
                          contentStyle={{ borderRadius: 10, borderColor: "#E5E7EB" }}
                          labelStyle={{ color: "#111827" }}
                        />
                        <Line
                          type="monotone"
                          dataKey="count"
                          stroke="#0ea5e9"
                          strokeWidth={2}
                          dot={false}
                          activeDot={{ r: 4 }}
                          animationBegin={0}
                        />
                      </LineChart>
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
  byHour,
  byDayOfWeek,
  totalCount,
}: {
  startDate: string;
  endDate: string;
  byHour: Record<string, number>;
  byDayOfWeek: Record<string, number>;
  totalCount: number;
}) {
  const [open, setOpen] = useState(false);

  const dayKorean: Record<string,string> = {
    MONDAY: "월", TUESDAY: "화", WEDNESDAY: "수",
    THURSDAY: "목", FRIDAY: "금", SATURDAY: "토", SUNDAY: "일"
  };

  const dayOrder = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"];

  // 요일별 데이터 계산
  const dayData = useMemo(() => {
    const days = dayOrder.map(key => ({
      key,
      label: dayKorean[key],
      count: Number(byDayOfWeek?.[key] || 0)
    }));
    const maxCount = Math.max(...days.map(d => d.count), 1);
    return days.map(d => ({
      ...d,
      ratio: totalCount > 0 ? Math.round((d.count / totalCount) * 100) : 0,
      barWidth: maxCount > 0 ? Math.round((d.count / maxCount) * 100) : 0
    }));
  }, [byDayOfWeek, totalCount]);

  const topDay = useMemo(() => {
    const sorted = [...dayData].sort((a,b) => b.count - a.count);
    return sorted[0];
  }, [dayData]);

  // 시간대별 데이터 계산
  const hourData = useMemo(() => {
    const hours = Array.from({length: 24}, (_, h) => ({
      hour: h,
      count: Number(byHour?.[String(h)] || 0)
    }));
    return hours;
  }, [byHour]);

  const peakHour = useMemo(() => {
    const sorted = [...hourData].sort((a,b) => b.count - a.count);
    return sorted[0];
  }, [hourData]);

  // 시간대별 구간 통계 (새벽/오전/오후/밤)
  const timeRangeData = useMemo(() => {
    const dawn = hourData.slice(0, 6).reduce((sum, h) => sum + h.count, 0); // 0-5시
    const morning = hourData.slice(6, 12).reduce((sum, h) => sum + h.count, 0); // 6-11시
    const afternoon = hourData.slice(12, 18).reduce((sum, h) => sum + h.count, 0); // 12-17시
    const night = hourData.slice(18, 24).reduce((sum, h) => sum + h.count, 0); // 18-23시

    const ranges = [
      { label: "새벽(0-5시)", count: dawn, color: "from-indigo-400 to-purple-500" },
      { label: "오전(6-11시)", count: morning, color: "from-amber-400 to-orange-500" },
      { label: "오후(12-17시)", count: afternoon, color: "from-sky-400 to-blue-500" },
      { label: "밤(18-23시)", count: night, color: "from-violet-400 to-purple-600" }
    ];

    const maxCount = Math.max(...ranges.map(r => r.count), 1);
    return ranges.map(r => ({
      ...r,
      ratio: totalCount > 0 ? Math.round((r.count / totalCount) * 100) : 0,
      barWidth: maxCount > 0 ? Math.round((r.count / maxCount) * 100) : 0
    }));
  }, [hourData, totalCount]);

  return (
    <>
      <button
        onClick={() => setOpen(o => !o)}
        className="text-xs leading-none px-3 py-2 rounded-md border border-gray-200 hover:bg-gray-50 text-gray-600"
        aria-expanded={open}
      >
        상세 보기
      </button>

      {open && (
        <div
          className="absolute left-0 top-14 z-20 bg-white border border-gray-200 rounded-xl shadow-xl p-4 max-h-[80vh] overflow-y-auto"
          style={{ width: "520px" }}
          role="dialog"
        >
          {/* 헤더 */}
          <div className="flex items-start justify-between mb-3">
            <div>
              <div className="text-xs text-gray-500">선택 기간</div>
              <div className="text-xs font-medium text-gray-900">{startDate} ~ {endDate}</div>
            </div>
            <button
              className="text-gray-400 hover:text-gray-600"
              onClick={() => setOpen(false)}
              aria-label="닫기"
            >
              ✕
            </button>
          </div>

          {/* 상단 요약 카드 2개 */}
          <div className="grid grid-cols-2 gap-2 mb-3">
            <div className="rounded-lg p-2.5 bg-gradient-to-br from-sky-50 to-blue-50 border border-sky-100">
              <div className="text-[10px] text-gray-600">총 수용 건수</div>
              <div className="text-lg font-bold text-sky-600">{totalCount}건</div>
            </div>
            <div className="rounded-lg p-2.5 bg-gradient-to-br from-purple-50 to-pink-50 border border-purple-100">
              <div className="text-[10px] text-gray-600">피크 시간대</div>
              <div className="text-lg font-bold text-purple-600">{peakHour.hour}시 ({peakHour.count}건)</div>
            </div>
          </div>

          {/* 2열 그리드: 시간대 구간 + 요일별 */}
          <div className="grid grid-cols-2 gap-3 mb-3">
            {/* 시간대별 구간 분포 */}
            <div>
              <div className="text-xs font-medium text-gray-700 mb-1.5">시간대 구간</div>
              <div className="space-y-1.5">
                {timeRangeData.map((range, idx) => (
                  <div key={idx} className="flex items-center gap-1.5">
                    <div className="w-16 text-[9px] text-gray-600 shrink-0">{range.label}</div>
                    <div className="flex-1 h-4 bg-gray-100 rounded-full overflow-hidden">
                      <div
                        className={`h-full bg-gradient-to-r ${range.color} rounded-full`}
                        style={{ width: `${range.barWidth}%` }}
                        title={`${range.count}건 (${range.ratio}%)`}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* 요일별 분포 */}
            <div>
              <div className="text-xs font-medium text-gray-700 mb-1.5">요일별 (최다: {topDay.label})</div>
              <div className="space-y-1.5">
                {dayData.map((day) => (
                  <div key={day.key} className="flex items-center gap-1.5">
                    <div className="w-5 text-[9px] text-gray-600 shrink-0">{day.label}</div>
                    <div className="flex-1 h-4 bg-gray-100 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-gradient-to-r from-emerald-400 to-teal-500 rounded-full"
                        style={{ width: `${day.barWidth}%` }}
                        title={`${day.count}건 (${day.ratio}%)`}
                      />
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* 시간대별 상세 분포 (24시간) */}
          <div>
            <div className="text-xs font-medium text-gray-700 mb-1.5">24시간 상세 분포</div>
            <div className="grid grid-cols-12 gap-0.5">
              {hourData.map((h) => {
                const maxCount = Math.max(...hourData.map(hh => hh.count), 1);
                const heightPercent = Math.round((h.count / maxCount) * 100);
                return (
                  <div key={h.hour} className="flex flex-col items-center gap-0.5">
                    <div className="h-8 w-full flex items-end">
                      <div
                        className="w-full bg-gradient-to-t from-sky-400 to-blue-500 rounded-t"
                        style={{ height: `${heightPercent}%` }}
                        title={`${h.hour}시: ${h.count}건`}
                      />
                    </div>
                    <div className="text-[7px] text-gray-500">{h.hour}</div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      )}
    </>
  );
}

