import React from 'react';
import Select from 'react-select';

import medicineIcon from '../../../assets/medicine.svg';
import './Step7.css';

// react-select 커스터마이징 스타일
const customStyles = {
  control: (provided, state) => ({
    ...provided,
    backgroundColor: '#f7fafc',
    border: '1px solid #529174',
    borderRadius: '12px',
    padding: '8px',
    boxShadow: state.isFocused
        ? '0 0 0 2px rgba(82, 145, 116, 0.5)'
        : 'none',
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

const Step7 = ({ drugsAllergy, updateDrugsAllergy }) => {
  // 대표적인 약물 알레르기를 일으키는 약물 목록
  const options = [
    { label: '페니실린', value: '페니실린' },
    { label: '아스피린', value: '아스피린' },
    { label: '설파제', value: '설파제' },
    { label: '항생제', value: '항생제' },
    { label: '비스테로이드성 소염제', value: '비스테로이드성  소염제' },
    { label: '인슐린', value: '인슐린' },
    { label: '조영제', value: '조영제' },
    { label: '항암제', value: '항암제' },
    { label: '마취제', value: '마취제' },
    { label: '항생제 연고', value: '항생제 연고' },
  ];

  const handleDrugChange = (selectedOptions) => {
    updateDrugsAllergy(selectedOptions);
    console.log(selectedOptions);
  };

  return (
      <>
        <img
            src={medicineIcon}
            alt="Medicine Icon"
            className="step7-medicine-icon"
        />
        <p className="step7-card-text">약물 알러지가 있으신가요?</p>

        {/* 셀렉트 박스 */}
        <div className="step7-select-container">
          <Select
              isMulti // 여러 개 선택 가능
              options={options} // 선택 가능한 약물 옵션들
              value={drugsAllergy} // 선택된 약물들
              onChange={handleDrugChange} // 선택 시 상태 업데이트
              placeholder="약물 검색 및 선택"
              className="step7-select"
              styles={customStyles} // 커스터마이징된 스타일 적용
          />
        </div>
      </>
  );
};

export default Step7;
