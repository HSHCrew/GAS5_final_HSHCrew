import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Calendar from "react-calendar";
import "react-calendar/dist/Calendar.css";
import "./MedicationManagement.css";
import apiClient from "../../api/apiClient";

import plusIcon from "../../assets/plus.svg";

const MedicationManagement = () => {
  const [date, setDate] = useState(new Date());
  const [prescriptions, setPrescriptions] = useState([]);
  const [selectedPrescriptions, setSelectedPrescriptions] = useState([]);
  const [filter, setFilter] = useState("전체");
  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth());
  const [viewMode, setViewMode] = useState("전체");
  const navigate = useNavigate();

  // API 호출
  useEffect(() => {
    const fetchPrescriptions = async () => {
      try {
        const username =
          localStorage.getItem("username") || sessionStorage.getItem("username");

        if (!username) {
          console.error("사용자 이름이 누락되었습니다. API 요청을 중단합니다.");
          return;
        }

        const response = await apiClient.get(
          `/altari/getInfo/userPrescription/${username}`
        );

        let data = response.data.map((prescription) => {
          const startDate = new Date(
            prescription.manufactureDate[0],
            prescription.manufactureDate[1] - 1,
            prescription.manufactureDate[2]
          );

          // totalDosingDay가 null인 경우 기본값 1 설정
          const totalDosingDay = prescription.totalDosingDay || 1;
          const endDate = new Date(startDate);
          endDate.setDate(endDate.getDate() + totalDosingDay - 1);

          return { ...prescription, startDate, endDate };
        });

        // 중복 제거: prescriptionNo 기준
        data = data.filter(
          (item, index, self) =>
            index ===
            self.findIndex((t) => t.prescriptionNo === item.prescriptionNo)
        );

        setPrescriptions(data);
      } catch (error) {
        console.error("처방전 데이터를 가져오는 중 오류 발생:", error);
      }
    };

    fetchPrescriptions();
  }, []);

  const getPrescriptionStatus = (prescription) => {
    const today = new Date();
    const adjustedEndDate = new Date(prescription.endDate);
    adjustedEndDate.setDate(adjustedEndDate.getDate() + 1);

    if (today < prescription.startDate) {
      return "예정";
    } else if (today >= prescription.startDate && today <= adjustedEndDate) {
      return "복용 중";
    } else {
      return "복약 종료";
    }
  };

  const handleDateChange = (selectedDate) => {
    setDate(selectedDate);
    const filteredPrescriptions = prescriptions.filter(
      (prescription) =>
        selectedDate >= prescription.startDate &&
        selectedDate <= prescription.endDate
    );
    setSelectedPrescriptions(filteredPrescriptions);
    setViewMode("날짜");
  };

  const handleFilterChange = (selectedFilter) => {
    setFilter(selectedFilter);
    const filteredPrescriptions = prescriptions.filter((prescription) => {
      const status = getPrescriptionStatus(prescription);
      if (selectedFilter === "전체") return true;
      return status === selectedFilter;
    });
    setSelectedPrescriptions(filteredPrescriptions);
    setViewMode(selectedFilter);
  };

  useEffect(() => {
    const filteredPrescriptions = prescriptions.filter(
      (prescription) =>
        prescription.startDate.getMonth() === currentMonth ||
        prescription.endDate.getMonth() === currentMonth
    );
    setSelectedPrescriptions(filteredPrescriptions);
  }, [currentMonth, prescriptions]);

  const tileClassName = ({ date, view }) => {
    if (view === "month") {
      for (const prescription of prescriptions) {
        if (date >= prescription.startDate && date <= prescription.endDate) {
          if (date.getTime() === prescription.startDate.getTime()) {
            return "curve-start circle-day";
          } else if (date.getTime() === prescription.endDate.getTime()) {
            return "curve-end circle-day";
          } else {
            return "curve-middle circle-day";
          }
        }
      }
    }
    return null;
  };

  const tileContent = ({ date, view }) => {
    if (view === "month") {
      const hasPrescription = prescriptions.some(
        (prescription) =>
          date >= prescription.startDate && date <= prescription.endDate
      );

      if (hasPrescription) {
        return (
          <div className="tile-dot">
            <span className="dot"></span>
            {prescriptions.map((prescription, index) => {
              if (date.getTime() === prescription.startDate.getTime()) {
                return (
                  <div
                    key={`${prescription.prescriptionNo}-${index}`}
                    className="prescription-start-text"
                  >
                    {`${prescription.startDate.getMonth() + 1}월 ${
                      prescription.startDate.getDate()
                    }일 처방`}
                  </div>
                );
              }
              return null;
            })}
          </div>
        );
      }
    }
    return null;
  };

  const handlePrescriptionClick = (prescription) => {
    // prescription.userPrescriptionId를 사용해 상세 페이지로 이동
    navigate(`/prescriptionDetail/${prescription.userPrescriptionId}`);
  };  
  

  return (
    <div className="medication-management-container">
      <div className="calendar-container">
        <Calendar
          onChange={handleDateChange}
          value={date}
          tileClassName={tileClassName}
          tileContent={tileContent}
          formatDay={(locale, date) => `${date.getDate()}`}
          onActiveStartDateChange={({ activeStartDate }) =>
            setCurrentMonth(activeStartDate.getMonth())
          }
          prev2Label={null}
          next2Label={null}
        />
      </div>

      <div className="filter-buttons">
        <button onClick={() => handleFilterChange("전체")}>
          전체 ({prescriptions.length})
        </button>
        <button onClick={() => handleFilterChange("복용 중")}>
          복용 중 (
          {prescriptions.filter((p) => getPrescriptionStatus(p) === "복용 중")
            .length}
          )
        </button>
        <button onClick={() => handleFilterChange("복약 종료")}>
          복약 종료 (
          {
            prescriptions.filter((p) => getPrescriptionStatus(p) === "복약 종료")
              .length
          }
          )
        </button>
      </div>

      <div className="prescription-info">
        <p>
          {viewMode === "날짜"
            ? `${date.getMonth() + 1}월 ${date.getDate()}일 처방전:`
            : `${filter} 처방전:`}
        </p>
        {selectedPrescriptions.length > 0 ? (
          selectedPrescriptions.map((prescription) => {
            return (
              <div
                key={prescription.userPrescriptionId}
                className="prescription-card"
                onClick={() => handlePrescriptionClick(prescription)}
              >
                <div className="prescription-details">
                  <img
                    src={plusIcon}
                    alt="처방 아이콘"
                    className="prescription-icon"
                  />
                  <div className="prescription-text">
                    <p className="prescription-name">
                      {prescription.prescriptionOrg}
                    </p>
                    <p className="prescription-dates">
                      {prescription.startDate.toLocaleDateString()} ~{" "}
                      {prescription.endDate.toLocaleDateString()}
                    </p>
                  </div>
                </div>
                <p
                  className={`prescription-status ${
                    getPrescriptionStatus(prescription) === "복약 종료"
                      ? "status-ended"
                      : ""
                  }`}
                >
                  {getPrescriptionStatus(prescription)}
                </p>
              </div>
            );
          })
        ) : (
          <p>
            <br />
            선택한 날짜에 처방전이 없습니다.
          </p>
        )}
      </div>
    </div>
  );
};

export default MedicationManagement;
