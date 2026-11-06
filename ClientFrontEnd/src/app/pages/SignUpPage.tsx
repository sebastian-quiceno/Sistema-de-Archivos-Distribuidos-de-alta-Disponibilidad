import { useState } from "react";
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

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [wrongPassword, setWrongPassword] = useState(false);


  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");

    if(password !== confirmPassword){
      setWrongPassword(true);
      return;
    }

    try {
      const response = await signUp({
        username,
        password,
      });

      // Mapea la respuesta del backend al contexto
      login(response);

      console.log("Usuario autenticado");
    } catch (error) {
      setError(
        error instanceof Error ? error.message : "No se pudo registrar"
      );
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
              text="Nombre de usuario"
              isPassword={false}
              placeholder="Ingrese el nombre de su usuario"
              onchange={setUsername}
            />
          </div>
          <div className="flex-2">
            <LoginInput
              text="Contraseña"
              isPassword={true}
              placeholder="Ingrese su contraseña"
              onchange={setPassword}
            />
          </div>
          <div className="flex-z">
            <LoginInput
              text="Confirmar Contraseña"
              isPassword={true}
              placeholder="Escriba de nuevo la contraseña"
              onchange={setConfirmPassword}
              isWrong = {wrongPassword}
            />
          </div>
          <div className="flex-4">
            <p>
              ¿Ya tienes cuenta? <span>¡Accede a ella!</span>{" "}
            </p>
          </div>
          <div className="flex-5">
            <Button
              text="Volver"
              fontColor={RETURN_BUTTON_TEXT}
              backgroundColor={RETURN_BUTTON_BACKGROUND}
              onClick={() => console.log("Se retrocedio")}
            />
            <Button
              text="Acceder"
              fontColor={SUBMIT_BUTTON_TEXT}
              backgroundColor={SUBMIT_BUTTON_BACKGROUND}
              onClick={() => console.log("Se envio el formulario")}
            />
          </div>
        </form>
      </div>
    </div>
  );
}

export default SignUpPage;
