// KakaoCallback.jsx
import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from '../api/axiosInstance';

function KakaoCallback() {
    const navigate = useNavigate();

    useEffect(() => {
        const urlParams = new URLSearchParams(window.location.search);
        const authorizationCode = urlParams.get('code');

        console.log(authorizationCode )

        if (authorizationCode) {
            sendAuthorizationCode(authorizationCode);
        } else {
            alert('로그인 실패: 인증 코드가 없습니다.');
            navigate('/signin');
        }
    }, [navigate]);

    const sendAuthorizationCode = async (code) => {
        try {
            const response = await axios.post('http://localhost:8080/altari/kakao/login', null, {
                params: { code },
            });

            const { accessToken } = response.data;

            // 토큰 저장 후 홈 화면으로 이동
            localStorage.setItem('token', accessToken);
            navigate('/home');
        } catch (error) {
            console.error('카카오 로그인 오류:', error);
            alert('카카오 로그인 실패: 서버 오류가 발생했습니다.');
            navigate('/signin');
        }
    };

    return <div>로그인 중입니다...</div>;
}

export default KakaoCallback;
