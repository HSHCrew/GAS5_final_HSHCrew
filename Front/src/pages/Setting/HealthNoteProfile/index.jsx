import React, { useState, useEffect, useRef } from 'react';
import Header from '../../../components/Header';
import apiRequest from '../../../utils/apiRequest';
import './style.css';

const HealthNoteProfile = () => {
  const username = localStorage.getItem('username') || sessionStorage.getItem('username');
  const [profileData, setProfileData] = useState({
    height: "170.0",
    weight: "70.0",
    bloodType: "RH+ A형",
    currentDiseases: [],
    pastDiseases: [],
    familyDiseases: [],
    drugAllergies: [],
    foodAllergies: [],
  });

  useEffect(() => {
    const fetchProfileData = async () => {
      try {
        const response = await apiRequest(`http://localhost:8080/altari/getInfo/userProfile/${username}`, {
          method: 'GET',
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token') || sessionStorage.getItem('token')}`,
          },
        });
        const data = response.data;
        setProfileData({
          height: data.height ?? "170.0",
          weight: data.weight ?? "70.0",
          bloodType: data.bloodType ?? "RH+ A형",
          currentDiseases: data.currentDiseases ?? [],
          pastDiseases: data.pastDiseases ?? [],
          familyDiseases: data.familyDiseases ?? [],
          drugAllergies: data.drugAllergies ?? [],
          foodAllergies: data.foodAllergies ?? [],
        });
      } catch (error) {
        console.error("Failed to fetch profile data:", error);
      }
    };

    fetchProfileData();
  }, [username]);

  return (
    <div className="health-profile-container">
      <Header title="건강 프로필" />
      <div className="health-profile-box">
        
        {/* 기본 정보 섹션 */}
        <div className="profile-section">
          <p className="profile-section-title">기본 정보</p>
          <EditableInfoItem label="키" type="number" unit="cm" step={0.1} scrollStep={1} decimal={1} fieldName="height" username={username} />
          <EditableInfoItem label="몸무게" type="number" unit="kg" step={0.1} scrollStep={1} decimal={1} fieldName="weight" username={username} />
          <EditableInfoItem label="혈액형" type="blood" fieldName="bloodType" username={username} />
        </div>

        {/* 병력 정보 섹션 */}
        <div className="info-section">
          <p className="profile-section-title">병력 정보</p>
          <EditableInfoItem label="# 현재력" type="multiSelect" options={["고혈압", "고지혈증", "비만", "당뇨", "노년 백내장", "치매", "비염", "위염", "치주질환", "치핵", "탈모"]} fieldName="currentDiseases" username={username} />
          <EditableInfoItem label="# 과거력" type="multiSelect" options={["고혈압", "고지혈증", "비만", "당뇨", "노년 백내장", "치매", "비염", "위염", "치주질환", "치핵", "탈모"]} fieldName="pastDiseases" username={username} />
          <EditableInfoItem label="# 가족력" type="multiSelect" options={["고혈압", "고지혈증", "비만", "당뇨", "노년 백내장", "치매", "비염", "위염", "치주질환", "치핵", "탈모"]} fieldName="familyDiseases" username={username} />
        </div>

        {/* 알러지 정보 섹션 */}
        <div className="info-section">
          <p className="profile-section-title">알러지 정보</p>
          <EditableInfoItem label="# 약물 알러지" type="multiSelect" options={["페니실린", "아스피린", "설파제", "항생제", "인슐린", "조영제", "항암제", "마취제", "항생제 연고", "비스테로이드성 소염제"]} fieldName="drugAllergies" username={username} />
          <EditableInfoItem label="# 약물 외 알러지" type="multiSelect" options={["복숭아", "땅콩", "갑각류", "계란", "우유", "밀가루", "콩", "호두", "생선", "메밀"]} fieldName="foodAllergies" username={username} />
        </div>
      </div>
    </div>
  );
};

const EditableInfoItem = ({ label, type, unit, step = 1, scrollStep = 1, decimal, options, defaultValue, fieldName, username }) => {
  const [value, setValue] = useState(defaultValue);
  const [isEditing, setIsEditing] = useState(false);
  const inputRef = useRef(null);
  const containerRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        handleBlur();
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
      newValue = parseFloat(newValue);
      if (isNaN(newValue)) {
        newValue = parseFloat(defaultValue).toFixed(decimal);
      } else {
        newValue = newValue.toFixed(decimal);
      }
    }

    setValue(newValue);
  };

  const handleBlur = async () => {
    setIsEditing(false);

    // 값이 변경되지 않았으면 API 요청을 보내지 않음
    if (value === defaultValue) return;

    try {
      // 변경된 필드만 포함하여 requestData 생성
      const requestData = {
        [fieldName]: value
      };

      await apiRequest(`http://localhost:8080/altari/updateInfo/userProfile/${username}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('token') || sessionStorage.getItem('token')}`,
        },
        data: requestData,
      });
      console.log("저장 성공:", requestData);
    } catch (error) {
      console.error("Failed to update profile field:", error);
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
            onBlur={handleBlur}
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
                  checked={value.includes(option)}
                  onChange={() => handleMultiSelectChange(option)}
                />
                <label>{option}</label>
              </div>
            ))}
          </div>
        ) : type === "blood" ? (
          <div className="info-blood">
            <select
              value={value ? value.split(" ")[0] : "RH+"}
              onChange={(e) => setValue(`${e.target.value} ${value.split(" ")[1] || "A형"}`)}
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
              value={value ? value.split(" ")[1] : "A형"}
              onChange={(e) => setValue(`${value.split(" ")[0] || "RH+"} ${e.target.value}`)}
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
            onBlur={handleBlur}
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
