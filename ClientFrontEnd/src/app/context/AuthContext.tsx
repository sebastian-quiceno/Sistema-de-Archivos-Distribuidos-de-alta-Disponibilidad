import { useCallback, useEffect, useMemo, useState, type ReactNode } from "react";
import { useNavigate } from "react-router-dom";
import { type AuthResponseType } from "../../types/userTypes";
import { clearStoredAuth, getStoredAuth, saveAuth } from "../../services/authStorage";
import { setUnauthorizedHandler } from "../../apis/apiClient";
import { AuthContext } from "./AuthContextDefinition";

interface AuthProviderProps {
  children: ReactNode;
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [auth, setAuth] = useState<AuthResponseType | null>(getStoredAuth);
  const navigate = useNavigate();

  const login = useCallback(
    ({ userId, userName, token, rootFolderId }: AuthResponseType) => {
      const nextAuth = { userId, userName, token, rootFolderId };
      saveAuth(nextAuth);
      setAuth(nextAuth);
      navigate("/", { replace: true });
    },
    [navigate],
  );

  const logout = useCallback(() => {
    clearStoredAuth();
    setAuth(null);
    navigate("/signin", { replace: true });
  }, [navigate]);

  useEffect(() => {
    setUnauthorizedHandler(() => {
      logout();
    });
    return () => {
      setUnauthorizedHandler(null);
    };
  }, [logout]);

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