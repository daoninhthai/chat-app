import React, { useEffect, useRef } from 'react';

const MessageList = ({ messages, currentUser }) => {
  const messagesEndRef = useRef(null);

  // tu dong scroll xuong cuoi khi co tin nhan moi
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages]);

  const formatTime = (timestamp) => {
    if (!timestamp) {
      const now = new Date();
      return now.toLocaleTimeString('vi-VN', {
        hour: '2-digit',
        minute: '2-digit',
      });
    }
    return new Date(timestamp).toLocaleTimeString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const renderMessage = (message, index) => {
    // tin nhan JOIN/LEAVE hien thi khac
    if (message.type === 'JOIN') {
      return (
        <div key={index} className="message-event">
          <span className="event-icon">&#8594;</span>
          {message.sender} da tham gia phong chat
        </div>
      );
    }

    if (message.type === 'LEAVE') {
      return (
        <div key={index} className="message-event leave">
          <span className="event-icon">&#8592;</span>
          {message.sender} da roi phong chat
        </div>
      );
    }

    // tin nhan binh thuong
    const isSent = message.sender === currentUser;

    return (
      <div key={index} className={`message ${isSent ? 'sent' : 'received'}`}>
        {!isSent && <div className="sender-name">{message.sender}</div>}
        <div className="message-text">{message.content}</div>
        <div className="message-time">{formatTime(message.timestamp)}</div>
      </div>
    );
  };

  return (
    <div className="message-list">
      {messages.length === 0 && (
        <div className="no-messages">
          Chua co tin nhan nao. Hay bat dau cuoc tro chuyen!
        </div>
      )}
      {messages.map((msg, idx) => renderMessage(msg, idx))}
      <div ref={messagesEndRef} />
    </div>
  );
};

export default MessageList;
