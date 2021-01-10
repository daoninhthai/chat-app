import React, { useState } from 'react';
import { Link, useHistory } from 'react-router-dom';
import api from '../../services/api';
import './Auth.css';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const history = useHistory();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await api.post('/auth/login', {
        username,
        password,
      });

      // luu token va username
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('username', username);

      // chuyen den trang chat
      history.push('/chat');
    } catch (err) {
      if (err.response && err.response.data) {
        setError(err.response.data.message || 'Dang nhap that bai!');
      } else {
        setError('Khong the ket noi den server.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h1>Chat App</h1>
        <p>Dang nhap de bat dau chat</p>

        {error && <div className="auth-error">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <input
              type="text"
              placeholder="Ten dang nhap"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              autoFocus
            />
          </div>
          <div className="form-group">
            <input
              type="password"
              placeholder="Mat khau"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <button type="submit" className="btn-auth" disabled={loading}>
            {loading ? 'Dang xu ly...' : 'Dang Nhap'}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            Chua co tai khoan?{' '}
            <Link to="/register">Dang ky ngay</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
