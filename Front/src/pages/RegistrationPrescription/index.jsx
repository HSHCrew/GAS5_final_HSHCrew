import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom'; // useNavigate 추가
import apiClient from '../../api/apiClient'; // axios 설정된 apiClient 불러오기
import './style.css';

function RegistrationPrescription() {
    const navigate = useNavigate(); // useNavigate 초기화
    const [formData, setFormData] = useState({
        userName: '',
        identityFront: '', // 주민등록번호 앞자리
        identityBack: '',  // 주민등록번호 뒷자리
        phoneNo: ''
    });

    const [userData, setUserData] = useState(null); // 첫 번째 API 응답 데이터
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

        if (!formData.userName || !formData.identityFront || !formData.identityBack || !formData.phoneNo) {
            setErrorMessage('모든 필드를 입력해 주세요.');
            return;
        }

        const sanitizedData = {
            userName: formData.userName,
            identity: formData.identityFront + formData.identityBack,
            phoneNo: formData.phoneNo.replace(/-/g, '')
        };

        try {
            console.log('Sending to /altari/prescriptions/enter-info:', sanitizedData);

            const response = await apiClient.post('/altari/prescriptions/enter-info', sanitizedData);

            setUserData(response.data.data);
            setSuccessMessage('카카오톡 인증을 완료한 후 인증확인 버튼을 눌러주세요!');
        } catch (error) {
            const errorMsg = error.response?.data?.message || '인증에 실패했습니다.';
            setErrorMessage(errorMsg);
        }
    };

    const handleCompleteAuthentication = async () => {
        if (!userData) {
            setErrorMessage('먼저 인증을 완료해주세요.');
            return;
        }

        setSuccessMessage('');
        setErrorMessage('');

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
            const response = await apiClient.post('/altari/prescriptions/verify-auth', secondApiData);

            if (response.status === 200) {
                setSuccessMessage('인증 완료가 성공적으로 완료되었습니다.');
                navigate('/medicationManagement'); // 인증 완료 시 페이지 이동
            } else {
                setErrorMessage(`오류가 발생했습니다: ${response.data.message}`);
            }
        } catch (error) {
            const errorMsg = error.response?.data?.message || '네트워크 오류가 발생했습니다.';
            setErrorMessage(errorMsg);
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
