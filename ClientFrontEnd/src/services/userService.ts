import {apiClient, getApiErrorMessage} from "../apis/apiClient";
import { type SignInRequestType, type SignUpRequestType, type AuthResponseType } from "../types/userTypes";
import { clearStoredAuth, getStoredAuth } from "./authStorage";


export async function signIn(
  request: SignInRequestType
): Promise<AuthResponseType> {

  try {
    const response = await apiClient.post<AuthResponseType>(
      "/auth/login",
      request
    );

    return response.data;

  } catch (error: unknown) {
    throw new Error(getApiErrorMessage(error, "Error al acceder"), { cause: error });
  }
}

export async function signUp(request: SignUpRequestType): Promise<AuthResponseType> {
  try {
    const response = await apiClient.post<AuthResponseType>("/auth/register", request);

    return response.data;


  } catch (error: unknown) {
    throw new Error(getApiErrorMessage(error, "Error al registrarse"), { cause: error });
  }
}

export function logoutUser() {
  clearStoredAuth();
}

export function getToken(): string | null {
  return getStoredAuth()?.token ?? null;
}

export function isAuthenticated(): boolean {
  return !!getToken();
}