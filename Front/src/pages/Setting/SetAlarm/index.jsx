import React, { useState, useEffect } from 'react';
import apiClient from '../../../api/apiClient'; // Axios 설정된 apiClient 불러오기
import Header from '../../../components/Header'; // Header 컴포넌트 가져오기

import morningIcon from '../../../assets/sunrise.svg';
import afternoonIcon from '../../../assets/sun.svg';
import eveningIcon from '../../../assets/moon.svg';

import './style.css'; // 스타일을 위한 CSS

const SetAlarm = () => {
  const [medicationTimes, setMedicationTimes] = useState({
    morning: '',
    lunch: '',
    dinner: ''
  });
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [loading, setLoading] = useState(true);

  const username = localStorage.getItem('username') || sessionStorage.getItem('username');

  const fetchUserProfile = async () => {
    if (!username) {
      setErrorMessage('로그인 정보가 없습니다. 다시 로그인해주세요.');
      return;
    }

    setLoading(true);
    setErrorMessage('');
    try {
      const response = await apiClient.get(`/altari/getInfo/userProfile/${username}`);
      const { morningMedicationTime, lunchMedicationTime, dinnerMedicationTime } = response.data;

      const formatTime = (timeArray) =>
        Array.isArray(timeArray) && timeArray.length === 2
          ? `${String(timeArray[0]).padStart(2, '0')}:${String(timeArray[1]).padStart(2, '0')}`
          : '';

      setMedicationTimes({
        morning: formatTime(morningMedicationTime),
        lunch: formatTime(lunchMedicationTime),
        dinner: formatTime(dinnerMedicationTime),
      });
    } catch (error) {
      setErrorMessage('사용자 데이터를 불러오는 데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUserProfile();
  }, []);

  const handleTimeChange = (e) => {
    const { name, value } = e.target;
    setMedicationTimes((prev) => ({
      ...prev,
      [name]: value
    }));
  };

  const updateMedicationTimes = async () => {
    if (!username) {
      setErrorMessage('로그인 정보가 없습니다. 다시 로그인해주세요.');
      return;
    }

    setSuccessMessage('');
    setErrorMessage('');

    try {
      const [morningHour, morningMinute] = medicationTimes.morning.split(':').map(Number);
      const [lunchHour, lunchMinute] = medicationTimes.lunch.split(':').map(Number);
      const [dinnerHour, dinnerMinute] = medicationTimes.dinner.split(':').map(Number);

      const updateData = {
        morningMedicationTime: [morningHour, morningMinute],
        lunchMedicationTime: [lunchHour, lunchMinute],
        dinnerMedicationTime: [dinnerHour, dinnerMinute]
      };

      await apiClient.put(`/altari/updateInfo/userProfile/${username}`, updateData);
      setSuccessMessage('저장 성공 ⊂◉‿◉つ ');
    } catch (error) {
      setErrorMessage('저장 실패 (｡•́︿•̀｡) ');
    }
  };

  return (
    <div className="set-alarm-container">
      <Header title="복약 알림 설정" backButton /> {/* Header 컴포넌트 사용 */}
      <div className="set-alarm-box">
        <p className="set-alarm-description">
          복약 알림을 위해 식사 완료 시간을 알려주세요!
        </p>
        {loading ? (
          <p className="loading-text">로딩 중...</p>
        ) : (
          <>
            {errorMessage && <p className="error-message">{errorMessage}</p>}
            {successMessage && <p className="success-message">{successMessage}</p>}
            <div className="alarm-time-row">
              <div className="alarm-time-icon-container">
                <img src={morningIcon} alt="Morning Icon" className="alarm-icon" />
                <p className="alarm-label">아침</p>
              </div>
              <input
                type="time"
                id="morning"
                name="morning"
                value={medicationTimes.morning}
                onChange={handleTimeChange}
                className="time-input"
              />
            </div>
            <div className="alarm-time-row">
              <div className="alarm-time-icon-container">
                <img src={afternoonIcon} alt="Afternoon Icon" className="alarm-icon" />
                <p className="alarm-label">점심</p>
              </div>
              <input
                type="time"
                id="lunch"
                name="lunch"
                value={medicationTimes.lunch}
                onChange={handleTimeChange}
                className="time-input"
              />
            </div>
            <div className="alarm-time-row">
              <div className="alarm-time-icon-container">
                <img src={eveningIcon} alt="Evening Icon" className="alarm-icon" />
                <p className="alarm-label">저녁</p>
              </div>
              <input
                type="time"
                id="dinner"
                name="dinner"
                value={medicationTimes.dinner}
                onChange={handleTimeChange}
                className="time-input"
              />
            </div>
            <button onClick={updateMedicationTimes} className="update-button">
              저장하기
            </button>
          </>
        )}
      </div>
    </div>
  );
};

export default SetAlarm;
