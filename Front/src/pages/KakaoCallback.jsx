import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from '../api/axiosInstance';

function KakaoCallback() {
    const navigate = useNavigate();

    useEffect(() => {
        const urlParams = new URLSearchParams(window.location.search);
        const authorizationCode = urlParams.get('code');

        if (authorizationCode) {
            sendAuthorizationCode(authorizationCode);
        } else {
            alert('로그인 실패: 인증 코드가 없습니다.');
            navigate('/signin');
        }
    }, [navigate]);

    // JWT에서 Payload 파싱하는 함수
    const parseJwt = (token) => {
        try {
            const base64Url = token.split('.')[1]; // JWT의 Payload 추출
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(
                atob(base64)
                    .split('')
                    .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                    .join('')
            );
            return JSON.parse(jsonPayload); // JSON 객체로 반환
        } catch (error) {
            console.error('토큰 파싱 오류:', error);
            return null;
        }
    };

    const sendAuthorizationCode = async (code) => {
        try {
            const response = await axios.post('http://localhost:8080/altari/kakao/login', null, {
                params: { code },
            });

            const { accessToken, refreshToken } = response.data;

            if (accessToken && refreshToken) {
                // Access Token에서 username 파싱
                const parsedToken = parseJwt(accessToken);
                const username = parsedToken?.username;

                if (!username) {
                    alert('로그인 실패: 사용자 정보를 파싱할 수 없습니다.');
                    navigate('/signin');
                    return;
                }

                // Local Storage에 데이터 저장
                localStorage.setItem('accessToken', accessToken);
                localStorage.setItem('refreshToken', refreshToken);
                localStorage.setItem('username', username);

                navigate('/home');
            } else {
                alert('로그인 실패: 필요한 데이터가 누락되었습니다.');
                navigate('/signin');
            }
        } catch (error) {
            console.error('카카오 로그인 오류:', error);
            alert('카카오 로그인 실패: 서버 오류가 발생했습니다.');
            navigate('/signin');
        }
    };

    return <div>로그인 중입니다...</div>;
}

export default KakaoCallback;
