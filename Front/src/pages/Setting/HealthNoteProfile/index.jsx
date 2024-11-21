import React, { useState, useEffect, useRef } from 'react';
import Header from '../../../components/Header';
import apiClient from '../../../api/apiClient'; // Axios 인스턴스 가져오기
import './style.css';

const HealthNoteProfile = () => {
  const username = localStorage.getItem('username') || sessionStorage.getItem('username');
  const [profileData, setProfileData] = useState(null);

  // 옵션들 정의
  const diseaseOptions = [
    { id: 1, name: '고혈압' },
    { id: 2, name: '고지혈증' },
    { id: 3, name: '비만' },
    { id: 4, name: '당뇨' },
    { id: 5, name: '노년 백내장' },
    { id: 6, name: '치매' },
    { id: 7, name: '비염' },
    { id: 8, name: '위염' },
    { id: 9, name: '치주질환' },
    { id: 10, name: '치핵' },
    { id: 11, name: '탈모' },
  ];

  const drugAllergyOptions = [
    '페니실린',
    '아스피린',
    '설파제',
    '항생제',
    '인슐린',
    '조영제',
    '항암제',
    '마취제',
    '항생제 연고',
    '비스테로이드성 소염제',
  ];

  const foodAllergyOptions = [
    '복숭아',
    '땅콩',
    '갑각류',
    '계란',
    '우유',
    '밀가루',
    '콩',
    '호두',
    '생선',
    '메밀',
  ];

  // 컴포넌트 마운트 시 데이터 가져오기
  useEffect(() => {
    const fetchProfileData = async () => {
      try {
        const profileResponse = await apiClient.get(`/altari/getInfo/userProfile/${username}`);
        const diseaseResponse = await apiClient.get(`/altari/getInfo/userHealth/${username}`);

        const profile = profileResponse.data;
        const diseases = diseaseResponse.data;

        // 질병 데이터를 처리하여 ID와 이름을 매칭
        const processDiseaseArray = (diseaseArray) => {
          if (Array.isArray(diseaseArray)) {
            return diseaseArray
              .filter((d) => d && d.diseaseId !== undefined && d.diseaseId !== null)
              .map((d) => ({
                id: d.diseaseId.toString(),
                name: diseaseOptions.find((option) => option.id === d.diseaseId)?.name || 'Unknown',
              }));
          }
          return [];
        };

        setProfileData({
          height: profile.height,
          weight: profile.weight,
          bloodType: profile.bloodType,
          currentDiseases: processDiseaseArray(diseases.diseases),
          pastDiseases: processDiseaseArray(diseases.pastDiseases),
          familyDiseases: processDiseaseArray(diseases.familyDiseases),
          drugAllergies: diseases.allergyMedications || [],
          foodAllergies: diseases.foodAllergies || [],
        });
      } catch (error) {
        console.error('Failed to fetch profile data:', error);
      }
    };

    fetchProfileData();
  }, [username]);

  // 값이 변경될 때 상태 업데이트
  const handleValueChange = async (fieldName, newValue) => {
    setProfileData((prevData) => ({
      ...prevData,
      [fieldName]: newValue,
    }));

    // 필드 업데이트 API 호출
    try {
      let apiPath = '';
      let requestData = {};

      if (fieldName === 'height' || fieldName === 'weight' || fieldName === 'bloodType') {
        apiPath = `/altari/updateInfo/userProfile/${username}`;
        requestData = { [fieldName]: newValue };
      } else if (fieldName === 'currentDiseases' || fieldName === 'pastDiseases' || fieldName === 'familyDiseases') {
        apiPath = fieldName === 'currentDiseases'
          ? `/altari/updateInfo/userDisease/${username}`
          : fieldName === 'pastDiseases'
          ? `/altari/updateInfo/userPastDisease/${username}`
          : `/altari/updateInfo/userFamilyDisease/${username}`;
        requestData = { diseases: newValue.map((d) => Number(d.id)) };
      } else if (fieldName === 'drugAllergies' || fieldName === 'foodAllergies') {
        apiPath = `/altari/updateInfo/userAllergy/${username}`;
        requestData = fieldName === 'drugAllergies'
          ? { allergyMedications: newValue }
          : { foodAllergies: newValue };
      }

      await apiClient.put(apiPath, requestData);
    } catch (error) {
      console.error(`Failed to update ${fieldName}:`, error);
    }
  };

  if (!profileData) {
    return (
      <div className="health-loading-spinner">
      </div>
    );
  }

  return (
    <div className="health-profile-container">
      <Header title="건강 프로필" />
      <div className="health-profile-box">
        {/* 기본 정보 섹션 */}
        <div className="info-section">
          <p className="profile-section-title">기본 정보</p>
          <EditableInfoItem
            label="키"
            type="number"
            unit="cm"
            step={0.1}
            fieldName="height"
            value={profileData.height}
            onValueChange={handleValueChange}
          />
          <EditableInfoItem
            label="몸무게"
            type="number"
            unit="kg"
            step={0.1}
            fieldName="weight"
            value={profileData.weight}
            onValueChange={handleValueChange}
          />
          <EditableInfoItem
            label="혈액형"
            type="blood"
            fieldName="bloodType"
            value={profileData.bloodType}
            onValueChange={handleValueChange}
          />
        </div>

        {/* 병력 정보 섹션 */}
        <div className="info-section">
          <p className="profile-section-title">병력 정보</p>
          <EditableInfoItem
            label="# 현재력"
            type="diseaseSelect"
            options={diseaseOptions}
            fieldName="currentDiseases"
            value={profileData.currentDiseases}
            onValueChange={handleValueChange}
          />
          <EditableInfoItem
            label="# 과거력"
            type="diseaseSelect"
            options={diseaseOptions}
            fieldName="pastDiseases"
            value={profileData.pastDiseases}
            onValueChange={handleValueChange}
          />
          <EditableInfoItem
            label="# 가족력"
            type="diseaseSelect"
            options={diseaseOptions}
            fieldName="familyDiseases"
            value={profileData.familyDiseases}
            onValueChange={handleValueChange}
          />
        </div>

        {/* 알러지 정보 섹션 */}
        <div className="info-section">
          <p className="profile-section-title">알러지 정보</p>
          <EditableInfoItem
            label="# 약물 알러지"
            type="multiSelect"
            options={drugAllergyOptions}
            fieldName="drugAllergies"
            value={profileData.drugAllergies}
            onValueChange={handleValueChange}
          />
          <EditableInfoItem
            label="# 약물 외 알러지"
            type="multiSelect"
            options={foodAllergyOptions}
            fieldName="foodAllergies"
            value={profileData.foodAllergies}
            onValueChange={handleValueChange}
          />
        </div>
      </div>
    </div>
  );
};

// EditableInfoItem 컴포넌트
const EditableInfoItem = ({
  label,
  type,
  unit,
  step = 1,
  options,
  value: propValue,
  fieldName,
  onValueChange,
}) => {
  const [value, setValue] = useState(propValue || (type === 'diseaseSelect' || type === 'multiSelect' ? [] : ''));
  const [isEditing, setIsEditing] = useState(false);

  useEffect(() => {
    setValue(propValue || (type === 'diseaseSelect' || type === 'multiSelect' ? [] : ''));
  }, [propValue]);

  const handleEdit = () => setIsEditing(true);
  const handleBlur = () => {
    setIsEditing(false);
    onValueChange(fieldName, value);
  };

  const handleChange = (e) => {
    const newValue = type === 'number' ? parseFloat(e.target.value) : e.target.value;
    setValue(newValue);
  };

  return (
    <div className="info-item" onClick={handleEdit}>
      <p className="info-label">{label}</p>
      {isEditing ? (
        type === 'number' ? (
          <input
            type="number"
            className="info-input"
            step={step}
            value={value}
            onChange={handleChange}
            onBlur={handleBlur}
            autoFocus
          />
        ) : type === 'blood' ? (
          <div className="info-blood" onBlur={handleBlur}>
            <select
              value={value.split(' ')[0] || 'RH+'}
              onChange={(e) => setValue(`${e.target.value} ${value.split(' ')[1] || ''}`)}
            >
              <option value="RH+">RH+</option>
              <option value="RH-">RH-</option>
            </select>
            <select
              value={value.split(' ')[1] || 'A형'}
              onChange={(e) => setValue(`${value.split(' ')[0]} ${e.target.value}`)}
            >
              <option value="A형">A형</option>
              <option value="B형">B형</option>
              <option value="O형">O형</option>
              <option value="AB형">AB형</option>
            </select>
          </div>
        ) : type === 'diseaseSelect' || type === 'multiSelect' ? (
          <div className="multi-select-options">
            {options.map((option) => (
              <div key={option.id || option} className="multi-select-option">
                <input
                  type="checkbox"
                  checked={value.some((v) => (typeof v === 'string' ? v : v.id) === (option.id || option))}
                  onChange={() => {
                    const optionValue = option.id ? option : option.toString();
                    setValue(
                      value.some((v) => (typeof v === 'string' ? v : v.id) === (option.id || option))
                        ? value.filter((v) => (typeof v === 'string' ? v : v.id) !== (option.id || option))
                        : [...value, optionValue]
                    );
                  }}
                />
                <label>{option.name || option}</label>
              </div>
            ))}
          </div>
        ) : (
          <input
            type="text"
            className="info-input"
            value={value}
            onChange={handleChange}
            onBlur={handleBlur}
            autoFocus
          />
        )
      ) : (
        <p className="info-value">
          {Array.isArray(value)
            ? value.map((v) => (typeof v === 'string' ? v : v.name)).join(', ')
            : value}
          {unit && <span className="unit"> {unit}</span>}
        </p>
      )}
    </div>
  );
};

export default HealthNoteProfile;
