import React from 'react';
import { useNavigate } from 'react-router-dom';
import './Step9.css';
import altariLogo from '../../../assets/altari-logo.svg';
import arrowIcon from '../../../assets/arrow.svg';
import morningIcon from '../../../assets/sunrise.svg';
import afternoonIcon from '../../../assets/sun.svg';
import eveningIcon from '../../../assets/moon.svg';

const Step9 = () => {
  const navigate = useNavigate();

  const goToStep8 = () => {
    navigate('/healthnote/step8'); 
  };

  // 건강노트 작성 완료 확인
  const completeHealthNote = () => {
    const confirmed = window.confirm('건강노트 작성을 완료하시겠습니까?');
    if (confirmed) {
      navigate('/home');
    }
  };

  return (
    <div className="step9-container">
      <div className="step9-inner-container">
        <div className="step9-logo">
          <img src={altariLogo} alt="Altari Logo" className="step9-logo-image" />
        </div>

        <div className="step9-content-container">
            {/* 아침 */}
            <div className="step9-time-row">
                <div className="step9-time-icon-container">
                <img src={morningIcon} alt="Morning Icon" className="step9-icon" />
                <p className="step9-label">아침</p>
                </div>
                <div className="step9-time-select">
                <select className="step9-select">
                    {Array.from({ length: 24 }, (_, i) => (
                    <option key={i} value={i < 10 ? `0${i}` : i}>
                        {i < 10 ? `0${i}` : i}
                    </option>
                    ))}
                </select>
                <span className="step9-colon">:</span>
                <select className="step9-select">
                    {Array.from({ length: 60 }, (_, i) => (
                    <option key={i} value={i < 10 ? `0${i}` : i}>
                        {i < 10 ? `0${i}` : i}
                    </option>
                    ))}
                </select>
                </div>
            </div>

            {/* 점심 */}
            <div className="step9-time-row">
                <div className="step9-time-icon-container">
                <img src={afternoonIcon} alt="Afternoon Icon" className="step9-icon" />
                <p className="step9-label">점심</p>
                </div>
                <div className="step9-time-select">
                <select className="step9-select">
                    {Array.from({ length: 24 }, (_, i) => (
                    <option key={i} value={i < 10 ? `0${i}` : i}>
                        {i < 10 ? `0${i}` : i}
                    </option>
                    ))}
                </select>
                <span className="step9-colon">:</span>
                <select className="step9-select">
                    {Array.from({ length: 60 }, (_, i) => (
                    <option key={i} value={i < 10 ? `0${i}` : i}>
                        {i < 10 ? `0${i}` : i}
                    </option>
                    ))}
                </select>
                </div>
            </div>

            {/* 저녁 */}
            <div className="step9-time-row">
                <div className="step9-time-icon-container">
                <img src={eveningIcon} alt="Evening Icon" className="step9-icon" />
                <p className="step9-label">저녁</p>
                </div>
                <div className="step9-time-select">
                <select className="step9-select">
                    {Array.from({ length: 24 }, (_, i) => (
                    <option key={i} value={i < 10 ? `0${i}` : i}>
                        {i < 10 ? `0${i}` : i}
                    </option>
                    ))}
                </select>
                <span className="step9-colon">:</span>
                <select className="step9-select">
                    {Array.from({ length: 60 }, (_, i) => (
                    <option key={i} value={i < 10 ? `0${i}` : i}>
                        {i < 10 ? `0${i}` : i}
                    </option>
                    ))}
                </select>
                </div>
            </div>
            </div>


        {/* 하단 단계 및 화살표 */}
        <div className="step9-footer">
        <img 
            src={arrowIcon} 
            alt="Previous Arrow" 
            className="step8-prev-arrow" 
            style={{ transform: 'rotate(180deg)' }} 
            onClick={goToStep8} 
          />
          <p className="step9-step-indicator">9 / 9</p>
          <img
            src={arrowIcon}
            alt="Next Arrow"
            className="step9-next-arrow"
            onClick={completeHealthNote}
          />
        </div>
      </div>
    </div>
  );
};

export default Step9;
