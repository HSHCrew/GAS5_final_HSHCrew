import React from 'react';
import { Link } from 'react-router-dom';
import './style.css';

import userLogo from '../../assets/user.svg';

function Setting() {
  return (
    <div className="setting-container">
      <div className="setting-box">
        <div className="setting-header">
          <p className="header-title">내정보</p>
        </div>

        <div className="profile-section">
          <img src={userLogo} alt="Profile" className="profile-image" />
          <p className="profile-name">홍길동</p>
          <Link to="/userInfo" className="profile-edit">
            개인정보 수정
          </Link>
        </div>

        <div className="menu-section">
          <MenuItem label="건강 프로필" path="/healthNoteProfile" />
          {/* <MenuItem label="가족 프로필 조회" path="/setting/familyprofile" /> */}
          {/* <MenuItem label="앱 설정" path="/setting/appsettings" /> */}
          <MenuItem label="복약알림 설정" path="/setAlarm" />
          {/* <MenuItem label="사용자 의견 보내기" path="/feedback" /> */}
          <MenuItem label="약관 보기" path="/provision" />
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
