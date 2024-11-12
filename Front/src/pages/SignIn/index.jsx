import React, { useState, useEffect } from 'react';
import './SignIn.css'; 
import { useNavigate } from 'react-router-dom';
import apiRequest from '../../utils/apiRequest';

import altariLogo from '../../assets/altari-logo.svg';
import lockerIcon from '../../assets/locker.svg';

function SignIn() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false); // "자동 로그인" 상태
  const navigate = useNavigate();

  useEffect(() => {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    if (token) {
      validateToken(token);
    }
  }, [navigate]);

  const validateToken = async (token) => {
    try {
      const response = await apiRequest('http://localhost:8080/altari/refresh', { headers: { 'Authorization': `Bearer ${token}` } });
      if (response.status === 200) {
        navigate('/home');
      } else {
        console.error('토큰이 유효하지 않습니다.');
      }
    } catch (error) {
      console.error('토큰 검증 오류:', error);
      localStorage.removeItem('token');
      sessionStorage.removeItem('token');
    }
  };

  const handleLogin = async () => {
    setErrorMessage('');
    setIsLoading(true);

    try {
        // Axios 인스턴스를 사용하여 로그인 API 호출
        const response = await apiRequest('http://localhost:8080/altari/login', {
            method: 'POST',
            data: { username, password }
        });

        // 서버로부터 토큰 응답을 받은 후 저장
        const { accessToken, refreshToken } = response.data;

        if (accessToken && refreshToken) {
            if (rememberMe) {
                localStorage.setItem('token', accessToken);
                localStorage.setItem('refreshToken', refreshToken);
                localStorage.setItem('username', username);
            } else {
                sessionStorage.setItem('token', accessToken);
                sessionStorage.setItem('refreshToken', refreshToken);
                sessionStorage.setItem('username', username);
            }
            alert('로그인 성공!');
            navigate('/home');
        } else {
            setErrorMessage('로그인 실패: 올바른 아이디와 비밀번호를 입력해주세요.');
        }
    } catch (error) {
        if (error.response && error.response.data && error.response.data.msg) {
            setErrorMessage(error.response.data.msg); // 서버에서 온 오류 메시지 사용
        } else {
            setErrorMessage('로그인 실패: 서버 오류가 발생했습니다.');
        }
    } finally {
        setIsLoading(false);
    }
};
  
  const handleSignUp = () => {
    navigate('/signUp');
  };

  const togglePasswordVisibility = () => {
    setShowPassword((prev) => !prev);
  };

  return (
    <div className="signin-container">
      <div className="signin-box">
        <img src={altariLogo} alt="Logo" className="signin-logo" />

        <form className="signin-form" onSubmit={(e) => { e.preventDefault(); handleLogin(); }}>
          <div className="signin-header">
            <img src={lockerIcon} alt="Login icon" className="signin-icon" />
            <p className="signin-title">로그인</p>
          </div>

          <div className="signin-input-container">
            <label htmlFor="username" className="hidden-label">아이디</label>
            <input
              type="text"
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="아이디를 입력해주세요."
              className="signin-input"
            />
          </div>

          <div className="signin-input-container">
            <label htmlFor="password" className="hidden-label">비밀번호</label>
            <input
              type={showPassword ? 'text' : 'password'}
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호를 입력해주세요."
              className="signin-input"
            />
            <button type="button" onClick={togglePasswordVisibility} className="toggle-password">
              {showPassword ? '숨기기' : '보기'}
            </button>
          </div>

          {/* "자동 로그인" 체크박스 추가: 로그인 폼 내부에 위치 */}
          <div className="remember-me-container">
            <input
              type="checkbox"
              id="rememberMe"
              checked={rememberMe}
              onChange={(e) => setRememberMe(e.target.checked)}
              className="remember-me-checkbox"
            />
            <label htmlFor="rememberMe" className="remember-me-label">자동 로그인</label>
          </div>

          {/* 에러 메시지 표시 */}
          {errorMessage && <p className="error-message">{errorMessage}</p>}

          <div className="signin-button-container">
            <button type="submit" className="signin-button" disabled={isLoading}>
              {isLoading ? '로그인 중...' : '로그인'}
            </button>
          </div>
        </form>

        <div className="signin-footer">
          <span>회원이 아니신가요? </span>
          <a onClick={handleSignUp} className="signup-link" role="button">
            회원가입
          </a>
        </div>
      </div>
    </div>
  );
}

export default SignIn;
