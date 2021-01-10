import React, { useState } from 'react';
import { Link, useHistory } from 'react-router-dom';
import api from '../../services/api';
import './Auth.css';

const Register = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const history = useHistory();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    // kiem tra mat khau khop
    if (password !== confirmPassword) {
      setError('Mat khau khong khop!');
      return;
    }

    if (password.length < 6) {
      setError('Mat khau phai co it nhat 6 ky tu!');
      return;
    }

    setLoading(true);

    try {
      await api.post('/auth/register', {
        username,
        email,
        password,
      });

      // dang ky thanh cong, chuyen sang trang login
      history.push('/login');
    } catch (err) {
      if (err.response && err.response.data) {
        setError(err.response.data.message || 'Dang ky that bai!');
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
        <h1>Tao Tai Khoan</h1>
        <p>Dang ky de bat dau su dung Chat App</p>

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
              type="email"
              placeholder="Email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
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
          <div className="form-group">
            <input
              type="password"
              placeholder="Xac nhan mat khau"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </div>
          <button type="submit" className="btn-auth" disabled={loading}>
            {loading ? 'Dang xu ly...' : 'Dang Ky'}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            Da co tai khoan?{' '}
            <Link to="/login">Dang nhap</Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;
