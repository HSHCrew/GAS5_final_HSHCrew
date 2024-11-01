import React, { useState } from 'react';
import './style.css';

import example from '../../assets/altari.svg';  // 사용자 아이콘 경로

function Chatting() {
  const [messages, setMessages] = useState([
    { id: 1, text: '안녕하세요!', sent: true },
    { id: 2, text: '안녕하세요! 어떻게 도와드릴까요?', sent: false },
  ]);
  const [input, setInput] = useState('');

  const handleSend = () => {
    if (input.trim()) {
      setMessages([...messages, { id: Date.now(), text: input, sent: true }]);
      setInput('');
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      if (e.ctrlKey) {
        setInput((prevInput) => prevInput + '\n');
      } else {
        e.preventDefault();
        handleSend();
      }
    }
  };

  return (
    <div className="chatting-page-wrapper">
      <div className="chatting-container">
        <div className="message-section">
          {messages.map((message) => (
            <div
              key={message.id}
              className={`message-container ${message.sent ? 'sent' : 'received'}`}
            >
              {!message.sent && (
                <img src={example} className="message-icon" alt="User Avatar" />
              )}
              <div
                className={`chatting-message ${
                  message.sent ? 'chatting-message-sent' : 'chatting-message-received'
                }`}
              >
                {message.text}
              </div>
            </div>
          ))}
        </div>

        <div className="input-section">
          <textarea
            className="input-box"
            placeholder="메시지를 입력하세요..."
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyPress}
            rows="1"
          />
          <button className="send-button" onClick={handleSend}>
            전송
          </button>
        </div>
      </div>
    </div>
  );
}

export default Chatting;
