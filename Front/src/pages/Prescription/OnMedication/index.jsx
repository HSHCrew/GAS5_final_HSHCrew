import React from "react";
import './style.css';
import { useNavigate } from 'react-router-dom';

import tylenolIcon from '../../../assets/tylenol.svg';
import backIcon from '../../../assets/left.svg';
import plusIcon from '../../../assets/plus.svg';

const OnMedication = () => {
  const navigate = useNavigate();

  const handleBackClick = () => {
    navigate(-1); // 뒤로 가기
  };

  const handleMedicationClick = () => {
    navigate('/medicineinfo'); // 약 정보 페이지로 이동
  };

  return (
    <div className="prescription-page-container">
      <div className="prescription-page-header">
        <img
          src={backIcon}
          alt="Back Icon"
          className="prescription-page-back-button"
          onClick={handleBackClick}
        />
        <img src={plusIcon} alt="처방 아이콘" className="plus-icon" />
        <div className="prescription-title-container">
            <h2 className="prescription-title">편두통</h2>
            <p className="prescription-date">2024.10.3 ~ 2024.10.5</p>
        </div>
      </div>

      <div className="prescription-schedule">
        <h3 className="schedule-sub-title">복약일정</h3>
        <p className="schedule-remaining-days">남은기간 <span className="schedule-days"><br />3일</span></p>

        <div className="schedule-progress-bar-container">
          <div className="schedule-progress-bar" style={{ width: '25%' }}></div>
        </div>

        <div className="schedule-day-count">
          <p className="start-day">1일차</p>
          <p className="total-days">총 4일</p>
        </div>
      </div>

      <div className="prescription-details-card">
        {/* 타이레놀 이미지, 클릭 시 페이지 이동 */}
        <div className="medication-image-container" onClick={handleMedicationClick}>
          <img src={tylenolIcon} alt="Tylenol" className="endprescription-medication-image" />
        </div>

        {/* 약 정보 텍스트, 클릭 시 페이지 이동 */}
        <p className="medication-info" onClick={handleMedicationClick}>
          타이레놀8시간이알서방정 325mg<br />1정
        </p>
      </div>
    </div>
  );
};

export default OnMedication;
