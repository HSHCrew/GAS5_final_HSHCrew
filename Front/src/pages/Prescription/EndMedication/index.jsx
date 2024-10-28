import React from "react";
import './style.css';
import { useNavigate } from 'react-router-dom';

import backIcon from '../../../assets/left.svg';
import plusIcon from '../../../assets/plus.svg';

const EndMedication = ({ progressPercentage = 50 }) => {
  const navigate = useNavigate(); // navigate 정의

  const handleBackClick = () => {
    navigate(-1); // 뒤로 가기
  };

  const circumference = 2 * Math.PI * 100; // 원 둘레
  const offset = circumference - (circumference * progressPercentage) / 100;

  // 성공률에 따른 메시지 설정
  let completionMessage;
  if (progressPercentage === 100) {
    completionMessage = "완벽하게 복약을 끝 맞혔어요!";
  } else if (progressPercentage >= 70) {
    completionMessage = "잘하셨어요! 복약을 거의 완벽히 수행하셨습니다.";
  } else if (progressPercentage >= 50) {
    completionMessage = "복약이 절반 이상 성공적으로 완료되었습니다.";
  } else {
    completionMessage = (
      <>
        복약을 끝까지 지켜나가기가 어려웠네요.
        <br />
        다음엔 더 잘해봐요!
      </>
    );
  }

  return (
    <div className="prescription-complete-page-container">
      <div className="prescription-complete-header">
        <img
          src={backIcon}
          alt="Back Icon"
          className="prescription-complete-back-button"
          onClick={handleBackClick} // 클릭 시 뒤로 가기
        />
        <img src={plusIcon} alt="처방 아이콘" className="plus-icon" />
        <div className="prescription-complete-title-container">
            <h2 className="prescription-complete-title">편두통</h2>
            <p className="prescription-complete-date">2024.10.3 ~ 2024.10.5</p>
        </div>
      </div>

      <div className="prescription-complete-schedule">
        <h3 className="prescription-complete-schedule-title">복약일정</h3>
        <p className="prescription-complete-status">복약종료</p>

        {/* 스케줄 진행 바 */}
        <div className="schedule-progress-bar-container">
          <div className="schedule-progress-bar" style={{ width: `${100}%` }}></div>
        </div>

        <div className="prescription-complete-day-count">
          <p>4일차</p>
          <p>총 4일</p>
        </div>
      </div>

      <div className="prescription-complete-success">
        <h3 className="prescription-complete-success-title">복약성공도</h3>
        <div className="circular-progress">
          <svg className="progress-ring" width="230" height="230">
            <circle
              className="progress-ring__background"
              cx="115"
              cy="115"
              r="100"
            />
            <circle
              className="progress-ring__circle"
              cx="115"
              cy="115"
              r="100"
              strokeDasharray={circumference}
              strokeDashoffset={offset} // 진행률에 따라 동적으로 offset 조정
            />
          </svg>
          <div className="circular-progress-text">
            <p className="progress-percentage">{progressPercentage}%</p>
            <p className="progress-label">성공률</p>
          </div>
        </div>
        <p className="completion-message">{completionMessage}</p>
      </div>
    </div>
  );
};

export default EndMedication;
