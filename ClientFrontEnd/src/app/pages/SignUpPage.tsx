import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "../../styles/pages/SignUpPage.css";
import beigeLogo from "../../assets/AilatanLogoBeige.png";

import { signUp } from "../../services/userService";
import { useAuth } from "../../hooks/users/useAuth";

import LoginInput from "../../components/users/LoginInput";
import { Button } from "../../components/users/Button";

const SUBMIT_BUTTON_BACKGROUND = "#D5B6A1";
const SUBMIT_BUTTON_TEXT = "#573B2C";
const RETURN_BUTTON_BACKGROUND = undefined;
const RETURN_BUTTON_TEXT = "#7B7D9F";

function SignUpPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [wrongPassword, setWrongPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setWrongPassword(false);

    const trimmedUsername = username.trim();
    if (!trimmedUsername || !password || !confirmPassword) {
      setError("Por favor complete todos los campos");
      return;
    }

    if (password !== confirmPassword) {
      setWrongPassword(true);
      setError("Las contraseñas no coinciden");
      return;
    }

    setIsLoading(true);
    try {
      const response = await signUp({
        username: trimmedUsername,
        password,
      });

      // Mapea la respuesta del backend al contexto
      login(response);

      console.log("Usuario autenticado");
      navigate("/", { replace: true });
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "No se pudo registrar",
      );
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div className="sign-up-wrapper">
      <div className="register-modal">
        <div className="modal__information">
          <img src={beigeLogo} alt="" />
          <div className="information__text">
            <h2>Crea tu cuenta</h2>
            <h3>
              Despues de crearla vas a poder usar <span>Ailatan</span>
            </h3>
          </div>
        </div>
        <form className="modal__form" onSubmit={handleSubmit}>
          <div className="flex-1">
            <LoginInput
              id="signup-username"
              name="username"
              text="Nombre de usuario"
              isPassword={false}
              placeholder="Ingrese el nombre de su usuario"
              autoComplete="username"
              disabled={isLoading}
              onchange={(val) => {
                setUsername(val);
                if (error) setError("");
              }}
            />
          </div>
          <div className="flex-2">
            <LoginInput
              id="signup-password"
              name="password"
              text="Contraseña"
              isPassword={true}
              placeholder="Ingrese su contraseña"
              autoComplete="new-password"
              disabled={isLoading}
              onchange={(val) => {
                setPassword(val);
                if (wrongPassword) setWrongPassword(false);
                if (error) setError("");
              }}
            />
          </div>
          <div className="flex-z">
            <LoginInput
              id="signup-confirm-password"
              name="confirmPassword"
              text="Confirmar Contraseña"
              isPassword={true}
              placeholder="Escriba de nuevo la contraseña"
              autoComplete="new-password"
              disabled={isLoading}
              onchange={(val) => {
                setConfirmPassword(val);
                if (wrongPassword) setWrongPassword(false);
                if (error) setError("");
              }}
              isWrong={wrongPassword}
              errorMessage="Las contraseñas no coinciden"
            />
          </div>
          <div className="flex-4">
            <p>
              ¿Ya tienes cuenta?{" "}
              <Link to="/signin" style={{ textDecoration: "none" }}>
                <span>¡Accede a ella!</span>
              </Link>
            </p>
          </div>
          <div className="flex-5">
            <Button
              text="Volver"
              fontColor={RETURN_BUTTON_TEXT}
              backgroundColor={RETURN_BUTTON_BACKGROUND}
              onClick={() => navigate("/signin")}
              disabled={isLoading}
            />
            <Button
              text={isLoading ? "Registrando..." : "Crear Cuenta"}
              fontColor={SUBMIT_BUTTON_TEXT}
              backgroundColor={SUBMIT_BUTTON_BACKGROUND}
              isSubmit={true}
              disabled={isLoading}
            />
          </div>
          {error && <p className="error-message">{error}</p>}
        </form>
      </div>
    </div>
  );
}

export default SignUpPage;
