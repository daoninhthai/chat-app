import React from 'react';
import './OnlineUsers.css';

const OnlineUsers = ({ users }) => {
  return (
    <div className="online-users-container">
      <div className="online-users-header">
        <h4>Dang Online - {users.length}</h4>
      </div>
      <div className="online-users-list">
        {users.length === 0 && (
          <div className="no-users">Khong co ai online</div>
        )}
        {users.map((user, index) => (
          <div key={index} className="online-user-item">
            <span className="online-dot"></span>
            <span className="online-username">{user}</span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default OnlineUsers;
