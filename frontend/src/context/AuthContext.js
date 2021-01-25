import React, { createContext, useState, useContext, useEffect } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);

  // kiem tra token khi app khoi dong
  useEffect(() => {
    const savedToken = localStorage.getItem('token');
    const savedUsername = localStorage.getItem('username');

    if (savedToken && savedUsername) {
      setToken(savedToken);
      setUser({ username: savedUsername });
    }
    setLoading(false);
  }, []);

  // dang nhap
  const login = async (username, password) => {
    try {
      const response = await api.post('/auth/login', { username, password });
      const { token: newToken } = response.data;

      localStorage.setItem('token', newToken);
      localStorage.setItem('username', username);

      setToken(newToken);
      setUser({ username });

      return { success: true };
    } catch (error) {
      const message =
        error.response?.data?.message || 'Dang nhap that bai!';
      return { success: false, message };
    }
  };

  // dang xuat
  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    setToken(null);
    setUser(null);
  };

  // kiem tra da dang nhap chua
  const isAuthenticated = () => {
    return !!token && !!user;
  };

  const value = {
    user,
    token,
    loading,
    login,
    logout,
    isAuthenticated,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

// custom hook de su dung AuthContext
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth phai duoc su dung ben trong AuthProvider');
  }
  return context;
};

export default AuthContext;
