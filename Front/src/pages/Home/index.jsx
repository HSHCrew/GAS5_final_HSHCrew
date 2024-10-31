import React, { useState } from 'react';
import { useSwipeable } from 'react-swipeable';
import { CSSTransition, TransitionGroup } from 'react-transition-group';
import { useNavigate } from 'react-router-dom'; // useNavigate 추가
import './Home.css';

import tylenolIcon from '../../assets/tylenol.svg'; // 타이레놀 이미지 경로
import clockIcon from '../../assets/clock.svg'; // 시계 아이콘 이미지 경로

function Home() {
    const [day, setDay] = useState(0);
    const [direction, setDirection] = useState('');
    const [notification, setNotification] = useState(true);
    const navigate = useNavigate(); // 페이지 이동을 위한 useNavigate 훅

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

    const toggleNotification = () => {
        setNotification(!notification);
    };

    const getDayLabel = () => {
        switch (day) {
            case -1:
                return "어제 먹은 약";
            case 1:
                return "내일 먹을 약";
            default:
                return "오늘 먹을 약";
        }
    };

    const handleMedicationClick = () => {
        navigate('/medicineinfo'); // 약 정보 페이지로 이동
    };

    return (
        <div className="home-container" {...handlers}>
            <TransitionGroup component={null}>
                <CSSTransition
                    key={day}
                    classNames={direction === 'left' ? 'slide-left' : 'slide-right'}
                    timeout={300}
                >
                    <div className="home-content">
                        <p className="day-label">{getDayLabel()}</p>

                        <div className="medication-card">
                            {/* 시계 아이콘 */}
                            <img src={clockIcon} alt="Clock" className="clock-icon" />
                            <p className="time">오전 : 8:00</p>

                            {/* 타이레놀 이미지, 클릭 시 페이지 이동 */}
                            <div className="medication-image-container" onClick={handleMedicationClick}>
                                <img src={tylenolIcon} alt="Tylenol" className="medication-image" />
                            </div>

                            {/* 약 정보 텍스트, 클릭 시 페이지 이동 */}
                            <p className="medication-info" onClick={handleMedicationClick}>
                                타이레놀8시간이알서방정 325mg<br />1정
                            </p>

                            {/* 알림 토글 */}
                            <div className="notification-container">
                                <span className="notification-label">알림</span>
                                <label className="toggle-switch">
                                    <input
                                        type="checkbox"
                                        checked={notification}
                                        onChange={toggleNotification} 
                                    />
                                    <span className="slider"></span>
                                </label>
                            </div>
                        </div>
                    </div>
                </CSSTransition>
            </TransitionGroup>
        </div>
    );
}

export default Home;
