import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Select from 'react-select'; // react-select 사용
import './Step7.css';

import altariLogo from '../../../assets/altari-logo.svg';
import arrowIcon from '../../../assets/arrow.svg';
import medicineIcon from '../../../assets/medicine.svg';

const Step7 = () => {
  const navigate = useNavigate();

  // 대표적인 약물 알레르기를 일으키는 약물 목록
  const options = [
    { label: '페니실린', value: '페니실린' },
    { label: '아스피린', value: '아스피린' },
    { label: '설파제', value: '설파제' },
    { label: '항생제', value: '항생제' },
    { label: '비스테로이드성 소염제', value: '비스테로이드성 소염제' },
    { label: '인슐린', value: '인슐린' },
    { label: '조영제', value: '조영제' },
    { label: '항암제', value: '항암제' },
    { label: '마취제', value: '마취제' },
    { label: '항생제 연고', value: '항생제 연고' },
  ];

  const [selectedDrugs, setSelectedDrugs] = useState([]); // 선택된 약물

  const handleDrugChange = (selectedOptions) => {
    setSelectedDrugs(selectedOptions);
  };

  const goToStep6 = () => {
    navigate('/healthnote/step6');
  };

  const goToStep8 = () => {
    navigate('/healthnote/step8');
  };

  // react-select 커스터마이징 스타일
  const customStyles = {
    control: (provided, state) => ({
      ...provided,
      backgroundColor: '#f7fafc',
      border: '1px solid #529174',
      borderRadius: '12px',
      padding: '8px',
      boxShadow: state.isFocused ? '0 0 0 2px rgba(82, 145, 116, 0.5)' : 'none',
      '&:hover': {
        borderColor: '#529174',
      },
    }),
    multiValue: (provided) => ({
      ...provided,
      backgroundColor: '#e6f4ea',
      color: '#0f7445',
    }),
    multiValueLabel: (provided) => ({
      ...provided,
      color: '#0f7445',
    }),
    multiValueRemove: (provided) => ({
      ...provided,
      color: '#0f7445',
      ':hover': {
        backgroundColor: '#0f7445',
        color: 'white',
      },
    }),
    placeholder: (provided) => ({
      ...provided,
      color: '#999999',
    }),
    dropdownIndicator: (provided) => ({
      ...provided,
      color: '#0f7445',
    }),
  };

  return (
    <div className="step7-container">
      <div className="step7-inner-container">
        <div className="step7-logo">
          <img src={altariLogo} alt="Altari Logo" className="step7-logo-image" />
        </div>
        <div className="step7-content-container">
          <div className="step7-card">
            <img src={medicineIcon} alt="Medicine Icon" className="step7-medicine-icon" />
            <p className="step7-card-text">약물 알러지가 있으신가요?</p>

            {/* 셀렉트 박스 */}
            <div className="step7-select-container">
              <Select
                isMulti // 여러 개 선택 가능
                options={options} // 선택 가능한 약물 옵션들
                value={selectedDrugs} // 선택된 약물들
                onChange={handleDrugChange} // 선택 시 상태 업데이트
                placeholder="약물 검색 및 선택"
                className="step7-select"
                styles={customStyles} // 커스터마이징된 스타일 적용
              />
            </div>
          </div>
        </div>

        {/* 하단 단계 및 화살표 */}
        <div className="step7-footer">
          <img
            src={arrowIcon}
            alt="Previous Arrow"
            className="step7-prev-arrow"
            style={{ transform: 'rotate(180deg)' }}
            onClick={goToStep6}
          />
          <p className="step7-step-indicator">7 / 9</p>
          <img
            src={arrowIcon}
            alt="Next Arrow"
            className="step7-next-arrow"
            onClick={goToStep8}
          />
        </div>
      </div>
    </div>
  );
};

export default Step7;
