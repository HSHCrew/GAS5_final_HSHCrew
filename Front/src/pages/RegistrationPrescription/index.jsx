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
    
        // 필수 입력 필드가 비어 있는지 확인
        if (!formData.userName || !formData.identityFront || !formData.identityBack || !formData.phoneNo) {
            setErrorMessage('모든 필드를 입력해 주세요.');
            return;
        }
    
        const token = localStorage.getItem('token') || sessionStorage.getItem('token');
        if (!token) {
            setErrorMessage('로그인 토큰이 없습니다. 다시 로그인해주세요.');
            console.warn('Authentication Error: 로그인 토큰이 없습니다.');
            return;
        }
    
        const sanitizedData = {
            userName: formData.userName,
            identity: formData.identityFront + formData.identityBack,
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
        <div className="registration-container">
            <div className="registration-box">
                <div className="info-section">
                    <h2 className="registration-title">처방전 추가</h2>
                    <form className="registration-form" onSubmit={(e) => e.preventDefault()}>
                        <div className="registration-form-group">
                            <label htmlFor="userName">이름</label>
                            <input
                                type="text"
                                id="userName"
                                name="userName"
                                value={formData.userName}
                                onChange={handleChange}
                                placeholder="이름을 입력하세요"
                                className="registration-input"
                                required
                            />
                        </div>
                        <div className="registration-form-group">
                            <label>주민등록번호</label>
                            <div className="registration-identity-inputs">
                                <input
                                    type="text"
                                    name="identityFront"
                                    value={formData.identityFront}
                                    onChange={handleChange}
                                    placeholder="생년월일 (6자리)"
                                    maxLength="6"
                                    className="registration-first-identity-input"
                                    required
                                />
                                <span className="registration-identity-separator">-</span>
                                <input
                                    type="password"
                                    name="identityBack"
                                    value={formData.identityBack}
                                    onChange={handleChange}
                                    placeholder="7자리"
                                    maxLength="7"
                                    className="registration-second-identity-input"
                                    required
                                />
                            </div>
                        </div>
                        <div className="registration-form-group">
                            <label htmlFor="phoneNo">전화번호</label>
                            <input
                                type="text"
                                id="phoneNo"
                                name="phoneNo"
                                value={formData.phoneNo}
                                onChange={handleChange}
                                placeholder="01012345678"
                                className="registration-input"
                                required
                            />
                        </div>
                        {errorMessage && <p className="registration-error-message">{errorMessage}</p>}
                        {successMessage && <p className="registration-success-message">{successMessage}</p>}
                        <div className="registration-button-group">
                            <button type="button" className="registration-auth-button" onClick={handleAuthenticate}>
                                인증하기
                            </button>
                            <button
                                type="button"
                                className="registration-complete-auth-button"
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
