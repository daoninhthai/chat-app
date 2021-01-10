import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams } from 'react-router-dom';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import api from '../../services/api';
import './ChatRoom.css';

const ChatRoom = () => {
  const { roomId } = useParams();
  const [messages, setMessages] = useState([]);
  const [messageInput, setMessageInput] = useState('');
  const [connected, setConnected] = useState(false);
  const [typingUsers, setTypingUsers] = useState([]);
  const stompClientRef = useRef(null);
  const messagesEndRef = useRef(null);
  const typingTimerRef = useRef(null);
  const isTypingRef = useRef(false);

  const currentUser = localStorage.getItem('username') || 'Anonymous';
  const currentRoomId = roomId || '1';

  // tu dong scroll xuong cuoi
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // load tin nhan cu tu server
  const loadMessages = useCallback(async () => {
    try {
      const response = await api.get(`/chat/rooms/${currentRoomId}/messages`);
      setMessages(response.data);
    } catch (error) {
      console.log('Chua co tin nhan cu hoac loi khi load:', error.message);
    }
  }, [currentRoomId]);

  // ket noi WebSocket
  useEffect(() => {
    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = Stomp.over(socket);
    stompClient.debug = null; // tat debug log

    stompClient.connect({}, () => {
      setConnected(true);
      stompClientRef.current = stompClient;

      // subscribe tin nhan trong room
      stompClient.subscribe(`/topic/room.${currentRoomId}`, (payload) => {
        const message = JSON.parse(payload.body);
        setMessages((prev) => [...prev, message]);
      });

      // subscribe typing indicator
      stompClient.subscribe(`/topic/room.${currentRoomId}.typing`, (payload) => {
        const typingEvent = JSON.parse(payload.body);
        if (typingEvent.username !== currentUser) {
          if (typingEvent.isTyping || typingEvent.typing) {
            setTypingUsers((prev) => {
              if (!prev.includes(typingEvent.username)) {
                return [...prev, typingEvent.username];
              }
              return prev;
            });
          } else {
            setTypingUsers((prev) =>
              prev.filter((u) => u !== typingEvent.username)
            );
          }
        }
      });

      // gui thong bao join
      stompClient.send(
        `/app/chat.addUser/${currentRoomId}`,
        {},
        JSON.stringify({
          sender: currentUser,
          type: 'JOIN',
        })
      );
    }, (error) => {
      console.error('WebSocket connection error:', error);
      setConnected(false);
    });

    loadMessages();

    // cleanup khi unmount
    return () => {
      if (stompClientRef.current && stompClientRef.current.connected) {
        stompClientRef.current.disconnect();
      }
    };
  }, [currentRoomId, currentUser, loadMessages]);

  // xu ly gui tin nhan
  const sendMessage = (e) => {
    e.preventDefault();
    const content = messageInput.trim();

    if (content && stompClientRef.current && connected) {
      const chatMessage = {
        sender: currentUser,
        content: content,
        type: 'CHAT',
        roomId: currentRoomId,
      };

      stompClientRef.current.send(
        `/app/chat.sendMessage/${currentRoomId}`,
        {},
        JSON.stringify(chatMessage)
      );

      setMessageInput('');

      // reset typing status
      if (isTypingRef.current) {
        isTypingRef.current = false;
        clearTimeout(typingTimerRef.current);
        sendTypingStatus(false);
      }
    }
  };

  // xu ly typing indicator
  const handleInputChange = (e) => {
    setMessageInput(e.target.value);

    if (!stompClientRef.current || !connected) return;

    if (!isTypingRef.current) {
      isTypingRef.current = true;
      sendTypingStatus(true);
    }

    clearTimeout(typingTimerRef.current);
    typingTimerRef.current = setTimeout(() => {
      isTypingRef.current = false;
      sendTypingStatus(false);
    }, 500);
  };

  const sendTypingStatus = (typing) => {
    if (stompClientRef.current && connected) {
      stompClientRef.current.send(
        `/app/chat.typing/${currentRoomId}`,
        {},
        JSON.stringify({
          username: currentUser,
          roomId: currentRoomId,
          isTyping: typing,
        })
      );
    }
  };

  // render 1 tin nhan
  const renderMessage = (message, index) => {
    if (message.type === 'JOIN' || message.type === 'LEAVE') {
      return (
        <div key={index} className="message-event">
          {message.sender} {message.type === 'JOIN' ? 'da tham gia' : 'da roi'} phong chat
        </div>
      );
    }

    const isSent = message.sender === currentUser;
    return (
      <div key={index} className={`message ${isSent ? 'sent' : 'received'}`}>
        {!isSent && <div className="sender-name">{message.sender}</div>}
        <div className="message-text">{message.content}</div>
        <div className="message-time">
          {message.timestamp
            ? new Date(message.timestamp).toLocaleTimeString('vi-VN', {
                hour: '2-digit',
                minute: '2-digit',
              })
            : new Date().toLocaleTimeString('vi-VN', {
                hour: '2-digit',
                minute: '2-digit',
              })}
        </div>
      </div>
    );
  };

  return (
    <div className="chatroom-container">
      <div className="chatroom-header">
        <h2>Phong Chat #{currentRoomId}</h2>
        <span className={`connection-status ${connected ? 'online' : 'offline'}`}>
          {connected ? 'Da ket noi' : 'Mat ket noi'}
        </span>
      </div>

      <div className="chatroom-messages">
        {messages.map((msg, idx) => renderMessage(msg, idx))}
        <div ref={messagesEndRef} />
      </div>

      {typingUsers.length > 0 && (
        <div className="typing-indicator">
          {typingUsers.join(', ')} dang go...
        </div>
      )}

      <form className="chatroom-input" onSubmit={sendMessage}>
        <input
          type="text"
          value={messageInput}
          onChange={handleInputChange}
          placeholder="Nhap tin nhan..."
          autoComplete="off"
          autoFocus
        />
        <button type="submit" disabled={!connected}>
          Gui
        </button>
      </form>
    </div>
  );
};

export default ChatRoom;
