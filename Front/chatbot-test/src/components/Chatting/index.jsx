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
  const [isStreaming, setIsStreaming] = useState(false);
  const lastMessageRef = useRef(null);
  const userId = 9999; // 테스트용 사용자 ID

  // 서버에 메시지 전송 및 응답 받기
  const sendMessageToServer = async (message) => {
    try {
      console.log("[DEBUG] Starting to send message:", message);
      setIsStreaming(true);
      
      // 사용자 메시지 추가
      const userMessage = { id: Date.now(), text: message, sent: true };
      setMessages(prev => [...prev, userMessage]);
      
      // 챗봇 응답을 위한 빈 메시지 추가
      const botMessageId = Date.now() + 1;
      setMessages(prev => [...prev, { id: botMessageId, text: '', sent: false }]);

      const response = await fetch(`http://localhost:8000/user/${userId}/medications/chat/message/stream`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          message: message,
          user_profile_id: userId
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let accumulatedText = '';  // 누적할 텍스트

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;
        
        const chunk = decoder.decode(value);
        console.log("[DEBUG] Received chunk:", chunk);
        
        const lines = chunk.split('\n');
        
        for (const line of lines) {
          if (line.trim() && line.startsWith('data: ')) {
            try {
              const jsonStr = line.slice(6);
              console.log("[DEBUG] Parsing JSON:", jsonStr);
              
              const jsonData = JSON.parse(jsonStr);
              console.log("[DEBUG] Parsed data:", jsonData);
              
              if (jsonData.token) {
                // 토큰을 누적
                accumulatedText += jsonData.token;
                console.log("[DEBUG] Accumulated text:", accumulatedText);
                
                // 누적된 텍스트로 메시지 업데이트
                setMessages(prev => {
                  const newMessages = prev.map(msg =>
                    msg.id === botMessageId
                      ? { ...msg, text: accumulatedText }
                      : msg
                  );
                  console.log("[DEBUG] Updated messages:", newMessages);
                  return newMessages;
                });
              }
            } catch (e) {
              console.error('[ERROR] Error parsing SSE data:', e, 'Line:', line);
            }
          }
        }
      }

      console.log("[DEBUG] Starting follow-up polling");
      pollForFollowUp();
      
    } catch (error) {
      console.error("[ERROR] Error in sendMessageToServer:", error);
      setMessages(prev => [...prev, {
        id: Date.now(),
        text: `Error: ${error.message}`,
        sent: false
      }]);
    } finally {
      setIsStreaming(false);
    }
  };

  const pollForFollowUp = async (maxAttempts = 30) => {
    let attempts = 0;
    const pollInterval = setInterval(async () => {
      try {
        const response = await axios.get(`http://localhost:8000/user/${userId}/medications/chat/follow-up`);
        
        if (response.data.follow_up) {
          setMessages(prev => [...prev, {
            id: Date.now(),
            text: response.data.follow_up,
            sent: false
          }]);
          clearInterval(pollInterval);
        } else if (response.data.status === "completed" || attempts >= maxAttempts) {
          clearInterval(pollInterval);
        }
        
        attempts++;
      } catch (error) {
        console.error("Follow-up polling error:", error);
        clearInterval(pollInterval);
      }
    }, 1000);

    // 컴포넌트 언마운트 시 인터벌 정리
    return () => clearInterval(pollInterval);
  };

  const handleSend = () => {
    if (input.trim() && !isStreaming) {
      const message = input.trim();
      setInput('');
      sendMessageToServer(message);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      if (e.ctrlKey) {
        setInput(prev => prev + '\n');
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
              ref={index === messages.length - 1 ? lastMessageRef : null}
              className={`message-container ${message.sent ? 'sent' : 'received'}`}
            >
              {!message.sent && (
                <img src={example} className="message-icon" alt="Bot Avatar" />
              )}
              <div className={`chatting-message ${
                message.sent ? 'chatting-message-sent' : 'chatting-message-received'
              }`}>
                <ReactMarkdown
                  components={{
                    p: ({ node, ...props }) => (
                      <p style={{ color: message.sent ? 'white' : 'black' }} {...props} />
                    ),
                    strong: ({ node, ...props }) => (
                      <strong style={{ color: message.sent ? 'white' : 'black' }} {...props} />
                    ),
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
            disabled={isStreaming}
          />
          <button 
            className="send-button" 
            onClick={handleSend}
            disabled={isStreaming}
          >
            전송
          </button>
        </div>
      </div>
    </div>
  );
}

export default Chatting;