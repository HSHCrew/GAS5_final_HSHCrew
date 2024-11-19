import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "./style.css";
import apiClient from "../../api/apiClient";

import backIcon from "../../assets/left.svg";
import plusIcon from "../../assets/plus.svg";

const PrescriptionDetail = () => {
  const navigate = useNavigate();
  const { prescriptionId } = useParams(); // URL에서 처방전 ID 가져오기
  const [prescription, setPrescription] = useState(null); // 처방전 데이터 상태
  const [progressPercentage, setProgressPercentage] = useState(0); // 복약 성공률
  const [isCompleted, setIsCompleted] = useState(false); // 복약 종료 여부
  const [error, setError] = useState(false); // 에러 상태 추가

  // 처방전 데이터 가져오기
  useEffect(() => {
    const fetchPrescription = async () => {
      try {
        const username =
          localStorage.getItem("username") || sessionStorage.getItem("username");
        if (!username) {
          console.error("로그인 정보가 없습니다.");
          return;
        }

        const response = await apiClient.get(
          `/altari/getInfo/userPrescription/${username}`
        );
        const prescriptions = response.data;

        // 특정 prescriptionId로 데이터 필터링
        const selectedPrescription = prescriptions.find(
          (item) => String(item.userPrescriptionId) === String(prescriptionId)
        );

        if (!selectedPrescription) {
          console.error("해당 처방전 정보를 찾을 수 없습니다.");
          setError(true);
          return;
        }

        const totalDays = selectedPrescription.totalDosingDay || 1;
        const takenDays = selectedPrescription.takenDays || 0;

        setProgressPercentage(Math.round((takenDays / totalDays) * 100));

        const today = new Date();
        const endDate = new Date(selectedPrescription.endDate);
        setIsCompleted(today > endDate); // 복약 기간이 종료되었는지 확인

        setPrescription(selectedPrescription);
      } catch (error) {
        console.error("처방전 데이터를 가져오는 중 오류 발생:", error);
        setError(true);
      }
    };

    fetchPrescription();
  }, [prescriptionId]);

  const handleBackClick = () => {
    navigate(-1); // 뒤로 가기
  };

  const handleMedicationClick = (medicationId) => {
    navigate(`/medicineinfo/${medicationId}`); // 약 정보 페이지로 이동
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

  const startDate = new Date(prescription.manufactureDate[0], prescription.manufactureDate[1] - 1, prescription.manufactureDate[2]);
  const endDate = new Date(startDate);
  endDate.setDate(startDate.getDate() + (prescription.totalDosingDay || 1) - 1);

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
          <h2 className="prescription-title">
            {prescription.prescriptionOrg || "처방전"}
          </h2>
          <p className="prescription-date">
            {startDate.toLocaleDateString()} ~ {endDate.toLocaleDateString()}
          </p>
        </div>
      </div>

      <div className="prescription-schedule">
        <h3 className="schedule-sub-title">복약 일정</h3>
        {isCompleted ? (
          <p className="schedule-status">복약 종료</p>
        ) : (
          <p className="schedule-remaining-days">
            남은 기간{" "}
            <span className="schedule-days">
              {Math.max(0, Math.ceil((endDate - new Date()) / (1000 * 60 * 60 * 24)))}일
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
          <p>총 {prescription.totalDosingDay || 1}일</p>
        </div>
      </div>
    </div>
  );
};

export default PrescriptionDetail;
