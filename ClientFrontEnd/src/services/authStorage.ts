import type { AuthResponseType } from "../types/userTypes";

export const AUTH_STORAGE_KEY = "auth_session";

//Funcion para validar si un objeto es un "AuthResponseType" valido
function isAuthResponse(value: unknown): value is AuthResponseType {
  if (typeof value !== "object" || value === null) {
    return false;
  }

  const auth = value as Record<string, unknown>;

  return (
    typeof auth.userId === "string" &&
    typeof auth.userName === "string" &&
    typeof auth.token === "string" &&
    auth.token.length > 0 &&
    (auth.rootFolderId === undefined || typeof auth.rootFolderId === "string")
  );
}

export function getStoredAuth(): AuthResponseType | null {
  const storedAuth = localStorage.getItem(AUTH_STORAGE_KEY);

  if (!storedAuth) {
    return null;
  }

  try {
    const parsedAuth: unknown = JSON.parse(storedAuth);

    if (isAuthResponse(parsedAuth)) {
      return parsedAuth;
    }
  } catch {
    // El valor inválido se elimina para evitar reutilizar una sesión corrupta.
  }

  localStorage.removeItem(AUTH_STORAGE_KEY);
  return null;
}

export function saveAuth(auth: AuthResponseType): void {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth));
}

export function clearStoredAuth(): void {
  localStorage.removeItem(AUTH_STORAGE_KEY);
}
