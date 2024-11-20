import React, { useState, useRef } from 'react';
import { useSwipeable } from 'react-swipeable';
import { CSSTransition, TransitionGroup } from 'react-transition-group';
import { useNavigate } from 'react-router-dom';
import useFetchMedications from '../../api/useFetchMedications'; // 훅 임포트
import clockIcon from '../../assets/clock.svg';
import './Home.css';

const Home = () => {
    const [day, setDay] = useState(0); // -1: 어제, 0: 오늘, 1: 내일
    const [direction, setDirection] = useState('');
    const navigate = useNavigate();
    const username = localStorage.getItem('username') || sessionStorage.getItem('username');
    const { medications, loading, error } = useFetchMedications(username, day); // 훅 사용
    const nodeRef = useRef(null);

    const handlers = useSwipeable({
        onSwipedLeft: () => {
            setDirection('left');
            setDay((prevDay) => prevDay + 1);
        },
        onSwipedRight: () => {
            setDirection('right');
            setDay((prevDay) => prevDay - 1);
        },
        preventScrollOnSwipe: true,
        delta: 10,
        trackMouse: true,
    });

    const handleMedicationClick = (medicationId) => {
        navigate(`/medicineinfo/${medicationId}`);
    };

    const groupMedicationsByTime = () => {
        const grouped = {
            morning: [],
            afternoon: [],
            evening: [],
        };

        medications.forEach((medication) => {
            if (medication.time === '10:00') grouped.morning.push(medication);
            else if (medication.time === '14:00') grouped.afternoon.push(medication);
            else if (medication.time === '20:00') grouped.evening.push(medication);
        });

        return grouped;
    };

    const { morning, afternoon, evening } = groupMedicationsByTime();

    const renderMedicationGroup = (meds, label, time) => (
        <div className="home-medication-card" key={time}>
            <div className="home-card-header">
                <img src={clockIcon} alt="Clock" className="home-clock-icon" />
                <p className="home-time">{label}</p>
            </div>
            <div className="home-medications-list">
                {meds.length === 0 ? (
                    <p>복약할 약이 없습니다.</p>
                ) : (
                    meds.map((medication) => (
                        <div
                            key={medication.medicationId}
                            className="home-medication-item"
                            onClick={() => handleMedicationClick(medication.medicationId)}
                        >
                            <div className="home-medication-image-container">
                                {medication.itemImage ? (
                                    <img
                                        src={medication.itemImage}
                                        alt={medication.medicationName}
                                        className="home-medication-image"
                                    />
                                ) : (
                                    <p>이미지 없음</p>
                                )}
                            </div>
                            <div className="home-medication-info">
                                <p>{medication.medicationName}</p>
                                <p>{medication.oneDose}정</p>
                            </div>
                        </div>
                    ))
                )}
            </div>
        </div>
    );

    if (loading) {
        return (
            <div className="spinner-container" role="status" aria-live="polite" aria-busy="true">
                <div className="spinner" aria-label="로딩 중"></div>
                <p>데이터를 불러오는 중입니다...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="spinner-container">
                <p className="error-text">데이터를 가져오는 중 오류가 발생했습니다.</p>
                <button onClick={() => window.location.reload()}>다시 시도</button>
            </div>
        );
    }

    return (
        <div className="home-container" {...handlers}>
            <TransitionGroup component={null}>
                <CSSTransition
                    key={day}
                    nodeRef={nodeRef}
                    classNames={direction === 'left' ? 'home-slide-left' : 'home-slide-right'}
                    timeout={300}
                >
                    <div className="home-content" ref={nodeRef}>
                        <p className="home-day-label">{`${
                            day === -1 ? '어제' : day === 1 ? '내일' : '오늘'
                        } 먹을 약`}</p>

                        {renderMedicationGroup(morning, '10:00 ', 'morning')}
                        {renderMedicationGroup(afternoon, '14:00 ', 'afternoon')}
                        {renderMedicationGroup(evening, '20:00 ', 'evening')}
                    </div>
                </CSSTransition>
            </TransitionGroup>
        </div>
    );
};

export default Home;
