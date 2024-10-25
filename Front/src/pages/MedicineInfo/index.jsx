import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./MedicineInfo.css";

import backArrowImg from '../../assets/left.svg';  // 뒤로가기 이미지 경로

const MedicineInfo = () => {
  const [selectedTab, setSelectedTab] = useState("summary");  // 탭 상태
  const navigate = useNavigate();  // 뒤로가기 기능

  const handleBack = () => {
    navigate(-1);  // 뒤로가기
  };

  const renderContent = () => {
    switch (selectedTab) {
      case "summary":
        return (
          <div className="content">
            <h2>요약설명</h2>
            <p><br/>타이레놀8시간이알서방정325mg은 해열 및 진통 효과가 있는 약물로, 성분명은 아세트아미노펜입니다.</p>
          </div>
        );
      case "info":
        return (
          <div className="content">
            <h2>약 정보</h2>
            <p><br/>이 약은 325mg의 아세트아미노펜을 함유하고 있으며, 두통, 근육통 등의 통증 완화에 효과적입니다.</p>
          </div>
        );
      case "warning":
        return (
          <div className="content">
            <h2>주의사항</h2>
            <p><br/>간 질환이 있는 환자 또는 과량 복용 시 간 손상이 발생할 수 있으므로 주의하십시오.</p>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="container">
      <div className="info-box">
        <div className="header">
          <img 
            src={backArrowImg} 
            alt="back arrow" 
            className="back-arrow-img" 
            onClick={handleBack}  // 뒤로가기 기능
          />
          <p className="medicine-info">
            타이레놀8시간이알서방정325mg<br />
            <span className="ingredient">성분명: 아세트아미노펜</span>
          </p>
        </div>

        <div className="button-container">
          <button className="button-tab" onClick={() => setSelectedTab("summary")}>
            요약설명
          </button>
          <button className="button-tab" onClick={() => setSelectedTab("info")}>
            약 정보
          </button>
          <button className="button-tab" onClick={() => setSelectedTab("warning")}>
            주의사항
          </button>
        </div>

        <div className="content-container">
          {renderContent()}
        </div>
      </div>
    </div>
  );
};

export default MedicineInfo;
