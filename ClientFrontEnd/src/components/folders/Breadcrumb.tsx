import type { SVGProps } from "react";
import "../../styles/folder/Breadcrumb.css";

export interface BreadcrumbItem {
  id: string;
  name: string;
}

interface BreadcrumbProps {
  items: BreadcrumbItem[];
  onNavigate: (folderId: string) => void;
}

function ChevronRightIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg
      {...props}
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.5"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <polyline points="9 18 15 12 9 6" />
    </svg>
  );
}

export function Breadcrumb({ items, onNavigate }: BreadcrumbProps) {
  if (!items || items.length === 0) {
    return null;
  }

  return (
    <nav className="breadcrumb" aria-label="Ruta de carpetas">
      <ol className="breadcrumb__list">
        {items.map((item, index) => {
          const isLast = index === items.length - 1;

          return (
            <li key={item.id} className="breadcrumb__item-wrapper">
              {isLast ? (
                <span
                  className="breadcrumb__item breadcrumb__item--active"
                  aria-current="page"
                >
                  {item.name}
                </span>
              ) : (
                <button
                  type="button"
                  className="breadcrumb__item breadcrumb__item--clickable"
                  onClick={() => onNavigate(item.id)}
                  title={`Ir a ${item.name}`}
                >
                  {item.name}
                </button>
              )}

              {!isLast && (
                <ChevronRightIcon
                  className="breadcrumb__separator"
                  aria-hidden="true"
                />
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}

