import { createContext } from "react";
import type { AuthResponseType } from "../../types/userTypes";

export interface AuthContextType {
  auth: AuthResponseType | null;
  login: (data: AuthResponseType) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextType | null>(null);
