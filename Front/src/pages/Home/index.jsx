import React, { useState, useRef } from 'react';
import { useSwipeable } from 'react-swipeable';
import { CSSTransition, TransitionGroup } from 'react-transition-group';
import { useNavigate } from 'react-router-dom';
import './Home.css';
import tylenolIcon from '../../assets/tylenol.svg';
import clockIcon from '../../assets/clock.svg';

function Home() {
    const [day, setDay] = useState(0);
    const [direction, setDirection] = useState('');
    const [notification, setNotification] = useState(true);
    const [medicationConfirmed, setMedicationConfirmed] = useState(false);
    const navigate = useNavigate();
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
        navigate('/medicineinfo');
    };

    const handleConfirmMedication = () => {
        setMedicationConfirmed(true);
    };

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
                        <p className="home-day-label">{getDayLabel()}</p>

                        <div className="home-medication-card">
                            <img src={clockIcon} alt="Clock" className="home-clock-icon" />
                            <p className="home-time">오전  8:00</p>

                            <div className="home-medication-image-container" onClick={handleMedicationClick}>
                                <img src={tylenolIcon} alt="Tylenol" className="home-medication-image" />
                            </div>

                            <p className="home-medication-info" onClick={handleMedicationClick}>
                                타이레놀8시간이알서방정 325mg<br />1정
                            </p>

                            <div className="home-notification-container">
                                <span className="home-notification-label">알림</span>
                                <label className="home-toggle-switch">
                                    <input
                                        type="checkbox"
                                        checked={notification}
                                        onChange={toggleNotification} 
                                    />
                                    <span className="home-slider"></span>
                                </label>
                            </div>

                            {/* 복약 확인 버튼을 같은 라인에 추가 */}
                            <button 
                                className="home-confirm-button" 
                                onClick={handleConfirmMedication} 
                                disabled={medicationConfirmed}
                            >
                                {medicationConfirmed ? "복약 완료" : "확인"}
                            </button>
                        </div>
                    </div>
                </CSSTransition>
            </TransitionGroup>
        </div>
    );
}

export default Home;
