import leftArrow from "@/assets/left-arrow.png";
import rightArrow from "@/assets/right-arrow.png";

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export default function Pagination({
  currentPage,
  totalPages,
  onPageChange,
}: PaginationProps) {
  const getPageNumbers = () => {
    const pages: (number | string)[] = [];
    const maxVisible = 5;

    if (totalPages <= maxVisible) {
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      if (currentPage <= 3) {
        for (let i = 1; i <= 4; i++) {
          pages.push(i);
        }
        pages.push("...");
        pages.push(totalPages);
      } else if (currentPage >= totalPages - 2) {
        pages.push(1);
        pages.push("...");
        for (let i = totalPages - 3; i <= totalPages; i++) {
          pages.push(i);
        }
      } else {
        pages.push(1);
        pages.push("...");
        pages.push(currentPage - 1);
        pages.push(currentPage);
        pages.push(currentPage + 1);
        pages.push("...");
        pages.push(totalPages);
      }
    }

    return pages;
  };

  return (
    <div className="flex items-center justify-center gap-2 py-3">
      {/* 이전 버튼 */}
      <button
        onClick={() => onPageChange(currentPage - 1)}
        disabled={currentPage === 1}
        className="w-[30px] h-[30px] rounded-lg bg-white border border-gray-300 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
      >
        <img
          src={leftArrow}
          alt="이전"
          className="w-4 h-4 opacity-70"
        />
      </button>

      {/* 페이지 번호 */}
      {getPageNumbers().map((page, index) => {
        if (page === "...") {
          return (
            <span key={`ellipsis-${index}`} className="px-2 text-gray-500">
              ...
            </span>
          );
        }

        return (
          <button
            key={page}
            onClick={() => onPageChange(page as number)}
            className={`w-[30px] h-[30px] rounded-lg transition-colors flex items-center justify-center ${
              currentPage === page
                ? "bg-sky-500 text-white font-semibold shadow-sm"
                : "bg-white border border-gray-300 text-gray-700 hover:bg-gray-100"
            }`}
          >
            {page}
          </button>
        );
      })}

      {/* 다음 버튼 */}
      <button
        onClick={() => onPageChange(currentPage + 1)}
        disabled={currentPage === totalPages}
        className="w-[30px] h-[30px] rounded-lg bg-white border border-gray-300 hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
      >
        <img
          src={rightArrow}
          alt="다음"
          className="w-4 h-4 opacity-70"
        />
      </button>
    </div>
  );
}
