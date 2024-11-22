import React, { useState, useEffect } from 'react';
import './SignIn.css';
import { useNavigate } from 'react-router-dom';
import apiClient from '../../api/apiClient';

import altariLogo from '../../assets/altari-logo.svg';
import lockerIcon from '../../assets/locker.svg';
import kakaoTalk from '../../assets/kakaotalk.svg';

// Toastify import
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

function SignIn() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const navigate = useNavigate();

  const KAKAO_CLIENT_ID = "e8e345ebe8a751ac4562318628819200";
  const REDIRECT_URI = "http://localhost:3030/kakao/callback";
  const kakaoAuthUrl = `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_CLIENT_ID}&response_type=code&redirect_uri=${REDIRECT_URI}`;

  useEffect(() => {
    const token = localStorage.getItem('token') || sessionStorage.getItem('token');
    if (token) {
      validateToken(token);
    }
  }, [navigate]);

  const validateToken = async (token) => {
    try {
      const response = await apiClient.get('/altari/refresh');
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

    // 입력 필드 검증
    if (!username || !password) {
      setErrorMessage('아이디와 비밀번호를 모두 입력해주세요.');
      setIsLoading(false);
      return;
    }

    try {
      const response = await apiClient.post('/altari/login', {
        username,
        password
      });

      const { accessToken, refreshToken } = response.data;

      if (accessToken && refreshToken) {
        const storage = rememberMe ? localStorage : sessionStorage;
        storage.setItem('token', accessToken);
        storage.setItem('refreshToken', refreshToken);
        storage.setItem('username', username);

        // Toast 알림 추가
        toast.success('로그인 성공!', {
          position: 'top-center',
          autoClose: 2000, // 2초 후 자동 닫힘
        });

        // 2초 후 페이지 이동
        setTimeout(() => {
          navigate('/home');
        }, 2000);
      } else {
        setErrorMessage('로그인 실패: 올바른 아이디와 비밀번호를 입력해주세요.');
      }
    } catch (error) {
      if (error.response && error.response.data && error.response.data.msg) {
        setErrorMessage(error.response.data.msg);
      } else {
        setErrorMessage('로그인 실패: 서버 오류가 발생했습니다.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleKakaoLogin = () => {
    window.location.href = kakaoAuthUrl;
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

          {errorMessage && <p className="error-message">{errorMessage}</p>}

          <div className="signin-button-container">
            <button type="submit" className="signin-button" disabled={isLoading}>
              {isLoading ? '로그인 중...' : '로그인'}
            </button>
          </div>
        </form>

        <div className="signin-footer">
          <button onClick={handleKakaoLogin} className="kakao-login-button">
            <img src={kakaoTalk} alt="KakaoTalk Logo" className="kakao-icon" />
            <p className='KakaoTalk-text'>카카오 로그인</p>
          </button>

          <span>회원이 아니신가요? </span>
          <a onClick={handleSignUp} className="signup-link" role="button">
            회원가입
          </a>
        </div>
      </div>
      {/* Toastify 컨테이너 */}
      <ToastContainer />
    </div>
  );
}

export default SignIn;
