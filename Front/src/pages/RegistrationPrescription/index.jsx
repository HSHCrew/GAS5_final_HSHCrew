import React, { useState } from 'react';
import './style.css';
import { v4 as uuidv4 } from 'uuid'; // UUID 생성 라이브러리 추가

function RegistrationPrescription() {
    const [formData, setFormData] = useState({
        userName: '',
        identity: '',
        phoneNo: ''
    });

    const [userData, setUserData] = useState(null); // 첫 번째 API 응답 데이터
    const [authData, setAuthData] = useState(null); // 두 번째 API 응답 데이터
    const [successMessage, setSuccessMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');

    // 입력 필드 변경 핸들러
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prevState => ({
            ...prevState,
            [name]: value
        }));
    };

    // 하이픈 제거 함수
    const removeHyphens = (value) => {
        return value.replace(/-/g, '');
    };

    // 첫 번째 API 호출: 인증하기
    const handleAuthenticate = async () => {
        setSuccessMessage('');
        setErrorMessage('');

        // 입력값 검증
        if (!formData.userName || !formData.identity || !formData.phoneNo) {
            setErrorMessage('모든 필드를 입력해주세요.');
            return;
        }

        const identityRegex = /^\d{6}-?\d{7}$/;
        if (!identityRegex.test(formData.identity)) {
            setErrorMessage('주민등록번호 형식이 올바르지 않습니다. 예: 000000-0000000 또는 0000000000000');
            return;
        }

        const phoneRegex = /^010-?\d{4}-?\d{4}$/;
        if (!phoneRegex.test(formData.phoneNo)) {
            setErrorMessage('전화번호 형식이 올바르지 않습니다. 예: 010-1234-5678 또는 01012345678');
            return;
        }

        // 하이픈 제거
        const sanitizedData = {
            userName: formData.userName,
            identity: removeHyphens(formData.identity),
            phoneNo: removeHyphens(formData.phoneNo)
        };

        try {
            const response = await fetch('/api/codef/first', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(sanitizedData)
            });

            if (response.ok) {
                const result = await response.json();
                setUserData(result); // userName, identity, phoneNo 저장

                // JWT 토큰을 헤더에서 가져와 로컬 스토리지에 저장
                const authHeader = response.headers.get('Authorization');
                if (authHeader && authHeader.startsWith('Bearer ')) {
                    const token = authHeader.substring(7);
                    localStorage.setItem('token', token);
                }

                setSuccessMessage('인증이 성공적으로 완료되었습니다.');
            } else {
                const errorData = await response.json();
                setErrorMessage(errorData.message || '인증에 실패했습니다.');
            }
        } catch (error) {
            setErrorMessage('네트워크 오류가 발생했습니다.');
            console.error('Error:', error);
        }
    };

    // 두 번째 API 호출: 인증 완료
    const handleCompleteAuthentication = async () => {
        if (!userData) {
            setErrorMessage('먼저 인증을 완료해주세요.');
            return;
        }

        setSuccessMessage('');
        setErrorMessage('');

        // 로컬 스토리지에서 토큰 가져오기
        const token = localStorage.getItem('token');

        if (!token) {
            setErrorMessage('인증 토큰이 없습니다. 다시 인증해주세요.');
            return;
        }

        // 두 번째 API 요청 데이터 생성
        const twoWayInfo = {
            jobIndex: uuidv4(), // 임의의 UUID 사용
            threadIndex: uuidv4(), // 임의의 UUID 사용
            jti: uuidv4(), // 임의의 UUID 사용
            twoWayTimestamp: Date.now() // 현재 시간 밀리초 단위
        };

        const secondApiData = {
            is2Way: true,
            twoWayInfo: twoWayInfo
        };

        try {
            const response = await fetch('/api/codef/second', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}` // JWT 토큰 포함
                },
                body: JSON.stringify(secondApiData)
            });

            if (response.ok) {
                const result = await response.json();
                setAuthData(result); // 두 번째 API의 응답 데이터 저장
                setSuccessMessage('인증 완료가 성공적으로 완료되었습니다.');

                // 인증 완료 후 상태 초기화
                setUserData(null);
                setFormData({
                    userName: '',
                    identity: '',
                    phoneNo: ''
                });
            } else {
                const errorData = await response.json();
                setErrorMessage(errorData.message || '인증 완료에 실패했습니다.');
            }
        } catch (error) {
            setErrorMessage('네트워크 오류가 발생했습니다.');
            console.error('Error:', error);
        }
    };

    return (
        <div className="userinfo-container">
            <div className="userinfo-box">
                <div className="info-section">
                    <h2 className="section-title">처방전 추가</h2>
                    <form className="prescription-form" onSubmit={(e) => e.preventDefault()}>
                        <div className="form-group">
                            <label htmlFor="userName">이름</label>
                            <input
                                type="text"
                                id="userName"
                                name="userName"
                                value={formData.userName}
                                onChange={handleChange}
                                placeholder="이름을 입력하세요"
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="identity">주민등록번호</label>
                            <input
                                type="text"
                                id="identity"
                                name="identity"
                                value={formData.identity}
                                onChange={handleChange}
                                placeholder="0000000000000"
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="phoneNo">전화번호</label>
                            <input
                                type="text"
                                id="phoneNo"
                                name="phoneNo"
                                value={formData.phoneNo}
                                onChange={handleChange}
                                placeholder="01012345678"
                                required
                            />
                        </div>
                        {errorMessage && <p className="error-message">{errorMessage}</p>}
                        {successMessage && <p className="success-message">{successMessage}</p>}
                        <div className="button-group">
                            <button type="button" className="auth-button" onClick={handleAuthenticate}>
                                인증하기
                            </button>
                            <button
                                type="button"
                                className="complete-auth-button"
                                onClick={handleCompleteAuthentication}
                                disabled={!userData}
                            >
                                인증 완료
                            </button>
                        </div>
                    </form>
                </div>
                {authData && (
                    <div className="auth-data-section">
                        <h3>인증 데이터</h3>
                        <p><strong>Job Index:</strong> {authData.jobIndex}</p>
                        <p><strong>Thread Index:</strong> {authData.threadIndex}</p>
                        <p><strong>JTI:</strong> {authData.jti}</p>
                        <p><strong>Two Way Timestamp:</strong> {authData.twoWayTimestamp}</p>
                    </div>
                )}
            </div>
        </div>
    );
}

export default RegistrationPrescription;
