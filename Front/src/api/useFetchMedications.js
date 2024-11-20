import { useState, useEffect, useCallback } from 'react';
import apiClient from './apiClient';

/**
 * 사용자 처방전 데이터를 가져오는 훅
 * @param {string} username 사용자 이름
 * @param {number} day 날짜 필터 (-1: 어제, 0: 오늘, 1: 내일)
 * @returns {Object} medications, loading, error 상태와 데이터
 */
const useFetchMedications = (username, day) => {
    const [medications, setMedications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // 상태 초기화 함수
    const resetState = useCallback(() => {
        setMedications([]);
        setLoading(true);
        setError(null);
    }, []);

    // 데이터 후처리 함수
    const processMedications = useCallback((data) => {
        return data.map((medication) => ({
            ...medication,
            isTaken: false, // 기본값
            onAlarm: false, // 기본값
        }));
    }, []);

    useEffect(() => {
        if (!username || day === undefined) {
            console.warn('username 또는 day 값이 유효하지 않습니다.');
            resetState();
            return;
        }

        const fetchMedications = async () => {
            try {
                resetState();
                const response = await apiClient.get(
                    `/altari/getInfo/userPrescription/${username}`,
                    { params: { day } }
                );

                // 응답 데이터 처리
                if (response.data && Array.isArray(response.data)) {
                    const processedData = processMedications(response.data);
                    setMedications(processedData);
                } else {
                    throw new Error('API 응답 형식이 유효하지 않습니다.');
                }
            } catch (error) {
                console.error('복약 데이터를 가져오는 중 오류 발생:', error);
                setError(error.message || '데이터를 가져오는 중 오류가 발생했습니다.');
            } finally {
                setLoading(false);
            }
        };

        fetchMedications();
    }, [username, day, resetState, processMedications]);

    return { medications, loading, error };
};

export default useFetchMedications;
