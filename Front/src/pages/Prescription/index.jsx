import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "./style.css";
import apiClient from "../../api/apiClient";

import backIcon from "../../assets/left.svg";
import plusIcon from "../../assets/plus.svg";

const PrescriptionDetail = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [prescription, setPrescription] = useState(null);
  const [progressPercentage, setProgressPercentage] = useState(0); // 복약 성공률
  const [error, setError] = useState(false);
  const [completionMessage, setCompletionMessage] = useState("");

  useEffect(() => {
    if (!id) {
      console.error("prescriptionId가 유효하지 않습니다.");
      setError(true);
      return;
    }

    const fetchPrescription = async () => {
      try {
        const response = await apiClient.get(`/altari/getInfo/Prescription/${id}`);
        const prescriptionData = response.data;

        if (!prescriptionData) {
          console.error("해당 처방전 정보를 찾을 수 없습니다.");
          setError(true);
          return;
        }

        // `drug.totalDosingDays` 중 가장 큰 값을 계산
        const maxDosingDays = prescriptionData.drugs
          ? Math.max(...prescriptionData.drugs.map((drug) => drug.totalDosingDays || 0))
          : 0;

        // 처방 시작 날짜
        const manufactureDate = new Date(
          prescriptionData.manufactureDate[0],
          prescriptionData.manufactureDate[1] - 1,
          prescriptionData.manufactureDate[2]
        );

        // 처방 종료 날짜 계산 (처방 시작 날짜 + 최대 복약 일수 - 1)
        const endDate = new Date(manufactureDate);
        endDate.setDate(manufactureDate.getDate() + maxDosingDays - 1);

        // 처방 데이터에 종료 날짜와 총 복약 기간 추가
        prescriptionData.endDate = endDate;
        prescriptionData.totalDosingDay = maxDosingDays;

        setPrescription(prescriptionData);
      } catch (error) {
        console.error("처방전 데이터를 가져오는 중 오류 발생:", error.response || error.message);
        setError(true);
      }
    };

    const fetchProgress = async () => {
      try {
        const username = localStorage.getItem("username") || "defaultUsername"; // 사용자 이름 가져오기
        const progressResponse = await apiClient.get(`/altari/medication/progress/${username}`);
        const progressData = progressResponse.data["prescription_progress: "];
        const prescriptionProgress = progressData.find(
          (progress) => progress.prescriptionId === parseInt(id)
        );
        if (prescriptionProgress) {
          setProgressPercentage(prescriptionProgress.progress);
          setCompletionMessage(
            prescriptionProgress.progress === 100 ? "모든 약 복용 완료" : "진행 중"
          );
        }
      } catch (error) {
        console.error("복약 성공률 데이터를 가져오는 중 오류 발생:", error.response || error.message);
      }
    };

    fetchPrescription();
    fetchProgress();
  }, [id]);

  const circumference = 2 * Math.PI * 100;
  const offset = circumference - (progressPercentage / 100) * circumference;

  const handleBackClick = () => {
    navigate(-1);
  };

  if (error) {
    return (
      <div className="error-container">
        <h1>오류 발생</h1>
        <p>처방전을 불러오는 중 문제가 발생했습니다. 다시 시도해주세요.</p>
        <button onClick={handleBackClick}>돌아가기</button>
      </div>
    );
  }

  if (!prescription) {
    return <p>로딩 중...</p>;
  }

  // 약 클릭 시 MedicineInfo 페이지로 이동하는 핸들러
  const handleMedicationClick = (medicationId) => {
    navigate(`/medicineinfo/${medicationId}`);
  };

  return (
    <div className="prescription-page-container">
      <div className="prescription-page-header">
        <img
          src={backIcon}
          alt="Back Icon"
          className="prescription-page-back-button"
          onClick={handleBackClick}
        />
        <img src={plusIcon} alt="처방 아이콘" className="plus-icon" />
        <div className="prescription-title-container">
          <h2 className="prescription-title">{prescription.prescriptionOrg || "처방전"}</h2>
          <p className="prescription-date">
            {new Date(
              prescription.manufactureDate[0],
              prescription.manufactureDate[1] - 1,
              prescription.manufactureDate[2]
            ).toLocaleDateString()}{" "}
            ~ {prescription.endDate ? prescription.endDate.toLocaleDateString() : ""}
          </p>
        </div>
      </div>

      <div className="prescription-schedule">
        <h3 className="schedule-sub-title">복약 일정</h3>
        {progressPercentage === 100 ? (
          <p className="schedule-status">복약 완료</p>
        ) : (
          <p className="schedule-remaining-days">
            남은 기간:{" "}
            <span className="schedule-days">
              {Math.max(
                0,
                Math.ceil(
                  (prescription.endDate.getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24)
                )
              )}{" "}
              일
            </span>
          </p>
        )}

        <div className="schedule-progress-bar-container">
          <div
            className="schedule-progress-bar"
            style={{ width: `${progressPercentage}%` }}
          ></div>
        </div>

        <div className="schedule-day-count">
          <p>1일차</p>
          <p>
            총 {prescription.totalDosingDay ? prescription.totalDosingDay : "0"}일
          </p>
        </div>
      </div>

      <div className="prescription-complete-success">
        <h3 className="prescription-complete-success-title">복약 성공률</h3>
        <div className="circular-progress">
          <svg className="progress-ring" width="230" height="230">
            <circle
              className="progress-ring__background"
              cx="115"
              cy="115"
              r="100"
            />
            <circle
              className="progress-ring__circle"
              cx="115"
              cy="115"
              r="100"
              strokeDasharray={circumference}
              strokeDashoffset={offset}
            />
          </svg>
          <div className="circular-progress-text">
            <p className="progress-percentage">{progressPercentage}%</p>
            <p className="progress-label">성공률</p>
          </div>
        </div>
        <p className="completion-message">{completionMessage}</p>
      </div>

      <div className="medication-list">
        <h3>약 정보</h3>
        {prescription.drugs?.map((drug) => (
          <div
            key={drug.medication.medicationId}
            className="medication-item"
            onClick={() => handleMedicationClick(drug.medication.medicationId)}
            style={{ cursor: "pointer" }} // 마우스 호버 시 포인터 표시
          >
            <div className="medication-image">
              {drug.medication.itemImage ? (
                <img
                  src={drug.medication.itemImage}
                  alt={drug.medication.medicationName}
                />
              ) : (
                <p>이미지 없음</p>
              )}
            </div>
            <div>
              <p className="medication-name">{drug.medication.medicationName}</p>
              <p className="medication-dosage">
                {drug.oneDose}정 x {drug.dailyDosesNumber}회 / {drug.totalDosingDays}일
              </p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default PrescriptionDetail;
