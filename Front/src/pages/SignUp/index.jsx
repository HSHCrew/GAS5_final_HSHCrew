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
  const [passwordMatch, setPasswordMatch] = useState(null);// 비밀번호 일치 여부

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
    const url = '/api/v1/check-username'; // 실제 백엔드 API 경로

    fetch(url, {
      method: 'POST', // POST 요청으로 유지
      headers: {
        'Content-Type': 'application/json',
      },
      // 서버가 'username' 키를 요구하므로, 'id'를 'username'으로 변경
      body: JSON.stringify({ username: formData.id }),
    })
      .then((response) => response.json())
      .then((isDuplicate) => {
        setIdAvailable(!isDuplicate); // 백엔드가 true/false를 반환하면 처리
      })
      .catch((error) => {
        console.error('아이디 중복 확인 중 오류 발생:', error);
        setIdAvailable(false); // 오류 발생 시 false로 처리
      })
      .finally(() => {
        setCheckingId(false);
      });
  };

  // 비밀번호 일치 확인 함수
  const handleCheckPasswordMatch = () => {
    if (formData.password && formData.confirmPassword) {
      setPasswordMatch(formData.password === formData.confirmPassword);
    } else {
      setPasswordMatch(null); // 비밀번호 입력이 없으면 메시지를 초기화
    }
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
  
    // 전화번호 형식 유효성 검사 (간단히 숫자만 포함되었는지 확인)
    const phoneRegex = /^[0-9]{10,11}$/;
    if (!phoneRegex.test(formData.phone)) {
      alert('유효한 전화번호를 입력해주세요.');
      return;
    }
  
    // 생년월일이 제대로 선택되었는지 확인
    if (!formData.year || !formData.month || !formData.day) {
      alert('생년월일을 모두 선택해주세요.');
      return;
    }
  
    // 서버로 보내기 위한 요청 URL
    const url = '/api/v1/register';
  
    // 서버로 보낼 데이터 객체
    const requestData = {
      username: formData.id,
      password: formData.password,
      role: 'USER',
      full_name: formData.name,
      date_of_birth: `${formData.year}-${String(formData.month).padStart(2, '0')}-${String(formData.day).padStart(2, '0')}`,
      phone_number: formData.phone,

      //Api 수정 전 json 전송을 위한 null 값 대체
      
      height: null, // 키 정보는 현재 null로 설정
      weight: null, // 몸무게 정보는 현재 null로 설정
      blood_type: null, // 혈액형 정보는 현재 null로 설정
      morning_medication_time: null, // 복약 시간 정보도 null
      lunch_medication_time: null,
      dinner_medication_time: null,
      disease_id: null, // 질병 정보도 null
      medication_id: null, // 약물 정보도 null
      food_name: null, // 음식 알러지 정보도 null
      family_relation: null, // 가족력 정보도 null
    };
  
    // fetch API를 사용해 서버로 데이터 전송
    fetch(url, {
      method: 'POST', // POST 요청
      headers: {
        'Content-Type': 'application/json', // JSON 데이터 형식으로 전송
      },
      body: JSON.stringify(requestData), // 데이터를 JSON 문자열로 변환
    })
      .then((response) => {
        if (!response.ok) {
          throw new Error('네트워크 응답에 문제가 있습니다.');
        }
        return response.json();
      })
   // JSON 응답 처리
      .then((data) => {
        if (data) {
          alert('회원가입이 완료되었습니다!');
          navigate('/healthNote'); // 성공 시 홈으로 이동
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
              <div className="password-input-wrapper">
                <input
                  type="password"
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  onBlur={handleCheckPasswordMatch}
                  className="input-box"
                  placeholder="비밀번호를 다시 입력해주세요"
                />
              </div>
              {passwordMatch === false && <p className="password-status unavailable">비밀번호가 일치하지 않습니다.</p>}
              {passwordMatch === true && <p className="password-status available">비밀번호가 일치합니다.</p>}
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
