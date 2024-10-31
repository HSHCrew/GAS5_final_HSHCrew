import React, { useState, useEffect } from "react";
import Calendar from 'react-calendar';
import 'react-calendar/dist/Calendar.css';
import './MedicationManagement.css';

import plusIcon from '../../assets/plus.svg';

const MedicationManagement = () => {
  const [date, setDate] = useState(new Date());
  const [selectedPrescriptions, setSelectedPrescriptions] = useState([]);
  const [filter, setFilter] = useState('전체');
  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth());
  const [viewMode, setViewMode] = useState('전체'); // viewMode 상태 추가

  // 임시 처방전 데이터
  const prescriptions = [
    { id: 1, name: "편두통", startDate: new Date(2024, 9, 3), endDate: new Date(2024, 9, 5) },
    { id: 2, name: "급성 장염", startDate: new Date(2024, 9, 4), endDate: new Date(2024, 9, 10) },
    { id: 3, name: "고혈압", startDate: new Date(2024, 9, 5), endDate: new Date(2024, 9, 15) }
  ];

  // 현재 날짜를 기준으로 동적으로 상태 계산
  const getPrescriptionStatus = (prescription) => {
    const today = new Date();
    if (today < prescription.startDate) {
      return "예정";
    } else if (today >= prescription.startDate && today <= prescription.endDate) {
      return "복용 중";
    } else {
      return "종료";
    }
  };

  // 특정 날짜를 클릭했을 때 그 날짜에 맞는 처방전 필터링
  const handleDateChange = (selectedDate) => {
    setDate(selectedDate);
    const filteredPrescriptions = prescriptions.filter(
      (prescription) => selectedDate >= prescription.startDate && selectedDate <= prescription.endDate
    );
    setSelectedPrescriptions(filteredPrescriptions); // 필터와 상관없이 선택한 날짜에 해당하는 처방전만 보여줌
    setViewMode("날짜"); // viewMode를 날짜 선택으로 변경
  };

  // 필터 변경에 따른 처리 (전체, 복용 중, 종료)
  const handleFilterChange = (selectedFilter) => {
    setFilter(selectedFilter);

    const filteredPrescriptions = prescriptions.filter((prescription) => {
      const status = getPrescriptionStatus(prescription);
      if (selectedFilter === '전체') return true;
      return status === selectedFilter;
    });

    setSelectedPrescriptions(filteredPrescriptions);
    setViewMode(selectedFilter); // 필터에 맞게 viewMode 변경
  };

  // 현재 달의 처방전들을 필터링
  useEffect(() => {
    const filteredPrescriptions = prescriptions.filter(
      (prescription) =>
        prescription.startDate.getMonth() === currentMonth || prescription.endDate.getMonth() === currentMonth
    );
    setSelectedPrescriptions(filteredPrescriptions);
  }, [currentMonth]);

  // 타일에 클래스를 동적으로 추가하는 함수 (처방 기간에 맞는 클래스 지정)
  const tileClassName = ({ date, view }) => {
    if (view === 'month') {
      for (const prescription of prescriptions) {
        if (date >= prescription.startDate && date <= prescription.endDate) {
          if (date.getTime() === prescription.startDate.getTime()) {
            return 'curve-start circle-day'; // 시작 날짜에 동그라미 추가
          } else if (date.getTime() === prescription.endDate.getTime()) {
            return 'curve-end circle-day'; // 종료 날짜에 동그라미 추가
          } else {
            return 'curve-middle circle-day'; // 중간 날짜에도 동그라미 추가
          }
        }
      }
    }
    return null;
  };

  // 시작 날짜 밑에 처방 관련 텍스트 및 dot 추가하는 함수
  const tileContent = ({ date, view }) => {
    if (view === 'month') {
      // 처방전 시작 날짜에 dot 추가
      const hasPrescription = prescriptions.some(
        (prescription) =>
          date >= prescription.startDate && date <= prescription.endDate
      );

      if (hasPrescription) {
        return (
          <div className="tile-dot">
            {/* dot 추가 */}
            <span className="dot"></span>
            {/* 처방전 시작 텍스트 */}
            {prescriptions.map((prescription) => {
              if (date.getTime() === prescription.startDate.getTime()) {
                return (
                  <div key={prescription.id} className="prescription-start-text">
                    {`${prescription.startDate.getMonth() + 1}월 ${prescription.startDate.getDate()}일 처방`}
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

  return (
    <div className="medication-management-container">
      {/* 달력 */}
      <div className="calendar-container">
        <Calendar
          onChange={handleDateChange}
          value={date}
          tileClassName={tileClassName}
          tileContent={tileContent}
          formatDay={(locale, date) => `${date.getDate()}`}
          onActiveStartDateChange={({ activeStartDate }) => setCurrentMonth(activeStartDate.getMonth())}
          prev2Label={null}
          next2Label={null}
        />
      </div>

      {/* 필터 버튼 */}
      <div className="filter-buttons">
        <button onClick={() => handleFilterChange('전체')}>
          전체 ({prescriptions.length})
        </button>
        <button onClick={() => handleFilterChange('복용 중')}>
          복용 중 ({prescriptions.filter((p) => getPrescriptionStatus(p) === '복용 중').length})
        </button>
        <button onClick={() => handleFilterChange('종료')}>
          종료 ({prescriptions.filter((p) => getPrescriptionStatus(p) === '종료').length})
        </button>
      </div>

      {/* 선택된 날짜에 해당하는 처방전 정보 표시 */}
      <div className="prescription-info">
        <p>{viewMode === '날짜' ? `${date.getMonth() + 1}월 ${date.getDate()}일 처방전:` : `${filter} 처방전:`}</p>
        {selectedPrescriptions.length > 0 ? (
          selectedPrescriptions.map((prescription) => {
            const status = getPrescriptionStatus(prescription); // 상태 가져오기
            return (
              <div key={prescription.id} className="prescription-card">
                <div className="prescription-details">
                  <img src={plusIcon} alt="처방 아이콘" className="prescription-icon" />
                  <div className="prescription-text">
                    <p className="prescription-name">{prescription.name}</p>
                    <p className="prescription-dates">{prescription.startDate.toLocaleDateString()} ~ {prescription.endDate.toLocaleDateString()}</p>
                  </div>
                </div>
                {/* 상태에 따른 텍스트 색상 동적 클래스 적용 */}
                <p className={`prescription-status ${status === '종료' ? 'status-ended' : ''}`}>
                  {status === '종료' ? '복약 종료' : status}
                </p>
              </div>
            );
          })
        ) : (
          <p><br />선택한 날짜에 처방전이 없습니다.</p>
        )}
      </div>
    </div>
  );
};

export default MedicationManagement;
