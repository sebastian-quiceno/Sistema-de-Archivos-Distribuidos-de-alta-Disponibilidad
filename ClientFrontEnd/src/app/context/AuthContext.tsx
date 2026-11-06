import { useCallback, useMemo, useState, type ReactNode } from "react";
import { type AuthResponseType } from "../../types/userTypes";
import { clearStoredAuth, getStoredAuth, saveAuth } from "../../services/authStorage";
import { AuthContext } from "./AuthContextDefinition";

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [auth, setAuth] = useState<AuthResponseType | null>(getStoredAuth);

  const login = useCallback(({ userId, userName, token }: AuthResponseType) => {
    const nextAuth = { userId, userName, token };
    saveAuth(nextAuth);
    setAuth(nextAuth);
  }, []);

  const logout = useCallback(() => {
    clearStoredAuth();
    setAuth(null);
  }, []);

  const contextValue = useMemo(
    () => ({ auth, login, logout }),
    [auth, login, logout],
  );

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
}