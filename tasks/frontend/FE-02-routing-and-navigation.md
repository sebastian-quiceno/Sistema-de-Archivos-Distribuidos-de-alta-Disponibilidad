# Tarea FE-02: Implementación de enrutamiento y control de acceso (Routing & Protected Routes)

## 1. Identificación
- **ID:** FE-02
- **Título:** Implementación del sistema de rutas, navegación declarativa y protección de sesión (SPA Routing)
- **Dominio:** Frontend
- **Tipo:** Funcionalidad faltante / Integración
- **Prioridad:** Crítica

---

## 2. Problema
### Estado actual
El componente raíz `App.tsx` renderiza de forma estática y exclusiva `<FileExplorerPage />`. No existe ningún mecanismo de enrutamiento (como `react-router-dom` o un gestor de rutas basado en el historial del navegador):
```tsx
function App() {
  return (
    <>
      <FileExplorerPage />
    </>
  );
}
```
Las páginas `SignInPage` y `SignUpPage` existen en el árbol de directorios (`src/app/pages/`) pero están completamente desconectadas de la navegación de la aplicación.
Adicionalmente, en `apiClient.ts:29`, ante una respuesta `401 Unauthorized` el interceptor ejecuta:
```ts
window.location.href = "/signin";
```
Dado que el servidor de desarrollo de Vite sirve `index.html` y `App.tsx` no discrimina la URL, navegar a `/signin` continúa mostrando `FileExplorerPage` en un estado degradado sin usuario autenticado (`auth == null`).

### Evidencia
- `package.json` no cuenta con `react-router-dom` ni librerías de enrutado.
- Navegar a `/signin`, `/signup` o cualquier otra ruta siempre carga el explorador de archivos.
- Un usuario no autenticado puede ver la estructura del explorador (aunque vacía) en lugar de ser dirigido al login.

### Impacto
La aplicación no puede ser utilizada por usuarios nuevos ni existentes a través de un flujo convencional. El usuario no puede iniciar sesión ni registrarse a través de la interfaz web estándar.

---

## 3. Ubicación
- `ClientFrontEnd/package.json`
- `ClientFrontEnd/src/app/App.tsx`
- `ClientFrontEnd/src/main.tsx`
- `ClientFrontEnd/src/apis/apiClient.ts`
- `ClientFrontEnd/src/app/pages/SignInPage.tsx`
- `ClientFrontEnd/src/app/pages/SignUpPage.tsx`
- `ClientFrontEnd/src/app/pages/FileExplorerPage.tsx`
- `ClientFrontEnd/src/app/context/AuthContext.tsx`

---

## 4. Objetivo
Establecer un esquema de enrutamiento cliente (SPA Routing) robusto que defina rutas públicas (`/signin`, `/signup`) y rutas protegidas (`/` o `/explorer`), asegurando que los usuarios no autenticados sean redirigidos automáticamente al inicio de sesión y que los usuarios autenticados sean dirigidos al explorador de archivos.

---

## 5. Requisitos
- Incorporar una solución de enrutamiento estándar (preferentemente `react-router-dom` v6/v7 o un router de navegación cliente desacoplado).
- Definir la tabla de rutas:
  - `/signin`: Renderiza `SignInPage` (pública, si el usuario ya está autenticado redirige a `/`).
  - `/signup`: Renderiza `SignUpPage` (pública, si el usuario ya está autenticado redirige a `/`).
  - `/`: Renderiza `FileExplorerPage` dentro de un guardia de ruta protegida (`ProtectedRoute`), requiriendo que `auth` no sea nulo.
  - `*` (rutas no reconocidas): Redirección inteligente hacia `/` o página 404/Fallback.
- Conectar la redirección del interceptor 401 de Axios en `apiClient.ts` con la navegación del enrutador para evitar recargas duras innecesarias cuando sea posible.
- Proveer transiciones fluidas de navegación entre las vistas sin perder el estado del contexto.

---

## 6. Alcance
- **Cambios necesarios:**
  - Instalación y configuración de la dependencia de enrutamiento en `ClientFrontEnd`.
  - Creación del componente `ProtectedRoute` o guardián de autenticación.
  - Reestructuración de `App.tsx` para declarar el árbol de rutas.
  - Actualización de redirecciones en `SignInPage`, `SignUpPage` y el logout del contexto.
- **Restricciones:**
  - Mantener la persistencia en `localStorage` mediante `authStorage.ts` y sincronizarla con el estado de las rutas.

---

## 7. Criterios de aceptación
- Ingresar a la URL raíz `/` sin sesión activa en `localStorage` redirige inmediatamente a `/signin`.
- Ingresar a `/signin` o `/signup` con una sesión válida redirige al usuario a `/`.
- Iniciar sesión exitosamente desde `SignInPage` navega fluidamente a `/` mostrando los archivos del usuario.
- Cerrar sesión desde el explorador elimina las credenciales y redirige a `/signin`.
- Cuando un endpoint protegido retorne un error 401, el usuario es redirigido a `/signin` y se limpia la sesión expirada.

---

## 8. Pruebas
1. **Acceso no autenticado:** Con `localStorage` limpio, abrir `http://localhost:5173/` y confirmar la redirección a `/signin`.
2. **Acceso autenticado a login:** Almacenar una sesión válida de prueba en `localStorage` y cargar `/signin`; verificar que redirige a `/`.
3. **Flujo de login:** Iniciar sesión con credenciales válidas y comprobar que la URL cambia a `/` y se despliega `FileExplorerPage`.
4. **Respuesta 401:** Simular una llamada con token vencido/inválido y verificar redirección a `/signin`.

---

## 9. Dependencias
- **Tareas previas:** FE-01.
- **Tareas relacionadas:** FE-03 (flujos de autenticación), FE-06 (cierre de sesión en perfil).

