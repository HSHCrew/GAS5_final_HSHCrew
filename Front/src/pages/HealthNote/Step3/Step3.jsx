import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom'; // 페이지 이동을 위한 useNavigate 추가
import './Step3.css';

import altariLogo from '../../../assets/altari-logo.svg';
import arrowIcon from '../../../assets/arrow.svg';
import bloodIcon from '../../../assets/blood.svg'; // 혈액형 아이콘 이미지 추가

const Step3 = () => {
  const navigate = useNavigate();
  const [rhValue, setRhValue] = useState('Rh+'); // Rh 값 상태
  const [bloodType, setBloodType] = useState('A'); // 혈액형 값 상태

  // Rh 값 변경 핸들러
  const handleRhChange = (event) => {
    setRhValue(event.target.value);
  };

  // 혈액형 값 변경 핸들러
  const handleBloodTypeChange = (event) => {
    setBloodType(event.target.value);
  };

  // 이전 페이지로 이동 (Step2)
  const goToStep2 = () => {
    navigate('/healthnote/step2');
  };

  // 다음 페이지로 이동 (Step4)
  const goToStep4 = () => {
    navigate('/healthnote/step4');
  };

  return (
    <div className="step3-container">
      <div className="step3-inner-container">
        <div className="step3-logo">
          <img src={altariLogo} alt="Altari Logo" className="step3-logo-image" />
        </div>
        <div className="step3-content-container">
          <div className="step3-card">
            <img src={bloodIcon} alt="Blood Type Icon" className="step3-blood-icon" />
            <p className="step3-card-text">혈액형이 어떻게 되세요?</p>

            {/* Rh 값 선택 박스 */}
            <div className="step3-input-group">
              <select className="step3-input-box" value={rhValue} onChange={handleRhChange}>
                <option value="Rh+">Rh+</option>
                <option value="Rh-">Rh-</option>
              </select>

              {/* 혈액형 선택 박스 */}
              <select className="step3-input-box" value={bloodType} onChange={handleBloodTypeChange}>
                <option value="A">A</option>
                <option value="B">B</option>
                <option value="O">O</option>
                <option value="AB">AB</option>
              </select>
            </div>
          </div>
        </div>

        {/* 하단 단계 및 화살표 */}
        <div className="step3-footer">
          <img
            src={arrowIcon}
            alt="Previous Arrow"
            className="step3-prev-arrow"
            style={{ transform: 'rotate(180deg)' }}
            onClick={goToStep2} // 이전 페이지로 이동
          />
          <p className="step3-step-indicator">3 / 9</p>
          <img
            src={arrowIcon}
            alt="Next Arrow"
            className="step3-next-arrow"
            onClick={goToStep4} // 다음 페이지로 이동
          />
        </div>
      </div>
    </div>
  );
};

export default Step3;
