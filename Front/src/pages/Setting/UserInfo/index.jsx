import React from 'react';
import Header from '../../../components/Header';
import './style.css';

import userLogo from '../../../assets/user.svg';

const UserInfo = () => {
  return (
    <div className="userinfo-container">
      <Header title="개인정보 수정" />
      <div className="userinfo-box">
        {/* 프로필 섹션 */}
        <div className="profile-section">
          <img src={userLogo} alt="Profile" className="profile-image" />
          <p className="profile-name">홍길동</p>
          <button className="profile-edit">사진 수정</button>
        </div>

        {/* 개인정보 섹션 */}
        <div className="info-section">
          <InfoItem label="이름" value="홍길동" />
          <InfoItem label="생년월일" value="2000-01-01" />
          <InfoItem label="전화번호" value="010-1234-5678" />
          <InfoItem label="연동된 계정" value="카카오 계정" />
        </div>

        {/* 회원탈퇴 및 로그아웃 */}
        <div className="actions-section">
          <p className="delete-account">회원탈퇴</p>
        </div>
        <div className="logout-container">
            <p className="logout-button">로그아웃</p>
        </div>
      </div>
    </div>
  );
};

// 개별 정보 항목 컴포넌트
const InfoItem = ({ label, value }) => {
  return (
    <div className="info-item">
      <p className="info-label">{label}</p>
      <p className="info-value">{value}</p>
    </div>
  );
};

export default UserInfo;
