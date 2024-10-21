import React, { useState } from 'react'; 
import { useNavigate } from 'react-router-dom';
import Select from 'react-select'; // react-select 추가
import './Step5.css';

import altariLogo from '../../../assets/altari-logo.svg';
import arrowIcon from '../../../assets/arrow.svg';
import historyIcon from '../../../assets/decease.svg'; 

const Step5 = () => {
  const navigate = useNavigate();

  const goToStep4 = () => {
    navigate('/healthnote/step4');
  };

  const goToStep6 = () => {
    navigate('/healthnote/step6');
  };

  // 태그 옵션 데이터
  const options = [
    { label: '고혈압', value: '고혈압' },
    { label: '고지혈증', value: '고지혈증' },
    { label: '비만', value: '비만' },
    { label: '당뇨', value: '당뇨' },
    { label: '노년 백내장', value: '노년 백내장' },
    { label: '치매', value: '치매' },
    { label: '비염', value: '비염' },
    { label: '위염', value: '위염' },
    { label: '치주질환', value: '치주질환' },
    { label: '치핵', value: '치핵' },
    { label: '탈모', value: '탈모' },
  ];

  const [selectedTags, setSelectedTags] = useState([]); // 선택된 태그

  const handleTagChange = (selectedOptions) => {
    setSelectedTags(selectedOptions);
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
      backgroundColor: '#F7FAFC',
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
    <div className="step5-container">
      <div className="step5-inner-container">
        <div className="step5-logo">
          <img src={altariLogo} alt="Altari Logo" className="step5-logo-image" />
        </div>
        <div className="step5-content-container">
          <div className="step5-card">
            <img src={historyIcon} alt="History Icon" className="step5-history-icon" />
            <p className="step5-card-text">과거력이 있으신가요?</p>

            {/* 셀렉트 박스 */}
            <div className="step5-select-container">
              <Select
                isMulti // 여러 개 선택 가능
                options={options} // 선택 가능한 옵션들
                value={selectedTags} // 선택된 태그들
                onChange={handleTagChange} // 선택 시 상태 업데이트
                placeholder="태그 검색 및 선택"
                className="step5-select"
                styles={customStyles} // 커스터마이징된 스타일 적용
              />
            </div>
          </div>
        </div>

        {/* 하단 단계 및 화살표 */}
        <div className="step5-footer">
          <img 
            src={arrowIcon} 
            alt="Previous Arrow" 
            className="step5-prev-arrow" 
            style={{ transform: 'rotate(180deg)' }} 
            onClick={goToStep4}
          />
          <p className="step5-step-indicator">5 / 9</p>
          <img 
            src={arrowIcon} 
            alt="Next Arrow" 
            className="step5-next-arrow" 
            onClick={goToStep6}
          />
        </div>
      </div>
    </div>
  );
};

export default Step5;
