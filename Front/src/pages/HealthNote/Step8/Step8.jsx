import React, { useState } from 'react'; 
import { useNavigate } from 'react-router-dom';
import Select from 'react-select'; // react-select 사용
import './Step8.css';

import altariLogo from '../../../assets/altari-logo.svg';
import arrowIcon from '../../../assets/arrow.svg';
import alergeIcon from '../../../assets/alerge.svg'; 

const Step8 = () => {
  const navigate = useNavigate();

  // 대표적인 음식 알레르기를 일으키는 음식 목록
  const options = [
    { label: '복숭아', value: '복숭아' },
    { label: '땅콩', value: '땅콩' },
    { label: '갑각류', value: '갑각류' },
    { label: '계란', value: '계란' },
    { label: '우유', value: '우유' },
    { label: '밀가루', value: '밀가루' },
    { label: '콩', value: '콩' },
    { label: '호두', value: '호두' },
    { label: '생선', value: '생선' },
    { label: '메밀', value: '메밀' },
  ];

  const [selectedFoods, setSelectedFoods] = useState([]); // 선택된 음식들

  const handleFoodChange = (selectedOptions) => {
    setSelectedFoods(selectedOptions);
  };

  const goToStep7 = () => {
    navigate('/healthnote/step7'); 
  };

  const goToStep9 = () => {
    navigate('/healthnote/step9'); 
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
    <div className="step8-container">
      <div className="step8-inner-container">
        <div className="step8-logo">
          <img src={altariLogo} alt="Altari Logo" className="step8-logo-image" />
        </div>
        <div className="step8-content-container">
          <div className="step8-card">
            <img src={alergeIcon} alt="Alerge Icon" className="step8-alerge-icon" />
            <p className="step8-card-text">약물 외 알러지가 있으신가요?</p>

            {/* 셀렉트 박스 */}
            <div className="step8-select-container">
              <Select
                isMulti // 여러 개 선택 가능
                options={options} // 선택 가능한 음식 옵션들
                value={selectedFoods} // 선택된 음식들
                onChange={handleFoodChange} // 선택 시 상태 업데이트
                placeholder="음식 검색 및 선택"
                className="step8-select"
                styles={customStyles} // 커스터마이징된 스타일 적용
              />
            </div>
          </div>
        </div>

        {/* 하단 단계 및 화살표 */}
        <div className="step8-footer">
          <img 
            src={arrowIcon} 
            alt="Previous Arrow" 
            className="step8-prev-arrow" 
            style={{ transform: 'rotate(180deg)' }} 
            onClick={goToStep7} 
          />
          <p className="step8-step-indicator">8 / 9</p>
          <img 
            src={arrowIcon} 
            alt="Next Arrow" 
            className="step8-next-arrow" 
            onClick={goToStep9} 
          />
        </div>
      </div>
    </div>
  );
};

export default Step8;
