import React, { useState, useRef } from 'react';

const MessageInput = ({ onSendMessage, onTyping, disabled }) => {
  const [message, setMessage] = useState('');
  const inputRef = useRef(null);

  const handleSubmit = (e) => {
    e.preventDefault();
    const content = message.trim();

    if (content) {
      onSendMessage(content);
      setMessage('');
      // focus lai vao input sau khi gui
      inputRef.current?.focus();
    }
  };

  const handleKeyPress = (e) => {
    // gui tin nhan khi nhan Enter (khong phai Shift+Enter)
    if (e.key === 'Enter' && !e.shiftKey) {
      handleSubmit(e);
    }
  };

  const handleChange = (e) => {
    setMessage(e.target.value);
    // thong bao dang go cho typing indicator
    if (onTyping) {
      onTyping();
    }
  };

  return (
    <form className="message-input-form" onSubmit={handleSubmit}>
      <button
        type="button"
        className="emoji-btn-placeholder"
        title="Emoji (sap co)"
        disabled={disabled}
      >
        &#128512;
      </button>
      <input
        ref={inputRef}
        type="text"
        className="message-input"
        value={message}
        onChange={handleChange}
        onKeyPress={handleKeyPress}
        placeholder="Nhap tin nhan..."
        autoComplete="off"
        autoFocus
        disabled={disabled}
      />
      <button
        type="submit"
        className="send-btn"
        disabled={disabled || !message.trim()}
      >
        Gui
      </button>
    </form>
  );
};

export default MessageInput;
