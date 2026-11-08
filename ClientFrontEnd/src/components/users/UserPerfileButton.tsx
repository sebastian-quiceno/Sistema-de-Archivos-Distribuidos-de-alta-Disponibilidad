import '../../styles/user/UserPerfileButton.css'

import { useAuth } from "../../hooks/users/useAuth";

export function UserPerfileButton() {
  const { auth, logout } = useAuth();

  return (
    <div className='profile-wrapper'>
      <h3 className="profile__name">{auth?.userName}</h3>
      <button
        className='profile__button'
        type="button"
        onClick={logout}
        title="Cerrar sesión"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 24 24"
          className="profile__ico"
        >
          <path d="M17.925 20.056a6 6 0 0 0-11.851.001" />
          <circle cx="12" cy="11" r="4" />
          <circle cx="12" cy="12" r="10" />
        </svg>
      </button>
    </div>
  );
}
