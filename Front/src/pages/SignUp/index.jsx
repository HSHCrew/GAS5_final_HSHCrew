import React, { useState } from 'react';
import './SignupForm.css';
import { useNavigate } from 'react-router-dom';

import altariLogo from '../../assets/altari-logo.svg';

const SignupForm = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    confirmPassword: '',
    fullName: '',
    phoneNumber: '',
    year: '',
    month: '',
    day: '',
  });

  const [idAvailable, setIdAvailable] = useState(null);
  const [checkingId, setCheckingId] = useState(false);
  const [passwordMatch, setPasswordMatch] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });

    if (name === 'username') {
      setIdAvailable(null);
    }
  };

  const handleCheckId = () => {
    if (!formData.username) {
      setIdAvailable(null);
      return;
    }

    setCheckingId(true);
    const url = 'http://localhost:8080/altari/check/username';

    fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username: formData.username }),
    })
      .then((response) => response.json())
      .then((isDuplicate) => {
        setIdAvailable(!isDuplicate);
      })
      .catch((error) => {
        console.error('아이디 중복 확인 중 오류 발생:', error);
        setIdAvailable(false);
      })
      .finally(() => {
        setCheckingId(false);
      });
  };

  const handleCheckPasswordMatch = () => {
    if (formData.password && formData.confirmPassword) {
      setPasswordMatch(formData.password === formData.confirmPassword);
    } else {
      setPasswordMatch(null);
    }
  };

  const handleSignUp = () => {
    if (idAvailable === false) {
      alert('이미 사용 중인 아이디입니다. 다른 아이디를 선택해주세요.');
      return;
    }
    if (idAvailable === null) {
      alert('아이디 중복 확인을 먼저 진행해주세요.');
      return;
    }

    if (formData.password !== formData.confirmPassword) {
      alert('비밀번호가 일치하지 않습니다.');
      return;
    }

    const phoneRegex = /^[0-9]{10,11}$/;
    if (!phoneRegex.test(formData.phoneNumber)) {
      alert('유효한 전화번호를 입력해주세요.');
      return;
    }

    if (!formData.year || !formData.month || !formData.day) {
      alert('생년월일을 모두 선택해주세요.');
      return;
    }

    const registerUrl = 'http://localhost:8080/altari/register';
    const loginUrl = 'http://localhost:8080/altari/login';

    const requestData = {
      username: formData.username,
      password: formData.password,
      role: 'USER',
      fullName: formData.fullName,
      dateOfBirth: `${formData.year}-${String(formData.month).padStart(2, '0')}-${String(formData.day).padStart(2, '0')}`,
      phoneNumber: formData.phoneNumber,
    };

    // 회원가입 요청
    fetch(registerUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(requestData),
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error('회원가입에 실패했습니다.');
        }
        return response.json();
      })
      .then((data) => {
        alert('회원가입이 완료되었습니다!');
        
        // 자동 로그인 요청
        return fetch(loginUrl, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ username: formData.username, password: formData.password }),
        });
      })
      .then((loginResponse) => {
        if (!loginResponse.ok) {
          throw new Error('자동 로그인에 실패했습니다.');
        }
        return loginResponse.json();
      })
      .then((loginData) => {
        // 로그인 성공 시 토큰과 사용자 이름 저장
        localStorage.setItem('token', loginData.accessToken);
        localStorage.setItem('refreshToken', loginData.refreshToken);
        localStorage.setItem('username', formData.username); // username 저장

        // 건강노트 선택 화면으로 이동
        navigate('/healthNote');
      })
      .catch((error) => {
        console.error('오류:', error);
        alert('오류가 발생했습니다: ' + error.message);
      });
  };

  return (
    <div className="signup-container">
      <div className="signup-box">
        <img src={altariLogo} className="signup-image" alt="signup" />
        <div className="signup-form-container">
          <div className="form-box">
            <div className="form-header">
              <p className="signup-title">회원가입</p>
            </div>

            <div className="id-section">
              <p className="label-text">아이디</p>
              <div className="id-input-wrapper">
                <input
                  type="text"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  onBlur={handleCheckId}
                  className="input-box"
                  placeholder="아이디를 입력해주세요"
                />
              </div>
              {checkingId && <p className="id-status checking">확인 중...</p>}
              {idAvailable === true && <p className="id-status available">사용 가능한 아이디입니다.</p>}
              {idAvailable === false && <p className="id-status unavailable">이미 사용 중인 아이디입니다.</p>}
            </div>

            <div className="password-section">
              <p className="label-text">비밀번호</p>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className="input-box"
                placeholder="비밀번호를 입력해주세요"
              />
            </div>

            <div className="password-confirm-section">
              <p className="label-text">비밀번호 확인</p>
              <input
                type="password"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                onBlur={handleCheckPasswordMatch}
                className="input-box"
                placeholder="비밀번호를 다시 입력해주세요"
              />
              {passwordMatch === false && <p className="password-status unavailable">비밀번호가 일치하지 않습니다.</p>}
              {passwordMatch === true && <p className="password-status available">비밀번호가 일치합니다.</p>}
            </div>

            <div className="name-section">
              <p className="label-text">이름</p>
              <input
                type="text"
                name="fullName"
                value={formData.fullName}
                onChange={handleChange}
                className="input-box"
                placeholder="이름을 입력해주세요"
              />
            </div>

            <div className="phone-section">
              <p className="label-text">전화번호</p>
              <input
                type="tel"
                name="phoneNumber"
                value={formData.phoneNumber}
                onChange={handleChange}
                className="input-box"
                placeholder="전화번호를 입력해주세요"
              />
            </div>

            <div className="birthdate-section">
              <p className="label-text">생년월일</p>
              <div className="birthdate-select">
                <select
                  className="select-box"
                  name="year"
                  value={formData.year}
                  onChange={handleChange}
                >
                  <option value="">년</option>
                  {Array.from({ length: 100 }, (_, i) => (
                    <option key={i} value={2024 - i}>
                      {2024 - i}
                    </option>
                  ))}
                </select>
                <select
                  className="select-box"
                  name="month"
                  value={formData.month}
                  onChange={handleChange}
                >
                  <option value="">월</option>
                  {Array.from({ length: 12 }, (_, i) => (
                    <option key={i} value={i + 1}>
                      {i + 1}
                    </option>
                  ))}
                </select>
                <select
                  className="select-box"
                  name="day"
                  value={formData.day}
                  onChange={handleChange}
                >
                  <option value="">일</option>
                  {Array.from({ length: 31 }, (_, i) => (
                    <option key={i} value={i + 1}>
                      {i + 1}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="submit-button" onClick={handleSignUp}>
              <p className="submit-text">회원가입</p>
            </div>

            <div className="choice-button" onClick={() => navigate('/healthNote')}>
              <p className="choice-text">건강노트 선택 화면으로 이동 (임시)</p>
            </div>
          </div>

          <div className="login-prompt">
            <p>
              회원이신가요?
              <span className="login-link" onClick={() => navigate('/signIn')}>
                로그인
              </span>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SignupForm;
