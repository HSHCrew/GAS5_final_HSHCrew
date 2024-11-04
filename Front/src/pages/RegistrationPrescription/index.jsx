import React, { useState } from 'react';
import './style.css';

function RegistrationPrescription() {
    const [formData, setFormData] = useState({
        name: '',
        residentNumber: '',
        phoneNumber: ''
    });

    const [authData, setAuthData] = useState(null);
    const [successMessage, setSuccessMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prevState => ({
            ...prevState,
            [name]: value
        }));
    };

    const handleAuthenticate = async () => {
        setSuccessMessage('');
        setErrorMessage('');

        if (!formData.name || !formData.residentNumber || !formData.phoneNumber) {
            setErrorMessage('모든 필드를 입력해주세요.');
            return;
        }

        const residentRegex = /^\d{6}-\d{7}$/;
        if (!residentRegex.test(formData.residentNumber)) {
            setErrorMessage('주민등록번호 형식이 올바르지 않습니다. 예: 000000-0000000');
            return;
        }

        const phoneRegex = /^010-\d{4}-\d{4}$/;
        if (!phoneRegex.test(formData.phoneNumber)) {
            setErrorMessage('전화번호 형식이 올바르지 않습니다. 예: 010-1234-5678');
            return;
        }

        try {
            const response = await fetch('/api/codef/first', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(formData)
            });

            if (response.ok) {
                const result = await response.json();
                // 예상 응답 형식에 따라 authData 설정
                // 예: { jobIndex, threadIndex, jti, twoWayTimestamp }
                setAuthData(result);
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

    const handleCompleteAuthentication = async () => {
        if (!authData) {
            setErrorMessage('먼저 인증을 완료해주세요.');
            return;
        }

        setSuccessMessage('');
        setErrorMessage('');

        // 로컬 스토리지에서 토큰 가져오기
        const token = localStorage.getItem('token');

        if (!token) {
            setErrorMessage('인증 토큰이 없습니다. 다시 로그인해주세요.');
            return;
        }

        try {
            const response = await fetch('/api/codef/second', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}` // 실제 토큰 사용
                },
                body: JSON.stringify(authData)
            });

            if (response.ok) {
                const result = await response.json();
                setSuccessMessage('인증 완료가 성공적으로 완료되었습니다.');
                setAuthData(null);
                setFormData({
                    name: '',
                    residentNumber: '',
                    phoneNumber: ''
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
                            <label htmlFor="name">이름</label>
                            <input
                                type="text"
                                id="name"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                placeholder="이름을 입력하세요"
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="residentNumber">주민등록번호</label>
                            <input
                                type="text"
                                id="residentNumber"
                                name="residentNumber"
                                value={formData.residentNumber}
                                onChange={handleChange}
                                placeholder="000000-0000000"
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="phoneNumber">전화번호</label>
                            <input
                                type="text"
                                id="phoneNumber"
                                name="phoneNumber"
                                value={formData.phoneNumber}
                                onChange={handleChange}
                                placeholder="010-1234-5678"
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
                                disabled={!authData}
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
