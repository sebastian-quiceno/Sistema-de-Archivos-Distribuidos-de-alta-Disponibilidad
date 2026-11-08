# Tarea FE-03: Corrección funcional, validaciones y UX en formularios de autenticación

## 1. Identificación
- **ID:** FE-03
- **Título:** Corrección funcional, validaciones y experiencia de usuario en formularios de inicio de sesión y registro
- **Dominio:** Frontend
- **Tipo:** Error / Funcionalidad incompleta / UX
- **Prioridad:** Alta

---

## 2. Problema
### Estado actual
Los formularios de autenticación (`SignInPage` y `SignUpPage`) presentan múltiples defectos críticos que impiden o degradan la experiencia del usuario:

1. **Bug en `Button.tsx` (Evaluación booleana de `isSubmit`):**
   ```tsx
   type={(isSubmit === null || isSubmit === false)? "button": "submit"}
   ```
   Cuando `isSubmit` no se suministra a la propiedad (su valor es `undefined`), `(undefined === null || undefined === false)` resulta `false`, lo que evalúa el botón con `type="submit"`. Como consecuencia, el botón "Volver" en `SignInPage` y `SignUpPage` envía el formulario accidentalmente cada vez que se presiona.

2. **Formulario de Registro inoperante (`SignUpPage.tsx`):**
   - El botón principal tiene la etiqueta `"Acceder"` en lugar de `"Registrarse"` o `"Crear Cuenta"`.
   - El botón tiene asignado un handler `onClick={() => console.log("Se envio el formulario")}` y carece de `isSubmit={true}`. Por tanto, hacer clic en el botón principal NUNCA envía el formulario.
   - El estado de error capturado en el bloque `catch` (`setError(...)`) no se renderiza en ninguna parte del JSX.
   - Si la validación de contraseña falla (`password !== confirmPassword`), se marca `setWrongPassword(true)` pero no se ofrece un mensaje accesible o descriptivo.

3. **Navegación rota entre login y registro:**
   - En `SignInPage.tsx`, el texto `¿No tienes cuenta? <span>¡Crea Una!</span>` no tiene handler `onClick` ni enlace para ir a registro.
   - En `SignUpPage.tsx`, el texto `¿Ya tienes cuenta? <span>¡Accede a ella!</span>` no tiene handler ni enlace para ir a login.
   - Los botones "Volver" solo imprimen en consola (`console.log("Se retrocedio")`).

4. **Componente `LoginInput.tsx`:**
   - Atributo `name="login"` está hardcodeado para todos los campos (usuario, contraseña, confirmación).
   - Inversión en el botón de visibilidad de contraseña: cuando el campo es `type="password"`, el icono muestra pestañas cerradas y el aria-label indica "Ocultar contraseña"; al revelarse la contraseña el icono cambia a ojo abierto con aria-label "Ver contraseña", cuando la convención accesible es la inversa.
   - Las etiquetas `<label>` no están enlazadas a los campos mediante `htmlFor` e `id`.

5. **Falta de estados de carga (Loading/Pending):**
   - No hay deshabilitación de botones ni indicador visual de progreso durante la comunicación HTTP con `/api/auth/login` o `/api/auth/register`, permitiendo múltiples envíos consecutivos (doble clic).

### Impacto
Los usuarios no pueden registrarse a través de la interfaz visual. Intentar navegar hacia atrás envía el formulario con datos inválidos. No se muestra retroalimentación de errores y la accesibilidad de los formularios es muy deficiente.

---

## 3. Ubicación
- `ClientFrontEnd/src/app/pages/SignInPage.tsx`
- `ClientFrontEnd/src/app/pages/SignUpPage.tsx`
- `ClientFrontEnd/src/components/users/Button.tsx`
- `ClientFrontEnd/src/components/users/LoginInput.tsx`
- `ClientFrontEnd/src/styles/user/LoginInput.css`
- `ClientFrontEnd/src/styles/pages/SignInPage.css`
- `ClientFrontEnd/src/styles/pages/SignUpPage.css`

---

## 4. Objetivo
Lograr un flujo completo y confiable de autenticación y registro de usuarios, con validaciones en cliente, retroalimentación visual de errores devueltos por el backend, alternancia clara entre pantallas de login y registro, e insumos de formulario totalmente accesibles.

---

## 5. Requisitos
- **Corrección en `Button.tsx`:** Ajustar la evaluación del tipo de botón a `type={isSubmit ? "submit" : "button"}` para que los botones sin la prop sean de tipo `button` por defecto.
- **Corrección en `SignUpPage.tsx`:**
  - Cambiar el texto del botón de envío a "Crear Cuenta" o "Registrarse".
  - Configurar `isSubmit={true}` en el botón de acción principal para despachar el evento `onSubmit` del formulario.
  - Mostrar en pantalla el mensaje de error `error` devuelto por el backend (ej: "El nombre de usuario ya existe" con status 409).
  - Validar que las contraseñas coincidan y que los campos no estén vacíos antes de enviar.
- **Navegación e interacción:**
  - Convertir los enlaces secundarios en botones o vínculos de enrutamiento funcionales: de login a `/signup` y de registro a `/signin`.
  - Configurar el botón "Volver" para navegar hacia la ruta previa o a la pantalla de bienvenida.
- **Mejoras en `LoginInput.tsx`:**
  - Permitir pasar las propiedades `id`, `name` y `autoComplete` dinámicas (ej: `name="username"`, `name="password"`).
  - Enlazar la etiqueta `<label htmlFor={id}>` con el `<input id={id}>`.
  - Corregir los iconos y textos de accesibilidad en el toggle de visualización de contraseña.
- **Prevención de envíos concurrentes:**
  - Agregar un estado `isSubmitting` o `isLoading` durante la petición; deshabilitar los botones de acción y mostrar un spinner o texto "Accediendo..." / "Registrando...".

---

## 6. Alcance
- **Cambios necesarios:**
  - Edición de lógica y props en `Button.tsx` y `LoginInput.tsx`.
  - Conexión de enrutamiento y estado de carga en `SignInPage.tsx` y `SignUpPage.tsx`.
  - Estilización uniforme de mensajes de error de validación y de servidor.
- **Restricciones:**
  - Mantener los contratos de servicio existentes en `userService.signIn` y `userService.signUp`.

---

## 7. Criterios de aceptación
- Presionar el botón "Volver" NO envía el formulario bajo ninguna circunstancia.
- Un usuario puede registrarse exitosamente completando el formulario de `SignUpPage`; al finalizar se autentica y es redirigido a `/`.
- Si las contraseñas no coinciden, se muestra un mensaje de advertencia visual y no se contacta al backend.
- Si el backend rechaza el registro (ej. 409 Conflict: usuario duplicado), se muestra el mensaje de error devuelto por la API de forma visible y clara.
- Los campos de texto tienen identificadores únicos y las etiquetas se asocian correctamente.
- El toggle de contraseña oculta y muestra el texto con iconos coherentes y `aria-label` correcto.
- Durante el envío, los botones se deshabilitan evitando dobles peticiones.

---

## 8. Pruebas
1. **Validación de contraseñas desiguales:** Ingresar contraseñas distintas en `SignUpPage`; presionar "Registrarse" y validar que se visualice el error sin realizar peticiones de red.
2. **Envío de formulario de registro:** Completar un usuario nuevo y verificar que el formulario se envíe con éxito y redirija a la aplicación principal.
3. **Manejo de conflicto (409):** Intentar registrar un usuario ya existente; verificar que la pantalla exponga el mensaje retornado por el backend.
4. **Comportamiento de botón Volver:** Hacer clic en "Volver" con campos vacíos y confirmar que no se activan validaciones de envío ni peticiones HTTP.
5. **Alternancia entre páginas:** Hacer clic en "¡Crea Una!" y verificar que cargue `/signup`; en `/signup` hacer clic en "¡Accede a ella!" y confirmar el retorno a `/signin`.

---

## 9. Dependencias
- **Tareas previas:** FE-01, FE-02.
- **Tareas relacionadas:** FE-06 (gestión de sesión en perfil).

