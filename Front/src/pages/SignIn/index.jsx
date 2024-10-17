import React, { useState } from 'react';
import './SignIn.css'; // SignIn 스타일 연결

// 이미지 파일을 import 합니다.
import altariLogo from '../../assets/altari-logo.svg';
import lockerIcon from '../../assets/locker.svg';

function SignIn() {

  const dummyData = {
    username: 'testuser',
    password: 'password123'
  };
  
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  // 로그인 버튼 클릭 시 동작하는 함수
  const handleLogin = () => {
    if (username === '') {
      alert('아이디를 입력해주세요.');
    } else if (password === '') {
      alert('비밀번호를 입력해주세요.')
    } else if (username !== 'testuser') {
      alert('아이디를 확인해주세요.');
    } else if (password !== 'password123') {
      alert('비밀번호를 확인해주세요.')
    } else {
      console.log('아이디:', username, '비밀번호:', password);
      alert('로그인 되었습니다.')
      // alert('로그인 시도 중...'); 
      }
  };

  return (
    <div className="signin-container">
      <div className="signin-box">
        <img
          src={altariLogo} // 불러온 이미지를 사용합니다.
          alt="Logo"
          className="signin-logo"
        />

        <div className="signin-form">
          <div className="signin-header">
            <img
              src={lockerIcon} // 불러온 이미지를 사용합니다.
              alt="Login icon"
              className="signin-icon"
            />
            <p className="signin-title">로그인</p>
          </div>

          <div className="signin-input-container">
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
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="비밀번호를 입력해주세요."
              className="signin-input"
            />
          </div>

          <div className="signin-button-container">
            <button
              onClick={handleLogin}
              className="signin-button"
            >
              로그인
            </button>
          </div>
        </div>

        <div className="signin-footer">
          <span>회원이 아니신가요? </span>
          <a href="#" className="signup-link">
            회원가입
          </a>
        </div>
      </div>
    </div>
  );
}

export default SignIn;