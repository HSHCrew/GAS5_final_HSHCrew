import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import apiClient from '../../api/apiClient'; // apiClient로 변경
import './style.css';

import userLogo from '../../assets/user.svg';

function Setting() {
  const [profileName, setProfileName] = useState('');
  const [profileImage, setProfileImage] = useState(userLogo); // 기본 이미지를 초기값으로 설정
  const username = localStorage.getItem('username') || sessionStorage.getItem('username');

  useEffect(() => {
    if (username) {
      fetchUserProfile();
    } else {
      console.error('Username이 없습니다. 로그인 후 다시 시도하세요.');
    }
  }, [username]);

  const fetchUserProfile = async () => {
    try {
      // 프로필 데이터 요청
      const response = await apiClient.get(`/altari/getInfo/userProfile/${username}`);
      setProfileName(response.data.fullName || '');
      
      // 프로필 이미지 설정
      if (response.data.profileImage) {
        setProfileImage(response.data.profileImage);
      }
    } catch (error) {
      console.error('Failed to fetch user profile:', error);
    }
  };

  return (
    <div className="setting-container">
      <div className="setting-box">
        <div className="setting-header">
          <p className="header-title">내정보</p>
        </div>

        <div className="profile-section">
          {/* 프로필 이미지 */}
          <img src={profileImage} alt="Profile" className="profile-image" />
          <p className="profile-name">{profileName}</p>
          <Link to="/userInfo" className="profile-edit">
            개인정보 수정
          </Link>
        </div>

        <div className="menu-section">
          <MenuItem label="건강 프로필" path="/healthNoteProfile" />
          <MenuItem label="복약알림 설정" path="/setAlarm" />
          <MenuItem label="약관 보기" path="/termsPage" />
          <MenuItem label="버전 정보" version="v1.00.0" />
        </div>
      </div>
    </div>
  );
}

function MenuItem({ label, path, version }) {
  return (
    <Link to={path || '#'} className="menu-item">
      <p className="menu-label">{label}</p>
      {version && <p className="menu-version">{version}</p>}
    </Link>
  );
}

export default Setting;
