import { useState, useEffect } from "react";
import "../styles/SignInPage.css";

import beigeLogo from "../../../assets/AilatanLogoBeige.png";

import LoginInput from "../components/LoginInput";
import {Button} from '../components/Button'

const SUBMIT_BUTTON_BACKGROUND = "#D5B6A1";
const SUBMIT_BUTTON_TEXT = "#573B2C";
const RETURN_BUTTON_BACKGROUND = undefined;
const RETURN_BUTTON_TEXT = "#7B7D9F";


function SignInPage() {
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
        <div className="modal__form">
          <div className="flex-1">
            <LoginInput text="Nombre de usuario" isPassword={false} placeholder="Ingrese el nombre de su usuario"/>

          </div>
          <div className="flex-2">
            <LoginInput text="Contraseña" isPassword={true} placeholder="Ingrese su contraseña"/>
          </div>
          <div className="flex-3">
            <p>¿No tienes cuenta?  <span>¡Crea Una!</span> </p>
          </div>
          <div className="flex-4">
            <Button text="Volver" fontColor={RETURN_BUTTON_TEXT} backgroundColor={RETURN_BUTTON_BACKGROUND} onClick={()=>console.log("Se retrocedio")} />
            <Button text="Acceder" fontColor={SUBMIT_BUTTON_TEXT} backgroundColor={SUBMIT_BUTTON_BACKGROUND} onClick={()=>console.log("Se envio el formulario")} />
          </div>
        </div>
      </div>
    </div>
  );
}

export default SignInPage;
