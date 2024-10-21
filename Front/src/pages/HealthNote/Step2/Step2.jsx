import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Step2.css';

import altariLogo from '../../../assets/altari-logo.svg'; 
import scaleIcon from '../../../assets/weight.svg'; 
import arrowIcon from '../../../assets/arrow.svg';


const Step2 = () => {
  const [selectedWeightInteger, setSelectedWeightInteger] = useState(50); // 자연수 부분
  const [selectedWeightDecimal, setSelectedWeightDecimal] = useState(0); // 소수점 부분
  const navigate = useNavigate();

  // 자연수 부분 핸들러
  const handleWeightIntegerChange = (event) => {
    setSelectedWeightInteger(parseInt(event.target.value, 10));
  };

  // 소수점 부분 핸들러
  const handleWeightDecimalChange = (event) => {
    setSelectedWeightDecimal(parseInt(event.target.value, 10));
  };

  // 이전 화살표 클릭 시 Choice 페이지로 이동
  const goToStep1 = () => {
    navigate('/healthnote/step1'); // Choice 페이지로 이동
  };

  // 다음 화살표 클릭 시 Step2 페이지로 이동
  const goToStep3 = () => {
    navigate('/healthnote/step3'); // Step2 페이지로 이동
  };

  return (
    <div className="step2-container">
      <div className="step2-inner-container">
        <div className="step2-logo">
          <img src={altariLogo} alt="Altari Logo" className="step2-logo-image" />
        </div>

        <div className="step2-content-container">
          <div className="step2-card">
            <img src={scaleIcon} alt="Scale Icon" className="step2-scale-icon" />
            <p className="step2-card-text">몸무게가 몇 kg 인가요?</p>

            {/* 입력창 */}
            <div className="step2-input-group">
              {/* 자연수 부분 */}
              <select 
                className="step2-input-box whole-box" 
                aria-label="Select whole number part of weight" 
                value={selectedWeightInteger} 
                onChange={handleWeightIntegerChange}
              >
                {Array.from({ length: 101 }, (_, i) => (
                  <option key={i} value={30 + i}>
                    {30 + i}
                  </option>
                ))}
              </select>

              <span className="step2-dot-text">.</span> {/* 소수점 고정 텍스트 */}

              {/* 소수점 부분 */}
              <select 
                className="step2-input-box decimal-box" 
                aria-label="Select decimal part of weight" 
                value={selectedWeightDecimal} 
                onChange={handleWeightDecimalChange}
              >
                {Array.from({ length: 10 }, (_, i) => (
                  <option key={i} value={i}>
                    {i}
                  </option>
                ))}
              </select>

              <p className="step2-unit-text">kg</p>
            </div>
          </div>
        </div>

        {/* 하단 단계 및 화살표 */}
        <div className="step2-footer">
          {/* 왼쪽 화살표: Choice 페이지로 이동 */}
          <img
            src={arrowIcon}
            alt="Previous Arrow"
            className="step2-prev-arrow"
            style={{ transform: 'rotate(180deg)' }}
            onClick={goToStep1}
          />
          <p className="step2-step-indicator">2 / 9</p>

          {/* 오른쪽 화살표: Step3 페이지로 이동 */}
          <img
            src={arrowIcon}
            alt="Next Arrow"
            className="step2-next-arrow"
            onClick={goToStep3} // Step3 페이지로 이동
          />
        </div>
      </div>
    </div>
  );
};

export default Step2;
