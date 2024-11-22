/**
 * 특정 날짜로부터 복약 시작일과 종료일을 계산
 * @param {string} manufactureDate - 제조일 (YYYY-MM-DD 형식)
 * @param {number} totalDosingDays - 총 복약 일수
 * @returns {{ startDate: Date, endDate: Date }}
 */

// 날짜 배열을 문자열로 변환
const formatManufactureDate = (manufactureDateArray) => {
    const [year, month, day] = manufactureDateArray;
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
};

export const calculateDosingPeriod = (manufactureDate, totalDosingDays) => {
    const startDate = new Date(manufactureDate);
    const endDate = new Date(startDate);
    endDate.setDate(startDate.getDate() + totalDosingDays - 1); // 처방 기간 생성
    return { startDate, endDate };
};

/**
 * 날짜가 주어진 범위 내에 포함되는지 확인
 * @param {Date} targetDate - 검사할 날짜
 * @param {Date} startDate - 시작일
 * @param {Date} endDate - 종료일
 * @returns {boolean} 범위 내에 포함되는지 여부
 */
export const isDateInRange = (targetDate, startDate, endDate) => {
    return targetDate >= startDate && targetDate <= endDate;
};

/**
 * 활성화된 처방전 필터링
 * @param {Array} prescriptions - 처방전 목록
 * @returns {Array} 활성화된 처방전
 */
export const getActivePrescriptions = (prescriptions) => {
    const today = new Date();
    return prescriptions.filter((prescription) => {
        if (!Array.isArray(prescription.manufactureDate)) {
            console.warn('manufactureDate is not an array:', prescription.manufactureDate);
            return false;
        }

        const manufactureDate = formatManufactureDate(prescription.manufactureDate);

        // 모든 약물의 totalDosingDays를 고려
        const active = prescription.drugs.some((drug) => {
            const totalDosingDays = drug.totalDosingDays || 0;
            if (totalDosingDays === 0) return false;

            const { startDate, endDate } = calculateDosingPeriod(manufactureDate, totalDosingDays);
            return isDateInRange(today, startDate, endDate);
        });

        return active;
    });
};
