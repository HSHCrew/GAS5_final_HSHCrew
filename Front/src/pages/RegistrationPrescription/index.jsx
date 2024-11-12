import React, { useState } from 'react';
import './style.css';

function RegistrationPrescription() {
    const [formData, setFormData] = useState({
        userName: '',
        identityFront: '', // 주민등록번호 앞자리
        identityBack: '',  // 주민등록번호 뒷자리
        phoneNo: ''
    });

    const [userData, setUserData] = useState(null); // 첫 번째 API 응답 데이터
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

    // 첫 번째 API 호출: 인증하기
    const handleAuthenticate = async () => {
        setSuccessMessage('');
        setErrorMessage('');

        // 로그인 토큰 가져오기
        const token = localStorage.getItem('token') || sessionStorage.getItem('token');
        if (!token) {
            setErrorMessage('로그인 토큰이 없습니다. 다시 로그인해주세요.');
            console.warn('Authentication Error: 로그인 토큰이 없습니다.');
            return;
        }

        // 주민등록번호 앞자리와 뒷자리를 합쳐서 하나의 문자열로 전송
        const sanitizedData = {
            userName: formData.userName,
            identity: formData.identityFront + formData.identityBack, // 주민번호 앞자리와 뒷자리 합침
            phoneNo: formData.phoneNo.replace(/-/g, '')
        };

        console.log('Sending to /api/codef/first:', sanitizedData);

        try {
            const response = await fetch('http://localhost:8080/altari/prescriptions/enter-info', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(sanitizedData)
            });

            if (response.ok) {
                const result = await response.json();
                setUserData(result.data);
                setSuccessMessage('카카오톡 인증을 완료한 후 인증확인 버튼을 눌러주세요!');
            } else {
                const errorData = await response.json();
                setErrorMessage(errorData.message || '인증에 실패했습니다.');
            }
        } catch (error) {
            setErrorMessage('네트워크 오류가 발생했습니다.');
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

        const token = localStorage.getItem('token') || sessionStorage.getItem('token');
        if (!token) {
            setErrorMessage('인증 토큰이 없습니다. 다시 로그인해주세요.');
            return;
        }

        const secondApiData = {
            is2Way: true,
            twoWayInfo: {
                jobIndex: userData.jobIndex,
                threadIndex: userData.threadIndex,
                jti: userData.jti,
                twoWayTimestamp: userData.twoWayTimestamp
            }
        };

        try {
            const response = await fetch('http://localhost:8080/altari/prescriptions/verify-auth', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(secondApiData)
            });

            if (response.ok) {
                setSuccessMessage('인증 완료가 성공적으로 완료되었습니다.');
            } else {
                const errorText = await response.text();
                setErrorMessage(`오류가 발생했습니다: ${errorText}`);
            }
        } catch (error) {
            setErrorMessage('네트워크 오류가 발생했습니다.');
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
                            <label>주민등록번호</label>
                            <div className="identity-inputs">
                                <input
                                    type="text"
                                    name="identityFront"
                                    value={formData.identityFront}
                                    onChange={handleChange}
                                    placeholder="생년월일 (6자리)"
                                    maxLength="6"
                                    required
                                />
                                <span>-</span>
                                <input
                                    type="password"
                                    name="identityBack"
                                    value={formData.identityBack}
                                    onChange={handleChange}
                                    placeholder="7자리"
                                    maxLength="7"
                                    required
                                />
                            </div>
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
            </div>
        </div>
    );
}

export default RegistrationPrescription;
