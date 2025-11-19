import { ReactNode } from "react";

interface TableProps {
  children: ReactNode;
  className?: string;
}

interface TableHeaderProps {
  children: ReactNode;
  className?: string;
}

interface TableBodyProps {
  children: ReactNode;
  className?: string;
}

interface TableRowProps {
  children: ReactNode;
  variant?: "default" | "alert" | "warning";
  className?: string;
  onClick?: () => void;
}

interface TableCellProps {
  children: ReactNode;
  header?: boolean;
  className?: string;
}

export function Table({ children, className = "" }: TableProps) {
  return (
    <div className="w-full">
      <table className={`w-full border-collapse table-fixed ${className}`}>
        {children}
      </table>
    </div>
  );
}

export function TableHeader({ children, className = "" }: TableHeaderProps) {
  return <thead className={`bg-gray-200 ${className}`}>{children}</thead>;
}

export function TableBody({ children, className = "" }: TableBodyProps) {
  return <tbody className={className}>{children}</tbody>;
}

export function TableRow({
  children,
  variant = "default",
  className = "",
  onClick,
}: TableRowProps) {
  const variantStyles = {
    default: "bg-white hover:bg-gray-50",
    alert: "bg-amber-50 hover:bg-amber-100",
    warning: "bg-red-50 hover:bg-red-100",
  };

  return (
    <tr
      className={`border-b border-gray-200 transition-colors ${
        variantStyles[variant]
      } ${onClick ? "cursor-pointer" : ""} ${className}`}
      onClick={onClick}
    >
      {children}
    </tr>
  );
}

export function TableCell({
  children,
  header = false,
  className = "",
}: TableCellProps) {
  const baseStyles =
    "px-3 py-2 text-center text-sm border-r border-gray-200 last:border-r-0";
  const headerStyles = header
    ? "font-semibold text-gray-900"
    : "text-gray-800";

  if (header) {
    return (
      <th className={`${baseStyles} ${headerStyles} ${className}`}>
        {children}
      </th>
    );
  }

  return (
    <td className={`${baseStyles} ${headerStyles} ${className}`}>{children}</td>
  );
}
