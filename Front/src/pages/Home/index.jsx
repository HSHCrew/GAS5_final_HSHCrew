// Home.jsx

import React, { useState, useRef, useEffect } from 'react';
import { useSwipeable } from 'react-swipeable';
import { CSSTransition, TransitionGroup } from 'react-transition-group';
import { useNavigate } from 'react-router-dom';
import useFetchMedications from '../../api/useFetchMedications';
import apiClient from '../../api/apiClient'; // 사용자 프로필 및 알림 API 클라이언트
import clockIcon from '../../assets/clock.svg';
import './Home.css';

const Home = () => {
    const [day, setDay] = useState(0); // -1: 어제, 0: 오늘, 1: 내일
    const [direction, setDirection] = useState('');
    const [userProfile, setUserProfile] = useState(null); // 사용자 프로필 상태
    const [alarms, setAlarms] = useState({
        onMorningMedicationAlarm: false,
        onLunchMedicationTimeAlarm: false,
        onDinnerMedicationTimeAlarm: false,
        onNightMedicationTimeAlarm: false,
    
    });
    const [medicationStatus, setMedicationStatus] = useState({
        morningTaken: false,
        lunchTaken: false,
        dinnerTaken: false,
        nightTaken: false
    });

    const [morningTime, setMorningTime] = useState('');
    const [lunchTime, setLunchTime] = useState('');
    const [dinnerTime, setDinnerTime] = useState('');
    const [nightTime, setNightTime] = useState(''); 
    const [loadingProfile, setLoadingProfile] = useState(true); // 사용자 프로필 로딩 상태

    const navigate = useNavigate();
    const username = localStorage.getItem('username') || sessionStorage.getItem('username');
    const { medications, loading, error } = useFetchMedications(username);
    const nodeRefs = useRef([]);

    const getNodeRef = (dayIndex) => {
        if (!nodeRefs.current[dayIndex]) {
            nodeRefs.current[dayIndex] = React.createRef();
        }
        return nodeRefs.current[dayIndex];
    };
    
    // 사용자 프로필 및 알림 상태 조회
    useEffect(() => {
        const fetchUserProfileAndAlarms = async () => {
            try {
                setLoadingProfile(true);
                const profileResponse = await apiClient.get(`/altari/getInfo/userProfile/${username}`);
                const profileData = profileResponse.data;
                setUserProfile(profileData);
    
                if (profileData.morningMedicationTime) {
                    setMorningTime(formatTime(profileData.morningMedicationTime));
                }
                if (profileData.lunchMedicationTime) {
                    setLunchTime(formatTime(profileData.lunchMedicationTime));
                }
                if (profileData.dinnerMedicationTime) {
                    setDinnerTime(formatTime(profileData.dinnerMedicationTime));
                    setNightTime(calculateNightTime(profileData.dinnerMedicationTime));
                }
    
                console.log('Fetched user profile:', profileResponse.data);
    
                const alarmResponse = await apiClient.get(`/altari/medication/getAlarm/${username}`);
                setAlarms(alarmResponse.data);
                console.log('Fetched alarms:', alarmResponse.data);

                await fetchMedicationStatus();
    
            } catch (error) {
                console.error('사용자 데이터 가져오기 실패:', error);
            } finally {
                setLoadingProfile(false);
            }
        };
    
        fetchUserProfileAndAlarms();
    }, [username]);
    
    useEffect(() => {
        if (username && !loadingProfile) {  // 프로필 로딩이 완료된 후에만 실행
            fetchMedicationStatus();
        }
    }, [day, username, loadingProfile]);
    
    // fetchMedicationStatus 함수 수정
    const fetchMedicationStatus = async () => {
        try {
            const response = await apiClient.get(
                `/altari/medication/getMedicationCompletion/${username}`,
                {
                    headers: {
                        'Authorization': `Bearer ${localStorage.getItem('token')}`
                    }
                }
            );
            console.log('Medication status:', response.data);
            
            // 현재 날짜에 day 상태값을 더해서 인덱스 계산
            const today = new Date();
            const targetDate = today.getDate() + day; // day는 -1(어제), 0(오늘), 1(내일)
            const targetIndex = targetDate - 1;
            

            console.log('=== 복약 상태 조회 ===');
            console.log('현재 day 상태:', day);
            console.log('오늘 날짜:', today.getDate());
            console.log('계산된 targetDate:', targetDate);
            console.log('계산된 targetIndex:', targetIndex);
            console.log('서버 응답 데이터:', response.data);
            console.log('Current day:', day, 'Target index:', targetIndex);
            
            if (targetIndex >= 0 && targetIndex < response.data.length) {
                const statusData = response.data[targetIndex];
                if (statusData) {
                    setMedicationStatus({
                        morningTaken: Boolean(statusData.morningTaken),
                        lunchTaken: Boolean(statusData.lunchTaken),
                        dinnerTaken: Boolean(statusData.dinnerTaken),
                        nightTaken: Boolean(statusData.nightTaken)
                    });
                }
            }
        } catch (error) {
            console.error('복약 상태 조회 실패:', error);
        }
    };

    
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

    const handleConfirmMedicationGroup = async (meds, timeKey) => {
        try {
            const requestData = {
                morningTaken: timeKey === 'morning',
                lunchTaken: timeKey === 'lunch',
                dinnerTaken: timeKey === 'dinner',
                nightTaken: timeKey === 'night'
            };
    
            console.log('Sending request:', requestData);
    
            const response = await apiClient.post(
                `/altari/confirm/${username}`,
                requestData,
                {
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${localStorage.getItem('token')}`
                    }
                }
            );
            
            console.log('Response:', response.data);
            
            // 성공 시 즉시 상태 업데이트
            setMedicationStatus(prev => ({
                ...prev,
                [`${timeKey}Taken`]: true
            }));
            
            // 서버에서 최신 상태 다시 조회
            await fetchMedicationStatus();
            
            alert('복약이 확인되었습니다.');
        } catch (error) {
            console.error('복약 확인 실패:', error);
            alert('복약 확인 중 오류가 발생했습니다.');
        }
    };
    

    const toggleAlarm = async (alarmKey) => {
        try {
            const updatedAlarms = { ...alarms, [alarmKey]: !alarms[alarmKey] };
            setAlarms(updatedAlarms);

            await apiClient.post(`/altari/medication/onAlarm/${username}`, updatedAlarms);
            console.log('알림 상태 업데이트 완료');
        } catch (error) {
            console.error('알림 상태 업데이트 실패:', error);
        }
    };

    const formatTime = (timeArray) => {
        if (!timeArray || timeArray.length < 2) return '';
        const [hour, minute] = timeArray;
        return `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
    };

    const calculateNightTime = (dinnerTimeArray) => {
        if (!dinnerTimeArray || dinnerTimeArray.length < 2) return '';
        let [hour, minute] = dinnerTimeArray;
        hour += 3; // 저녁 시간에 3시간 추가
        if (hour >= 24) {
            hour -= 24; // 만약 24시를 초과하면 0시로 리셋
        }
        return `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
    };
    

    const renderGroupedMedications = (meds = [], time, alarmKey, timeKey) => {
        if (!meds || meds.length === 0) return null;
    
        const isTaken = medicationStatus[`${timeKey}Taken`];

        return (
            <div key={`${time}-${alarmKey}`} className="home-medication-group-card">
                <img src={clockIcon} alt="Clock" className="home-clock-icon" />
                <p className="home-time">{time}</p>
                <div className="medications-container">
                    {meds.map((medication) => (
                        <div
                            key={medication.medicationId}
                            className={`home-medication-item ${medication.isTaken ? 'medication-taken' : ''}`}
                            onClick={() => handleMedicationClick(medication.medicationId)}
                        >
                            {medication.itemImage ? (
                                <div className="home-medication-image">
                                    <img
                                        src={medication.itemImage}
                                        alt={medication.medicationName}
                                        className="home-medication-image"
                                    />
                                </div>
                            ) : (
                                <p>이미지 없음</p>
                            )}
                            <div className="home-medication-info-container">
                                <p className="home-medication-info">{medication.medicationName}</p>
                                <p className="home-medication-count">{medication.oneDose || ''}정</p>
                            </div>
                        </div>
                    ))}
                </div>
                {/* 확인 버튼 */}
                <button
                    className={`home-confirm-button ${isTaken ? 'taken' : ''}`}
                    onClick={() => handleConfirmMedicationGroup(meds, timeKey)}
                    disabled={isTaken}  // 복약 완료시 버튼 비활성화
                >
                    {isTaken ? '복약 완료' : '확인'}
                </button>
                {/* 알림 토글 */}
                <div className="home-notification-container">
                    <label className="home-notification-label">알림</label>
                    <label className="home-toggle-switch">
                        <input
                            type="checkbox"
                            checked={alarms[alarmKey]}
                            onChange={() => toggleAlarm(alarmKey)}
                        />
                        <span className="home-slider"></span>
                    </label>
                </div>
            </div>
        );
    };

    // 모든 약물 그룹이 비어있는지 확인
    const hasMedications = Object.values(medications).some((medArray) => medArray.length > 0);

    if (loading || loadingProfile) {
        return (
            <div className="loading-container">
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
            {/* 사용자 프로필 표시 */}
            <div className="home-profile-container">
                {userProfile ? (
                    <div className="profile-info">
                        <p className="profile-name">{userProfile.name}</p>
                        <p className="profile-email">{userProfile.email}</p>
                    </div>
                ) : (
                    <p>프로필 정보를 불러오는 중...</p>
                )}
            </div>

            <TransitionGroup component="div" className="home-transition-wrapper">
                <CSSTransition
                    key={day}
                    nodeRef={getNodeRef(day)}
                    classNames={direction === 'left' ? 'home-slide-left' : 'home-slide-right'}
                    timeout={300}
                >
                    <div className="home-content" ref={getNodeRef(day)}>
                        <p className="home-day-label">{`
                            ${day === -1 ? '어제' : day === 1 ? '내일' : '오늘'} 먹을 약`}</p>

                        {medications.morningMedications.length > 0 &&
                            renderGroupedMedications(
                                medications.morningMedications,
                                morningTime || '08:00',
                                'onMorningMedicationAlarm',
                                'morning'
                            )}
                        {medications.lunchMedications.length > 0 &&
                            renderGroupedMedications(
                                medications.lunchMedications,
                                lunchTime || '13:00', // 유저 프로필에서 불러온 점심 복용 시간
                                'onLunchMedicationTimeAlarm',
                                'lunch'
                            )}
                        {medications.dinnerMedications.length > 0 &&
                            renderGroupedMedications(
                                medications.dinnerMedications,
                                dinnerTime || '19:00', // 유저 프로필에서 불러온 저녁 복용 시간
                                'onDinnerMedicationTimeAlarm',
                                'dinner'
                            )}
                        {medications.nightMedications.length > 0 &&
                            renderGroupedMedications(
                                medications.nightMedications,
                                nightTime || '22:00', // 유저 프로필에서 계산된 야간 복용 시간
                                'onNightMedicationTimeAlarm',
                                'night'
                            )}

                        {/* 모든 약물 그룹이 비어있을 때 메시지 표시 */}
                        {!hasMedications && (
                            <p className="no-medications-message">복약할 약이 없습니다.</p>
                        )}
                    </div>
                </CSSTransition>
            </TransitionGroup>
        </div>
    );
};

export default Home;