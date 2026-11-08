import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import "../../styles/pages/SignInPage.css";
import beigeLogo from "../../assets/AilatanLogoBeige.png";

import { signIn } from "../../services/userService";
import { useAuth } from "../../hooks/users/useAuth";


import LoginInput from "../../components/users/LoginInput";
import { Button } from "../../components/users/Button";

const SUBMIT_BUTTON_BACKGROUND = "#D5B6A1";
const SUBMIT_BUTTON_TEXT = "#573B2C";
const RETURN_BUTTON_BACKGROUND = undefined;
const RETURN_BUTTON_TEXT = "#7B7D9F";

function SignInPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");

    const trimmedUsername = username.trim();
    if (!trimmedUsername || !password) {
      setError("Por favor complete todos los campos");
      return;
    }

    setIsLoading(true);
    try {
      const response = await signIn({
        username: trimmedUsername,
        password,
      });

      // Mapea la respuesta del backend al contexto
      login(response);

      console.log("Usuario autenticado");
      navigate("/", { replace: true });
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "No se pudo iniciar sesión",
      );
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div className="sign-in-wrapper">
      <div className="login-modal">
        <div className="modal__information">
          <img src={beigeLogo} alt="" />
          <div className="information__text">
            <h2>Accede a tu cuenta</h2>
            <h3>Identificate para poder entrar a Ailatan</h3>
          </div>
        </div>
        <form className="modal__form" onSubmit={handleSubmit}>
          <div className="flex-1">
            <LoginInput
              id="signin-username"
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
              id="signin-password"
              name="password"
              text="Contraseña"
              isPassword={true}
              placeholder="Ingrese su contraseña"
              autoComplete="current-password"
              disabled={isLoading}
              onchange={(val) => {
                setPassword(val);
                if (error) setError("");
              }}
            />
          </div>
          <div className="flex-3">
            <p>
              ¿No tienes cuenta?{" "}
              <Link to="/signup" style={{ textDecoration: "none" }}>
                <span>¡Crea Una!</span>
              </Link>
            </p>
          </div>
          <div className="flex-4">
            <Button
              text="Volver"
              fontColor={RETURN_BUTTON_TEXT}
              backgroundColor={RETURN_BUTTON_BACKGROUND}
              onClick={() => navigate(-1)}
              disabled={isLoading}
            />
            <Button
              text={isLoading ? "Accediendo..." : "Acceder"}
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

export default SignInPage;
