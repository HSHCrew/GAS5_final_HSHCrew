import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import ReactMarkdown from 'react-markdown';
import './style.css';

import example from '../../assets/altari.svg';  // 사용자 아이콘 경로

function Chatting() {
  const [messages, setMessages] = useState([
    { id: 1, text: '안녕하세요! 어떻게 도와드릴까요?', sent: false },
  ]);
  const [input, setInput] = useState('');

  // 마지막 메시지를 참조하기 위한 ref
  const lastMessageRef = useRef(null);

  // 서버에 메시지 전송 및 응답 받기
  const sendMessageToServer = async (message) => {
    try {
      const response = await axios.post('http://localhost:8000/user/medications/chat_message', {
        user_id: 1,
        message: message,
      });
      // 서버의 응답을 messages 배열에 추가
      setMessages((prevMessages) => [
        ...prevMessages,
        { id: Date.now(), text: response.data.response, sent: false },
      ]);
    } catch (error) {
      console.error("메시지를 서버에 전송하는 중 오류가 발생했습니다.", error);
    }
  };

  const handleSend = () => {
    if (input.trim()) {
      // 사용자가 보낸 메시지 추가
      const newMessage = { id: Date.now(), text: input, sent: true };
      setMessages((prevMessages) => [...prevMessages, newMessage]);
      
      // 서버로 메시지 전송
      sendMessageToServer(input);
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

  // 메시지가 추가될 때마다 스크롤을 아래로 이동
  useEffect(() => {
    if (lastMessageRef.current) {
      lastMessageRef.current.scrollIntoView({ behavior: 'smooth', block: 'end' });
    }
  }, [messages]);

  return (
    <div className="chatting-page-wrapper">
      <div className="chatting-container">
        <div className="message-section">
          {messages.map((message, index) => (
            <div
              key={message.id}
              ref={index === messages.length - 1 ? lastMessageRef : null} // 마지막 메시지에 ref 추가
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
                <ReactMarkdown
                  components={{
                    p: ({ node, ...props }) => (
                      <p style={{ color: message.sent ? 'white' : 'black' }} {...props} />
                    ),
                    strong: ({ node, ...props }) => (
                      <strong style={{ color: message.sent ? 'white' : 'black' }} {...props} />
                    ),
                    // 필요한 다른 요소들도 추가로 정의 가능
                  }}
                >
                  {message.text}
                </ReactMarkdown>
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
