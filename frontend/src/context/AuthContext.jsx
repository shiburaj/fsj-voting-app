import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('voting_user');
    if (saved) {
      try {
        return JSON.parse(saved);
      } catch (e) {
        return null;
      }
    }
    return null;
  });

  const [loading, setLoading] = useState(false);

  // Validate server-side HttpSession on initial load
  useEffect(() => {
    async function verifySession() {
      try {
        const res = await api.auth.me();
        if (res && res.data) {
          setUser(res.data);
          localStorage.setItem('voting_user', JSON.stringify(res.data));
        }
      } catch (err) {
        // Session expired or not logged in
        setUser(null);
        localStorage.removeItem('voting_user');
      }
    }
    verifySession();
  }, []);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const res = await api.auth.login(email, password);
      const userData = res.data;
      setUser(userData);
      localStorage.setItem('voting_user', JSON.stringify(userData));
      return userData;
    } finally {
      setLoading(false);
    }
  };

  const register = async (name, email, password) => {
    setLoading(true);
    try {
      const res = await api.auth.register(name, email, password);
      const userData = res.data;
      setUser(userData);
      localStorage.setItem('voting_user', JSON.stringify(userData));
      return userData;
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    try {
      await api.auth.logout();
    } catch (e) {
      console.error('Logout error', e);
    } finally {
      setUser(null);
      localStorage.removeItem('voting_user');
    }
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
