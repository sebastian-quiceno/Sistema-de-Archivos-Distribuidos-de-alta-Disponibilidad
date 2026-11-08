import { useEffect, useRef, useState } from "react";
import "../../styles/user/UserPerfileButton.css";

import { useAuth } from "../../hooks/users/useAuth";

export function UserPerfileButton() {
  const { auth, logout } = useAuth();
  const [isOpen, setIsOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!isOpen) return;

    function handleClickOutside(event: MouseEvent) {
      if (
        containerRef.current &&
        !containerRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
      }
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setIsOpen(false);
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    document.addEventListener("keydown", handleKeyDown);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [isOpen]);

  const userName = auth?.userName || "Usuario";
  const userId = auth?.userId || "N/A";

  return (
    <div className="profile-wrapper" ref={containerRef}>
      <button
        className="profile__trigger"
        type="button"
        onClick={() => setIsOpen((prev) => !prev)}
        aria-expanded={isOpen}
        aria-haspopup="true"
        title="Opciones de perfil"
      >
        <span className="profile__name">{userName}</span>
        <div className="profile__avatar">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            className="profile__ico"
          >
            <path d="M17.925 20.056a6 6 0 0 0-11.851.001" />
            <circle cx="12" cy="11" r="4" />
            <circle cx="12" cy="12" r="10" />
          </svg>
        </div>
      </button>

      {isOpen && (
        <div className="profile-dropdown" role="menu">
          <div className="profile-dropdown__header">
            <p className="profile-dropdown__user-name">{userName}</p>
            <p className="profile-dropdown__user-id" title={userId}>
              ID: <span>{userId}</span>
            </p>
          </div>
          <hr className="profile-dropdown__divider" />
          <button
            className="profile-dropdown__logout"
            type="button"
            role="menuitem"
            onClick={() => {
              setIsOpen(false);
              logout();
            }}
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              className="profile-dropdown__logout-ico"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
            >
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
              <polyline points="16 17 21 12 16 7" />
              <line x1="21" y1="12" x2="9" y2="12" />
            </svg>
            Cerrar sesión
          </button>
        </div>
      )}
    </div>
  );
}
