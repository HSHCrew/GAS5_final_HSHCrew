import React, { useState, useRef, useEffect } from 'react';
import Header from '../../../components/Header';
import './style.css';

const HealthNoteProfile = () => {
  return (
    <div className="health-profile-container">
      <Header title="건강 프로필" />
      <div className="health-profile-box">
        
        {/* 기본 정보 섹션 */}
        <div className="profile-section">
          <p className="profile-section-title">기본 정보</p>
          <EditableInfoItem label="키" type="number" unit="cm" step={0.1} scrollStep={1} decimal={1} defaultValue="170.0" />
          <EditableInfoItem label="몸무게" type="number" unit="kg" step={0.1} scrollStep={1} decimal={1} defaultValue="70.0" />
          <EditableInfoItem label="혈액형" type="blood" defaultValue="RH+ A형" />
          <EditableInfoItem label="흡연" type="select" options={["비흡연", "주 1회", "주 2회", "주 3회 이상"]} defaultValue="비흡연" />
          <EditableInfoItem label="음주" type="select" options={["비음주", "월 1회", "주 1회", "주 2회", "주 3회 이상"]} defaultValue="비음주" />
        </div>

        {/* 병력 정보 섹션 */}
        <div className="info-section">
          <p className="profile-section-title">병력 정보</p>
          <EditableInfoItem label="# 현재력" type="multiSelect" options={["고혈압", "고지혈증", "비만", "당뇨", "노년 백내장", "치매", "비염", "위염", "치주질환", "치핵", "탈모"]} />
          <EditableInfoItem label="# 과거력" type="multiSelect" options={["고혈압", "고지혈증", "비만", "당뇨", "노년 백내장", "치매", "비염", "위염", "치주질환", "치핵", "탈모"]} />
          <EditableInfoItem label="# 가족력" type="multiSelect" options={["고혈압", "고지혈증", "비만", "당뇨", "노년 백내장", "치매", "비염", "위염", "치주질환", "치핵", "탈모"]} />
        </div>

        {/* 알러지 정보 섹션 */}
        <div className="info-section">
          <p className="profile-section-title">알러지 정보</p>
          <EditableInfoItem label="# 약물 알러지" type="multiSelect" options={["페니실린", "아스피린", "설파제", "항생제", "인슐린", "조영제", "항암제", "마취제", "항생제 연고", "비스테로이드성 소염제"]} />
          <EditableInfoItem label="# 약물 외 알러지" type="multiSelect" options={["복숭아", "땅콩", "갑각류", "계란", "우유", "밀가루", "콩", "호두", "생선", "메밀"]} />
        </div>
      </div>
    </div>
  );
};

// 개별 정보 항목 컴포넌트 (수정 가능)
const EditableInfoItem = ({ label, type, unit, step = 1, scrollStep = 1, decimal, options, defaultValue }) => {
  const [value, setValue] = useState(defaultValue);
  const [isEditing, setIsEditing] = useState(false);
  const inputRef = useRef(null);
  const containerRef = useRef(null);

  // 외부 클릭 감지로 편집 모드 종료
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        containerRef.current &&
        !containerRef.current.contains(event.target)
      ) {
        setIsEditing(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  const handleDoubleClick = () => {
    setIsEditing(true);
  };

  const handleChange = (e) => {
    let newValue = e.target.value;

    if (type === "number" && decimal) {
      newValue = parseFloat(newValue).toFixed(decimal);
    }

    setValue(newValue);
  };

  const handleMultiSelectChange = (option) => {
    if (Array.isArray(value)) {
      if (value.includes(option)) {
        setValue(value.filter((item) => item !== option));
      } else {
        setValue([...value, option]);
      }
    } else {
      setValue([option]);
    }
  };

  return (
    <div className="info-item" onDoubleClick={handleDoubleClick} ref={containerRef}>
      <p className="info-label">{label}</p>
      {isEditing ? (
        type === "select" ? (
          <select
            value={value}
            onChange={handleChange}
            onBlur={() => setIsEditing(false)}
            className="info-input"
            autoFocus
          >
            {options.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        ) : type === "multiSelect" ? (
          <div className="multi-select-options">
            {options.map((option) => (
              <div key={option} className="multi-select-option">
                <input
                  type="checkbox"
                  checked={value && value.includes(option)}
                  onChange={() => handleMultiSelectChange(option)}
                />
                <label>{option}</label>
              </div>
            ))}
          </div>
        ) : type === "blood" ? (
          <div className="info-blood">
            <select
              value={value.split(" ")[0]}
              onChange={(e) => setValue(`${e.target.value} ${value.split(" ")[1]}`)}
              className="info-input"
              autoFocus
            >
              {["RH+", "RH-"].map((rh) => (
                <option key={rh} value={rh}>
                  {rh}
                </option>
              ))}
            </select>
            <select
              value={value.split(" ")[1]}
              onChange={(e) => setValue(`${value.split(" ")[0]} ${e.target.value}`)}
              className="info-input"
            >
              {["A형", "B형", "O형", "AB형"].map((type) => (
                <option key={type} value={type}>
                  {type}
                </option>
              ))}
            </select>
          </div>
        ) : (
          <input
            type="number"
            step={step}
            value={value}
            onChange={handleChange}
            onWheel={(e) => {
              const increment = e.deltaY < 0 ? scrollStep : -scrollStep;
              setValue((prevValue) => (parseFloat(prevValue) + increment).toFixed(decimal));
            }}
            className="info-input"
          />
        )
      ) : (
        <p className="info-value">
          {Array.isArray(value) ? value.join(", ") : value}
          {unit && <span className="unit"> {unit}</span>}
        </p>
      )}
    </div>
  );
};

export default HealthNoteProfile;
