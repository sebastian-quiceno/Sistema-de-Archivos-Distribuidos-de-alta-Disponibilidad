import { useState } from "react";
import "../../styles/user/LoginInput.css";

interface LoginInputProps {
  id?: string;
  name?: string;
  text: string;
  placeholder?: string;
  isPassword: boolean;
  isWrong?: boolean;
  errorMessage?: string;
  autoComplete?: string;
  disabled?: boolean;
  onchange: (event: string) => void;
  isvariant?: boolean;
}

function LoginInput({
  id,
  name,
  text,
  placeholder = "Sin descripción",
  isPassword,
  isWrong,
  errorMessage,
  autoComplete,
  disabled,
  onchange,
  isvariant,
}: LoginInputProps) {
  const [isFocused, setIsFocused] = useState(false);
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);

  const inputId = id || (name ? `input-${name}` : undefined);

  return (
    <div className="form-item">
      <input
        id={inputId}
        name={name || "login"}
        className={
          isWrong ? "form__input form__input--wrong" : "form__input"
        }
        type={isPassword ? (isPasswordVisible ? "text" : "password") : "text"}
        placeholder={isFocused ? placeholder : ""}
        onFocus={() => setIsFocused(true)}
        onBlur={() => setIsFocused(false)}
        required
        autoComplete={autoComplete}
        disabled={disabled}
        onChange={(event) => onchange(event.target.value)}
      />
      <label htmlFor={inputId} className={isvariant ? "form-item__label--variant" : ""}>
        {text}
      </label>
      {isPassword
        ? SeePasswordButton(isPasswordVisible, () =>
          setIsPasswordVisible(!isPasswordVisible),
        )
        : null}

      {isWrong ? (
        <p className="wrong-message">
          {errorMessage || "La contraseña es incorrecta"}
        </p>
      ) : null}
    </div>
  );
}

export default LoginInput;

const SeePasswordButton = (
  isVisible: boolean,
  toggleVisible: () => void,
) => {
  return (
    <button
      type="button"
      className="btn-svg"
      onClick={toggleVisible}
      aria-label={isVisible ? "Ocultar contraseña" : "Ver contraseña"}
    >
      {isVisible ? (
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
