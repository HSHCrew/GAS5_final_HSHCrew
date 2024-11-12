import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import apiRequest from '../../utils/apiRequest';
import './style.css';

import userLogo from '../../assets/user.svg';

function Setting() {
  const [profileName, setProfileName] = useState('');
  const username = localStorage.getItem('username') || sessionStorage.getItem('username');
  const accessToken = localStorage.getItem('token') || sessionStorage.getItem('token');

  useEffect(() => {
    if (username && accessToken) {
      fetchUserProfile();
    } else {
      console.error("Username 또는 Access Token이 없습니다. 로그인 후 다시 시도하세요.");
    }
  }, [username, accessToken]);

  const fetchUserProfile = async () => {
    try {
      const response = await apiRequest(`http://localhost:8080/altari/getInfo/userProfile/${username}`, {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${accessToken}`, // 엑세스 토큰 추가
        },
      });
      setProfileName(response.data.fullName || '');
    } catch (error) {
      console.error("Failed to fetch user profile:", error);
    }
  };

  return (
    <div className="setting-container">
      <div className="setting-box">
        <div className="setting-header">
          <p className="header-title">내정보</p>
        </div>

        <div className="profile-section">
          <img src={userLogo} alt="Profile" className="profile-image" />
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
