import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiRequest from '../../../utils/apiRequest'; // apiRequest로 교체
import Header from '../../../components/Header';
import './style.css';
import userLogo from '../../../assets/user.svg';

const UserInfo = () => {
  const [profile, setProfile] = useState({
    full_name: '',
    date_of_birth: '',
    phone_number: '',
    role: '',
  });
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  // username을 localStorage 또는 sessionStorage에서 가져옴
  const username = localStorage.getItem('username') || sessionStorage.getItem('username');

  useEffect(() => {
    if (username) {
      fetchUserProfile(username);
    } else {
      console.error("Username이 없습니다. 로그인 후 다시 시도하세요.");
    }
  }, [username]);

  const fetchUserProfile = async (username) => {
    try {
      const response = await apiRequest(`/api/v1/get-userProfile/${username}`);

      const dateOfBirthArray = response.data.date_of_birth;
      const formattedDateOfBirth = Array.isArray(dateOfBirthArray) 
        ? formatDateArray(dateOfBirthArray) 
        : response.data.date_of_birth;

      setProfile({
        full_name: response.data.full_name,
        date_of_birth: formattedDateOfBirth,
        phone_number: formatPhoneNumber(response.data.phone_number),
        role: response.data.role || "USER",
      });
      setLoading(false);
    } catch (error) {
      console.error("Failed to fetch user profile:", error);
      setLoading(false);
    }
  };

  const updateUserProfile = async (updatedProfile) => {
    try {
      const dataToSend = {
        username: username,
        role: updatedProfile.role || "USER",
        full_name: updatedProfile.full_name,
        date_of_birth: updatedProfile.date_of_birth,
        phone_number: updatedProfile.phone_number,
      };

      await apiRequest(`/api/v1/update-userProfile/${username}`, {
        method: 'PUT',
        data: dataToSend,
      });
    } catch (error) {
      if (error.response) {
        console.error("Server responded with error:", error.response.data);
      } else {
        console.error("Failed to reach server:", error.message);
      }
    }
  };

  const handleProfileChange = (field, value) => {
    const updatedProfile = { ...profile, [field]: value };
    setProfile(updatedProfile);
  };

  const handleProfileUpdate = (field, value) => {
    const updatedProfile = { ...profile, [field]: value };
    setProfile(updatedProfile);
    updateUserProfile(updatedProfile); // 블러 시에만 API 호출
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('username');

    alert("로그아웃되었습니다.")
    navigate('/'); // 로그아웃 후 로그인 페이지로 이동
  };

  const handleDeleteAccount = async () => {
    try {
      await apiRequest(`/api/v1/delete-userProfile/${username}`, { method: 'DELETE' });
      handleLogout(); // 계정 삭제 후 로그아웃 처리
    } catch (error) {
      console.error("Failed to delete account:", error);
    }
  };

  if (loading) return <p>Loading...</p>;

  return (
    <div className="userinfo-container">
      <Header title="내정보" />
      <div className="userinfo-box">
        <div className="profile-section">
          <img src={userLogo} alt="Profile" className="profile-image" />
          <p className="profile-name">{profile.full_name}</p>
          <button className="profile-edit">사진 수정</button>
        </div>

        <div className="info-section">
          <EditableInfoItem
            label="이름"
            value={profile.full_name}
            onChange={(newValue) => handleProfileChange('full_name', newValue)}
            onBlur={() => handleProfileUpdate('full_name', profile.full_name)}
          />
          <EditableInfoItem
            label="생년월일"
            value={profile.date_of_birth}
            onChange={(newValue) => handleProfileChange('date_of_birth', newValue)}
            onBlur={() => handleProfileUpdate('date_of_birth', profile.date_of_birth)}
          />
          <EditableInfoItem
            label="전화번호"
            value={profile.phone_number}
            onChange={(newValue) => handleProfileChange('phone_number', formatPhoneNumber(newValue))}
            onBlur={() => handleProfileUpdate('phone_number', profile.phone_number)}
            isPhoneNumber
          />
          <InfoItem label="연동된 계정" value={profile.role} />
        </div>

        {/* 회원탈퇴 및 로그아웃 버튼 */}
        <div className="actions-section">
          <button className="delete-account-button" onClick={handleDeleteAccount}>회원탈퇴</button>
          <button className="logout-button" onClick={handleLogout}>로그아웃</button>
        </div>
      </div>
    </div>
  );
};

// 개별 정보 항목 컴포넌트 (수정 가능)
const EditableInfoItem = ({ label, value, onChange, onBlur, isPhoneNumber }) => {
  const [isEditing, setIsEditing] = useState(false);
  const handleDoubleClick = () => setIsEditing(true);

  const handleChange = (e) => {
    const inputValue = e.target.value;
    onChange(isPhoneNumber ? unformatPhoneNumber(inputValue) : inputValue);
  };

  const handleBlur = () => {
    setIsEditing(false);
    onBlur(); // 블러 시 API 호출을 위해 onBlur 호출
  };

  return (
    <div className="info-item" onDoubleClick={handleDoubleClick}>
      <p className="info-label">{label}</p>
      {isEditing ? (
        <input
          type="text"
          value={isPhoneNumber ? unformatPhoneNumber(value) : value}
          onChange={handleChange}
          onBlur={handleBlur}
          className="info-input"
          autoFocus
        />
      ) : (
        <p className="info-value">{value}</p>
      )}
    </div>
  );
};

// 포맷팅 함수들
const formatPhoneNumber = (number) => {
  if (!number) return '';
  return number.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
};

const unformatPhoneNumber = (formattedNumber) => {
  return formattedNumber.replace(/-/g, '');
};

const formatDateArray = (dateArray) => {
  if (!Array.isArray(dateArray) || dateArray.length !== 3) return '';
  const [year, month, day] = dateArray;
  return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
};

// 단순 정보 항목 (수정 불가)
const InfoItem = ({ label, value }) => (
  <div className="info-item">
    <p className="info-label">{label}</p>
    <p className="info-value">{value}</p>
  </div>
);

export default UserInfo;
