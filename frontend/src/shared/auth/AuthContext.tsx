import { createContext, ReactNode, useContext, useMemo, useState } from 'react';
import { clearAuthToken, setAuthToken } from '../api/apiClient';
import type { AppUser } from '../types/user';

type AuthContextValue = {
  user: AppUser | null;
  login: (token: string, user: AppUser) => void;
  logout: () => void;
};

const storageKey = 'gaming-night-user';
const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AppUser | null>(() => {
    const stored = localStorage.getItem(storageKey);
    return stored ? (JSON.parse(stored) as AppUser) : null;
  });

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      login: (token, nextUser) => {
        setAuthToken(token);
        localStorage.setItem(storageKey, JSON.stringify(nextUser));
        setUser(nextUser);
      },
      logout: () => {
        clearAuthToken();
        localStorage.removeItem(storageKey);
        setUser(null);
      },
    }),
    [user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}
