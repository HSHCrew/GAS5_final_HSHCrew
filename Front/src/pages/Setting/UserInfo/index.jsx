import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../../../api/apiClient'; // apiRequest 대신 apiClient를 사용
import Header from '../../../components/Header';
import './style.css';
import userLogo from '../../../assets/user.svg';

const UserInfo = () => {
  const [profile, setProfile] = useState({
    fullName: '',
    dateOfBirth: '',
    phoneNumber: '',
    role: '',
  });
  const [profileImage, setProfileImage] = useState(userLogo); // 기본 이미지를 초기값으로 설정
  const [selectedImage, setSelectedImage] = useState(null); 
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

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
      // apiClient 사용
      const response = await apiClient.get(`/altari/getInfo/userProfile/${username}`);

      const dateOfBirthArray = response.data.dateOfBirth;
      const formattedDateOfBirth = Array.isArray(dateOfBirthArray)
        ? formatDateArray(dateOfBirthArray)
        : response.data.dateOfBirth;

      setProfile({
        fullName: response.data.fullName,
        dateOfBirth: formattedDateOfBirth,
        phoneNumber: formatPhoneNumber(response.data.phoneNumber),
        role: response.data.role || "USER",
      });

      // 프로필 이미지 설정
      if (response.data.profileImage) {
        setProfileImage(response.data.profileImage);
      }
      
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
        fullName: updatedProfile.fullName,
        dateOfBirth: updatedProfile.dateOfBirth,
        phoneNumber: updatedProfile.phoneNumber,
      };

      // apiClient 사용
      await apiClient.put(`/altari/updateInfo/userProfile/${username}`, dataToSend);
    } catch (error) {
      if (error.response) {
        console.error("Server responded with error:", error.response.data);
      } else {
        console.error("Failed to reach server:", error.message);
      }
    }
  };


  const handleProfileUpdate = (field, value) => {
    const updatedProfile = { ...profile, [field]: value };
    setProfile(updatedProfile);
    updateUserProfile(updatedProfile); // 블러 시에만 API 호출
    
  };
  
  const handleImageChange = (event) => {
    const file = event.target.files[0];
    if (file) {
      setSelectedImage(file);
      uploadImage(file); // 이미지 변경 시 업로드
    }
  };

  const uploadImage = async (file) => {
    const formData = new FormData();
    formData.append('profileImage', file);
    formData.append('username', username);

    try {
      const response = await apiClient.put(`/altari/updateInfo/userProfile/${username}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });

      if (response.data.profileImageUrl) {
        setProfileImage(response.data.profileImageUrl); // 서버에서 반환된 이미지 URL로 업데이트
      }
    } catch (error) {
      console.error("Failed to upload profile image:", error);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    sessionStorage.removeItem('token');
    sessionStorage.removeItem('username');

    alert("로그아웃되었습니다.");
    navigate('/'); // 로그아웃 후 로그인 페이지로 이동
  };

  const handleDeleteAccount = async () => {
    try {
      // apiClient 사용
      await apiClient.delete(`/altari/delete/${username}`);
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
          <img src={profileImage} alt="Profile" className="profile-image" />
          <label htmlFor="file-upload" className="profile-edit">
            사진 수정
          </label>
          <input
            id="file-upload"
            type="file"
            accept="image/*"
            onChange={handleImageChange}
            style={{ display: 'none' }}
          />
        </div>

        <div className="info-section">
          <EditableInfoItem
            label="이름"
            value={profile.fullName}
            onChange={(newValue) => handleProfileChange('fullName', newValue)}
            onBlur={() => handleProfileUpdate('fullName', profile.fullName)}
          />
          <EditableInfoItem
            label="생년월일"
            value={profile.dateOfBirth}
            onChange={(newValue) => handleProfileChange('dateOfBirth', newValue)}
            onBlur={() => handleProfileUpdate('dateOfBirth', profile.dateOfBirth)}
          />
          <EditableInfoItem
            label="전화번호"
            value={profile.phoneNumber}
            onChange={(newValue) => handleProfileChange('phoneNumber', formatPhoneNumber(newValue))}
            onBlur={() => handleProfileUpdate('phoneNumber', profile.phoneNumber)}
            isPhoneNumber
          />
          <InfoItem label="연동된 계정" value={profile.role} />
        </div>

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

// 전화번호 포맷팅 함수
const formatPhoneNumber = (number) => {
  if (!number) return '';

  // +82로 시작하고 다음 자리가 '1'인 경우, '010' 형식으로 변환
  if (number.startsWith('+82')) {
    number = '0' + number.slice(3);
  }

  // 숫자만 남긴 후, 휴대폰 번호 형식(010-xxxx-xxxx)에 맞게 변환
  number = number.replace(/\D/g, ''); // 숫자 이외의 문자는 제거

  // 전화번호가 10자리 또는 11자리일 때만 형식 적용
  if (number.length === 10) {
    return number.replace(/(\d{2})(\d{4})(\d{4})/, '$1-$2-$3');
  } else if (number.length === 11) {
    return number.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
  } else {
    return number;
  }
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