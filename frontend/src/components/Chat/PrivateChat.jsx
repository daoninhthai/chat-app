import React, { useState, useEffect, useRef, useCallback } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import api from '../../services/api';
import './PrivateChat.css';

const PrivateChat = () => {
  const [users, setUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [messages, setMessages] = useState([]);
  const [messageInput, setMessageInput] = useState('');
  const [unreadCounts, setUnreadCounts] = useState({});
  const stompClientRef = useRef(null);
  const messagesEndRef = useRef(null);

  const currentUser = localStorage.getItem('username');

  // tu dong scroll xuong cuoi
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // load danh sach user
  useEffect(() => {
    const loadUsers = async () => {
      try {
        const response = await api.get('/users');
        // loc bo chinh minh khoi danh sach
        const otherUsers = response.data.filter(
          (u) => u.username !== currentUser
        );
        setUsers(otherUsers);
      } catch (error) {
        console.error('Loi khi load danh sach user:', error);
      }
    };
    loadUsers();
  }, [currentUser]);

  // ket noi WebSocket de nhan tin nhan rieng
  useEffect(() => {
    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = Stomp.over(socket);
    stompClient.debug = null;

    stompClient.connect({}, () => {
      stompClientRef.current = stompClient;

      // subscribe de nhan tin nhan rieng
      stompClient.subscribe(`/user/${currentUser}/queue/private`, (payload) => {
        const message = JSON.parse(payload.body);
        handleIncomingMessage(message);
      });
    });

    return () => {
      if (stompClientRef.current && stompClientRef.current.connected) {
        stompClientRef.current.disconnect();
      }
    };
  }, [currentUser]);

  // xu ly tin nhan den
  const handleIncomingMessage = useCallback(
    (message) => {
      if (
        selectedUser &&
        (message.senderUsername === selectedUser.username ||
          message.senderUsername === currentUser)
      ) {
        // dang xem cuoc tro chuyen nay -> hien tin nhan
        setMessages((prev) => [...prev, message]);
      } else {
        // chua xem -> tang unread count
        setUnreadCounts((prev) => ({
          ...prev,
          [message.senderUsername]: (prev[message.senderUsername] || 0) + 1,
        }));
      }
    },
    [selectedUser, currentUser]
  );

  // chon user de chat
  const selectUser = async (user) => {
    setSelectedUser(user);
    setUnreadCounts((prev) => {
      const updated = { ...prev };
      delete updated[user.username];
      return updated;
    });

    // load lich su tin nhan
    try {
      const response = await api.get(`/messages/${user.id}`);
      setMessages(response.data);
    } catch (error) {
      console.error('Loi khi load tin nhan:', error);
      setMessages([]);
    }
  };

  // gui tin nhan
  const sendMessage = (e) => {
    e.preventDefault();
    const content = messageInput.trim();

    if (!content || !selectedUser || !stompClientRef.current) return;

    const messageData = {
      sender: currentUser,
      receiverId: selectedUser.id,
      content: content,
    };

    stompClientRef.current.send(
      '/app/chat.private',
      {},
      JSON.stringify(messageData)
    );

    setMessageInput('');
  };

  return (
    <div className="private-chat-container">
      {/* Danh sach user */}
      <div className="user-list-panel">
        <div className="user-list-header">
          <h3>Tin nhan rieng</h3>
        </div>
        <div className="user-list">
          {users.map((user) => (
            <div
              key={user.id}
              className={`user-item ${
                selectedUser && selectedUser.id === user.id ? 'active' : ''
              }`}
              onClick={() => selectUser(user)}
            >
              <div className="user-avatar">
                {user.username.charAt(0).toUpperCase()}
              </div>
              <div className="user-info">
                <div className="user-name">{user.username}</div>
                {user.displayName && (
                  <div className="user-display-name">{user.displayName}</div>
                )}
              </div>
              {unreadCounts[user.username] > 0 && (
                <span className="unread-badge">
                  {unreadCounts[user.username]}
                </span>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Khu vuc chat */}
      <div className="conversation-panel">
        {selectedUser ? (
          <>
            <div className="conversation-header">
              <div className="conversation-user-avatar">
                {selectedUser.username.charAt(0).toUpperCase()}
              </div>
              <h3>{selectedUser.username}</h3>
            </div>

            <div className="conversation-messages">
              {messages.length === 0 && (
                <div className="no-messages">
                  Bat dau cuoc tro chuyen voi {selectedUser.username}
                </div>
              )}
              {messages.map((msg, idx) => (
                <div
                  key={idx}
                  className={`private-message ${
                    msg.senderUsername === currentUser ? 'sent' : 'received'
                  }`}
                >
                  <div className="private-message-text">{msg.content}</div>
                  <div className="private-message-time">
                    {msg.timestamp
                      ? new Date(msg.timestamp).toLocaleTimeString('vi-VN', {
                          hour: '2-digit',
                          minute: '2-digit',
                        })
                      : ''}
                    {msg.read && msg.senderUsername === currentUser && (
                      <span className="read-indicator"> Da xem</span>
                    )}
                  </div>
                </div>
              ))}
              <div ref={messagesEndRef} />
            </div>

            <form className="conversation-input" onSubmit={sendMessage}>
              <input
                type="text"
                value={messageInput}
                onChange={(e) => setMessageInput(e.target.value)}
                placeholder={`Gui tin nhan cho ${selectedUser.username}...`}
                autoComplete="off"
                autoFocus
              />
              <button type="submit" disabled={!messageInput.trim()}>
                Gui
              </button>
            </form>
          </>
        ) : (
          <div className="no-conversation">
            <div className="no-conversation-icon">💬</div>
            <h3>Chon mot nguoi de bat dau chat</h3>
            <p>Tin nhan rieng chi co hai nguoi co the xem</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default PrivateChat;
