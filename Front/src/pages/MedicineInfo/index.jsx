import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import apiClient from "../../api/apiClient";
import ReactMarkdown from 'react-markdown';
import "./MedicineInfo.css";

import backArrowImg from '../../assets/left.svg'; // 뒤로가기 이미지

const MedicineInfo = () => {
    const { id } = useParams(); // URL에서 약물 ID 가져오기
    const [selectedTab, setSelectedTab] = useState("summary"); // 탭 상태
    const [medicineData, setMedicineData] = useState(null); // 약물 데이터
    const navigate = useNavigate(); // 뒤로가기 기능

    // 약물 데이터 불러오기
    useEffect(() => {
        const fetchMedicineInfo = async () => {
            try {
                const response = await apiClient.get(`/altari/drug-info/${id}`);
                setMedicineData(response.data); // 데이터 저장
            } catch (error) {
                console.error("약물 정보를 불러오는 중 오류 발생:", error);
            }
        };

        fetchMedicineInfo();
    }, [id]);

    // 탭 내용 렌더링
    const renderContent = () => {
        if (!medicineData) {
            return <p>데이터를 불러오는 중입니다...</p>;
        }

        switch (selectedTab) {
            case "summary":
                return (
                    <div className="medicine-info-content">
                        <h2 className="mediciine-info-content-subtitle">요약설명</h2>
                        <p>{medicineData.medicationInfo || "요약 정보가 없습니다."}</p>
                    </div>
                );
            case "info":
                return (
                    <div className="medicine-info-content">
                        <h2 className="mediciine-info-content-subtitle">약 정보</h2>
                        <p>{medicineData.additives || "추가 정보가 없습니다."}</p>
                    </div>
                );
            case "warning":
                return (
                    <div className="medicine-info-content">
                        <h2 className="mediciine-info-content-subtitle">주의사항</h2>
                        <ReactMarkdown>
                            {medicineData.medicationCautionWarningInfo || "주의사항 정보가 없습니다."}
                        </ReactMarkdown>
                    </div>
                );
            default:
                return null;
        }
    };

    // 뒤로가기 핸들러
    const handleBack = () => {
        navigate(-1);
    };

    return (
        <div className="medicine-info-container">
            <div className="medicine-info-header">
                <img
                    src={backArrowImg}
                    alt="back arrow"
                    className="medicine-info-back-arrow"
                    onClick={handleBack}
                />
                <div className="medicine-info-header-content">
                    {medicineData?.itemImage ? (
                        <img
                            src={medicineData.itemImage}
                            alt={medicineData.medicationName || "약 이미지"}
                            className="medicine-info-header-image"
                        />
                    ) : (
                        <div className="medicine-info-no-image">이미지 없음</div> // 이미지 없음 처리
                    )}
                    <div className="medicine-info-header-text">
                        <p className="medicine-info-name">
                            {medicineData?.medicationName || "약 이름 없음"}
                        </p>
                        <p className="medicine-info-ingredient">
                            {medicineData?.ingredient || "성분 정보 없음"}
                        </p>
                    </div>
                </div>
            </div>

            <div className="medicine-info-tabs">
                <button
                    className={`medicine-info-tab ${selectedTab === "summary" ? "active" : ""}`}
                    onClick={() => setSelectedTab("summary")}
                >
                    요약설명
                </button>
                <button
                    className={`medicine-info-tab ${selectedTab === "info" ? "active" : ""}`}
                    onClick={() => setSelectedTab("info")}
                >
                    약 정보
                </button>
                <button
                    className={`medicine-info-tab ${selectedTab === "warning" ? "active" : ""}`}
                    onClick={() => setSelectedTab("warning")}
                >
                    주의사항
                </button>
            </div>

            <div className="medicine-info-content-container">{renderContent()}</div>
        </div>
    );
};

export default MedicineInfo;
