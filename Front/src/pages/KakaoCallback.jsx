import React, { useEffect, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/axiosInstance';

function KakaoCallback() {
  const navigate = useNavigate();
  const isCalledRef = useRef(false); // 중복 호출 방지를 위한 Ref 사용
  const [errorMessage, setErrorMessage] = useState(null); // 에러 메시지 상태 관리

  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const authorizationCode = urlParams.get('code');

    if (authorizationCode && !isCalledRef.current) {
      isCalledRef.current = true; // 첫 실행 시에만 호출
      sendAuthorizationCode(authorizationCode);
    } else if (!authorizationCode) {
      setErrorMessage('로그인 실패: 인증 코드가 없습니다.');
      navigate('/signin');
    }
  }, [navigate]);

  const parseJwt = (token) => {
    try {
      const base64Payload = token.split('.')[1];
      const decodedPayload = atob(base64Payload); // base64 디코딩
      return JSON.parse(decodedPayload); // JSON 파싱
    } catch (error) {
      setErrorMessage('JWT 파싱 오류');
      return null;
    }
  };

  const sendAuthorizationCode = async (code) => {
    try {
      const response = await apiClient.post('/altari/kakao/login', null, {
        params: { code },
      });

      const { accessToken, refreshToken } = response.data;

      if (!accessToken || !refreshToken) {
        throw new Error('서버에서 필요한 토큰을 받지 못했습니다.');
      }

      const decodedAccessToken = parseJwt(accessToken);
      const username = decodedAccessToken?.username;

      if (!username) {
        throw new Error('Access Token에서 사용자 이름을 파싱하지 못했습니다.');
      }

      // 토큰과 사용자 정보 저장
      localStorage.setItem('token', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('username', username);

      // 홈 화면으로 이동
      navigate('/home');
    } catch (error) {
      setErrorMessage(`카카오 로그인 실패: ${error.message || '서버 오류가 발생했습니다.'}`);
      navigate('/signin');
    }
  };

  return (
    <div>
      {errorMessage ? <p>{errorMessage}</p> : <p>로그인 중입니다...</p>}
    </div>
  );
}

export default KakaoCallback;
