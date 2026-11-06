# AuthContext en el frontend

## Propósito

`AuthContext` centraliza la sesión autenticada para que cualquier componente
pueda consultar al usuario actual, obtener el JWT o cerrar la sesión sin pasar
estos datos manualmente por cada nivel de componentes.

La información compartida tiene esta forma:

```ts
{
  userId: string;
  userName: string;
  token: string;
}
```

La sesión se persiste en `localStorage` con la clave `auth_session`. El módulo
`src/services/authStorage.ts` es la única capa que valida, guarda y elimina
estos datos.

## Configuración del proveedor

El proveedor se monta una sola vez en `src/main.tsx`:

```tsx
<AuthProvider>
  <App />
</AuthProvider>
```

Por eso todos los componentes descendientes de `App` pueden consumir el
contexto. El estado se inicializa leyendo la sesión persistida, de modo que
una recarga conserva la autenticación si el JWT todavía es válido.

## `useContext` y `useAuth`

React expone el valor de un contexto mediante `useContext(AuthContext)`.
El proyecto encapsula esa llamada en `src/hooks/users/useAuth.tsx`:

```tsx
import { useAuth } from "../hooks/users/useAuth";

function UserMenu() {
  const { auth, logout } = useAuth();

  if (!auth) {
    return <p>Sesión no iniciada</p>;
  }

  return (
    <div>
      <span>Hola, {auth.userName}</span>
      <button onClick={logout}>Cerrar sesión</button>
    </div>
  );
}
```

El hook lanza un error claro si se usa fuera de `AuthProvider`. Esto evita
errores silenciosos por olvidar el proveedor.

## Inicio y cierre de sesión

1. `userService.signIn` o `userService.signUp` envía las credenciales al
   backend y devuelve un `AuthResponseType`.
2. El componente que recibe esa respuesta llama a `login(response)` desde
   `useAuth`.
3. `login` actualiza el estado React y persiste el usuario y el JWT en
   `auth_session`.
4. `apiClient` lee esa sesión antes de cada solicitud y agrega
   `Authorization: Bearer <token>`.
5. Si el backend responde `401`, el interceptor elimina la sesión y dirige al
   usuario a `/signin`.
6. `logout` elimina la sesión persistida y establece `auth` en `null`.

Ejemplo de uso después de un inicio de sesión:

```tsx
const { login } = useAuth();

const response = await signIn({ username, password });
login(response);
```

`auth` puede ser `null`, por lo que los componentes deben contemplar el estado
no autenticado. El JWT se guarda en `localStorage`; para aplicaciones con
requisitos de seguridad más estrictos conviene evaluar cookies `HttpOnly` en
coordinación con el backend.
