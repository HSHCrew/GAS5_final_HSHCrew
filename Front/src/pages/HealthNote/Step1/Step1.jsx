import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom'; // useNavigate 훅을 가져옵니다.
import './Step1.css';

import altariLogo from '../../../assets/altari-logo.svg';
import heightIcon from '../../../assets/height.svg';
import arrowIcon from '../../../assets/arrow.svg';

const Step1 = () => {
  const [selectedHeightInteger, setSelectedHeightInteger] = useState(170); // 자연수 부분
  const [selectedHeightDecimal, setSelectedHeightDecimal] = useState(0); // 소수점 부분
  const navigate = useNavigate(); // useNavigate를 사용하여 페이지 이동을 처리합니다.

  // 자연수 부분 핸들러
  const handleHeightIntegerChange = (event) => {
    setSelectedHeightInteger(parseInt(event.target.value, 10));
  };

  // 소수점 부분 핸들러
  const handleHeightDecimalChange = (event) => {
    setSelectedHeightDecimal(parseInt(event.target.value, 10));
  };

  // 이전 화살표 클릭 시 Choice 페이지로 이동
  const goToChoice = () => {
    navigate('/healthnote/choice'); // Choice 페이지로 이동
  };

  // 다음 화살표 클릭 시 Step2 페이지로 이동
  const goToStep2 = () => {
    navigate('/healthnote/step2'); // Step2 페이지로 이동
  };

  return (
    <div className="step1-container">
      <div className="step1-inner-container">
        <div className="step1-logo">
          <img src={altariLogo} alt="Altari Logo" className="step1-logo-image" />
        </div>
        <div className="step1-content-container">
          <div className="step1-card">
            <img src={heightIcon} alt="Height Icon" className="step1-height-icon" />
            <p className="step1-card-text">키가 몇 cm 인가요?</p>

            {/* 입력창 */}
            <div className="step1-input-group">
              <select
                className="step1-input-box whole-box"
                aria-label="Select whole number part of height"
                onChange={handleHeightIntegerChange}
              >
                {Array.from({ length: 51 }, (_, i) => (
                  <option key={i} value={150 + i}>
                    {150 + i}
                  </option>
                ))}
              </select>

              <span className="step1-dot-text">.</span> {/* 소수점 고정 텍스트 */}

              <select
                className="step1-input-box decimal-box"
                aria-label="Select decimal part of height"
                onChange={handleHeightDecimalChange}
              >
                {Array.from({ length: 10 }, (_, i) => (
                  <option key={i} value={i}>
                    {i}
                  </option>
                ))}
              </select>

              <p className="step1-unit-text">cm</p>
            </div>

          </div>
        </div>

        {/* 하단 단계 및 화살표 */}
        <div className="step1-footer">
          <img
            src={arrowIcon}
            alt="Previous Arrow"
            className="step1-prev-arrow"
            style={{ transform: 'rotate(180deg)' }}
            onClick={goToChoice} // 왼쪽 화살표 클릭 시 Choice 페이지로 이동
          />
          <p className="step1-step-indicator">1 / 9</p>
          <img
            src={arrowIcon}
            alt="Next Arrow"
            className="step1-next-arrow"
            onClick={goToStep2} // 오른쪽 화살표 클릭 시 Step2 페이지로 이동
          />
        </div>
      </div>
    </div>
  );
};

export default Step1;
