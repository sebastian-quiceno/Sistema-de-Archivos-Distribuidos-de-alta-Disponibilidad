import { useState } from "react";
import "../../styles/user/LoginInput.css";

interface LoginInputProps {
  text: string;
  placeholder?: string; // Opcional
  isPassword: boolean;
  isWrong?: boolean;
  onchange: (event: string) => void;
}

function LoginInput({
  text,
  placeholder = "Sin descripción",
  isPassword,
  isWrong,
  onchange,
}: LoginInputProps) {
  const [isFocused, setIsFocused] = useState(false);
  const [showPassword, setShowPassword] = useState(isPassword);

  return (
    <div className="form-item">
      <input
        className={
          isWrong ? "form__input form__input--wrong" : "form__input"
        }
        type={showPassword ? "password" : "text"}
        name="login"
        placeholder={isFocused ? placeholder : ""}
        onFocus={() => setIsFocused(true)}
        onBlur={() => setIsFocused(false)}
        required
        autoComplete="off"
        onChange={(event) => onchange(event.target.value)}
      />
      <label>{text}</label>
      {isPassword ? SeePasswordButton(showPassword, setShowPassword) : null}

      {isWrong ? <p className="wrong-message">La contraseña es incorrecta</p> : null}
    </div>
  );
}

export default LoginInput;

const SeePasswordButton = (
  seePassword: boolean,
  setSeePassword: (a: boolean) => void,
) => {
  return (
    <button
      type="button"
      className="btn-svg"
      onClick={() => setSeePassword(!seePassword)}
      aria-label={seePassword ? "Ocultar contraseña" : "Ver contraseña"}
    >
      {seePassword ? (
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 24 24"
          className="seeButon__svg"
        >
          <path d="m15 18-.722-3.25" />
          <path d="M2 8a10.645 10.645 0 0 0 20 0" />
          <path d="m20 15-1.726-2.05" />
          <path d="m4 15 1.726-2.05" />
          <path d="m9 18 .722-3.25" />
        </svg>
      ) : (
        <svg
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 24 24"
          className="seeButon__svg"
        >
          <path d="M2.062 12.348a1 1 0 0 1 0-.696 10.75 10.75 0 0 1 19.876 0 1 1 0 0 1 0 .696 10.75 10.75 0 0 1-19.876 0" />
          <circle cx="12" cy="12" r="3" />
        </svg>
      )}
    </button>
  );
};
