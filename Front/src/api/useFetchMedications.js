import { useState, useEffect, useMemo } from 'react';
import apiClient from './apiClient';
import { getActivePrescriptions } from './dateUtils';

// 초기 상태 정의
const initialMedicationsState = {
    morningMedications: [],
    lunchMedications: [],
    dinnerMedications: [],
    nightMedications: [],
};

const useFetchMedications = (username) => {
    const [medications, setMedications] = useState(initialMedicationsState);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        let isMounted = true;

        const fetchMedications = async () => {
            try {
                setLoading(true);

                // Step 1: 유저 처방전 조회
                const prescriptionResponse = await apiClient.get(
                    `/altari/getInfo/userPrescription/${username}`
                );

                // 응답 데이터 검증
                if (!Array.isArray(prescriptionResponse.data)) {
                    throw new Error('처방전 데이터 형식이 올바르지 않습니다.');
                }

                // Step 2: 활성화된 처방전 필터링
                const activePrescriptions = getActivePrescriptions(prescriptionResponse.data);

                if (activePrescriptions.length === 0) {
                    if (isMounted) setMedications(initialMedicationsState);
                    return;
                }

                // Step 3: 활성화된 처방전의 약물 데이터 조회
                const medicationPromises = activePrescriptions.map((prescription) =>
                    apiClient.get(`/altari/drug-TimedMedication/${prescription.userPrescriptionId}`)
                );
                const medicationResponses = await Promise.all(medicationPromises);

                // 각 응답 데이터 검증
                medicationResponses.forEach((response, index) => {
                    if (!response.data || typeof response.data !== 'object') {
                        console.warn(`Unexpected medication response format for prescription ${activePrescriptions[index].userPrescriptionId}:`, response.data);
                    }
                });

                // Step 4: 약물 데이터를 그룹화
                const groupedMedications = medicationResponses.reduce((acc, response) => {
                    if (!response.data || typeof response.data !== 'object') {
                        return acc;
                    }

                    // response.data가 이미 그룹화된 데이터인 경우 병합
                    Object.keys(acc).forEach((timeKey) => {
                        if (response.data[timeKey] && Array.isArray(response.data[timeKey])) {
                            acc[timeKey] = acc[timeKey].concat(response.data[timeKey]);
                        }
                    });

                    return acc;
                }, { ...initialMedicationsState });

                if (isMounted) setMedications(groupedMedications);
            } catch (err) {
                console.error('Error fetching medications:', err);
                if (isMounted) setError(err.message || '데이터를 불러오는 중 오류가 발생했습니다.');
                if (isMounted) setMedications(initialMedicationsState); // 오류 발생 시 초기 상태로 설정
            } finally {
                if (isMounted) setLoading(false);
            }
        };

        fetchMedications();

        return () => {
            isMounted = false;
        };
    }, [username]);

    const result = useMemo(() => ({ medications, loading, error }), [medications, loading, error]);

    return result;
};

export default useFetchMedications;
