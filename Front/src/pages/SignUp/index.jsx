import React, { useState } from 'react';
import './SignupForm.css';
import { useNavigate } from 'react-router-dom';

import altariLogo from '../../assets/altari-logo.svg';

const SignupForm = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    id: '',
    password: '',
    confirmPassword: '',
    name: '',
    phone: '',
    year: '',
    month: '',
    day: '',
  });

  const [idAvailable, setIdAvailable] = useState(null); // 아이디 중복 여부
  const [checkingId, setCheckingId] = useState(false); // 아이디 확인 중인지 여부

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });

    // 아이디가 변경되면 중복 여부 초기화
    if (name === 'id') {
      setIdAvailable(null);
    }
  };

  // 아이디 중복 확인 함수
  const handleCheckId = () => {
    if (!formData.id) {
      setIdAvailable(null);
      return;
    }

    setCheckingId(true);
    const url = '/api/check-id'; // 실제 아이디 중복 확인 API 엔드포인트로 변경하세요

    fetch(url, {
      method: 'POST', // 또는 GET, 서버 API에 맞게 설정
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ id: formData.id }),
    })
      .then((response) => response.json())
      .then((data) => {
        setIdAvailable(data.available);
      })
      .catch((error) => {
        console.error('아이디 중복 확인 중 오류 발생:', error);
        setIdAvailable(false);
      })
      .finally(() => {
        setCheckingId(false);
      });
  };

  // 회원가입 요청을 서버로 전송하는 함수
  const handleSignUp = () => {
    // 아이디 중복 확인 여부
    if (idAvailable === false) {
      alert('이미 사용 중인 아이디입니다. 다른 아이디를 선택해주세요.');
      return;
    }
    if (idAvailable === null) {
      alert('아이디 중복 확인을 먼저 진행해주세요.');
      return;
    }

    // 비밀번호와 비밀번호 확인 일치 여부 확인
    if (formData.password !== formData.confirmPassword) {
      alert('비밀번호가 일치하지 않습니다.');
      return;
    }

    // 기타 폼 데이터 유효성 검사 추가 가능

    // 서버로 보내기 위한 요청 URL
    const url = '/api/signup';

    // 서버로 보낼 데이터 객체
    const requestData = {
      id: formData.id,
      password: formData.password,
      name: formData.name,
      phone: formData.phone,
      birthdate: `${formData.year}-${formData.month}-${formData.day}`,
    };

    // fetch API를 사용해 서버로 데이터 전송
    fetch(url, {
      method: 'POST', // POST 요청
      headers: {
        'Content-Type': 'application/json', // JSON 데이터 형식으로 전송
      },
      body: JSON.stringify(requestData), // 데이터를 JSON 문자열로 변환
    })
      .then((response) => response.json()) // JSON 응답 처리
      .then((data) => {
        if (data.success) {
          alert('회원가입이 완료되었습니다!');
          navigate('/home'); // 성공 시 홈으로 이동

          // 회원가입 후 Choice 페이지로 이동 (주석 처리 가능)
          navigate('/healthnote/choice');
        } else {
          alert('회원가입에 실패했습니다: ' + data.message);
        }
      })
      .catch((error) => {
        console.error('회원가입 중 오류가 발생했습니다:', error);
        alert('회원가입 중 오류가 발생했습니다.');
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

            {/* 아이디 입력 필드 */}
            <div className="id-section">
              <p className="label-text">아이디</p>
              <div className="id-input-wrapper">
                <input
                  type="text"
                  name="id"
                  value={formData.id}
                  onChange={handleChange}
                  onBlur={handleCheckId} // 입력 필드를 벗어날 때 중복 확인
                  className="input-box"
                  placeholder="아이디를 입력해주세요"
                />
              </div>
              {checkingId && <p className="id-status checking">확인 중...</p>}
              {idAvailable === true && <p className="id-status available">사용 가능한 아이디입니다.</p>}
              {idAvailable === false && <p className="id-status unavailable">이미 사용 중인 아이디입니다.</p>}
            </div>

            {/* 비밀번호 입력 필드 */}
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

            {/* 비밀번호 확인 필드 */}
            <div className="password-confirm-section">
              <p className="label-text">비밀번호 확인</p>
              <input
                type="password"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                className="input-box"
                placeholder="비밀번호를 다시 입력해주세요"
              />
            </div>

            {/* 이름 입력 필드 */}
            <div className="name-section">
              <p className="label-text">이름</p>
              <input
                type="text"
                name="name"
                value={formData.name}
                onChange={handleChange}
                className="input-box"
                placeholder="이름을 입력해주세요"
              />
            </div>

            {/* 전화번호 입력 필드 */}
            <div className="phone-section">
              <p className="label-text">전화번호</p>
              <input
                type="tel"
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                className="input-box"
                placeholder="전화번호를 입력해주세요"
              />
            </div>

            {/* 생년월일 입력 필드 */}
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

            {/* 회원가입 버튼 */}
            <div className="submit-button" onClick={handleSignUp}>
              <p className="submit-text">회원가입</p>
            </div>

            {/* 임시로 Choice 페이지로 이동하는 버튼 추가 */}
            <div className="choice-button" onClick={() => navigate('/healthnote')}>
              <p className="choice-text">건강노트 선택 화면으로 이동 (임시)</p>
            </div>

          </div>

          {/* 로그인 유도 텍스트 */}
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

export default SignupForm;  // 컴포넌트 이름에 맞게 export 수정
