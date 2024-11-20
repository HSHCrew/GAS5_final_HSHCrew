/**
 * 특정 날짜로부터 복약 시작일과 종료일을 계산
 * @param {string} manufactureDate - 제조일 (YYYY-MM-DD 형식)
 * @param {number} totalDosingDays - 총 복약 일수
 * @returns {{ startDate: Date, endDate: Date }}
 */
export const calculateDosingPeriod = (manufactureDate, totalDosingDays) => {
    const startDate = new Date(manufactureDate);
    const endDate = new Date(startDate);
    endDate.setDate(startDate.getDate() + totalDosingDays - 1);
    return { startDate, endDate };
};

/**
 * 날짜 객체가 동일한 날인지 확인
 * @param {Date} date1 - 첫 번째 날짜
 * @param {Date} date2 - 두 번째 날짜
 * @returns {boolean} 동일한 날인지 여부
 */
export const isSameDay = (date1, date2) => {
    return (
        date1.getFullYear() === date2.getFullYear() &&
        date1.getMonth() === date2.getMonth() &&
        date1.getDate() === date2.getDate()
    );
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
 * 날짜를 포맷팅 (예: YYYY-MM-DD 형식)
 * @param {Date} date - 날짜 객체
 * @returns {string} 포맷된 날짜 문자열
 */
export const formatDate = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};

/**
 * 특정 날짜로부터 N일 전/후 날짜 계산
 * @param {Date} date - 기준 날짜
 * @param {number} days - 이동할 일수 (음수는 과거, 양수는 미래)
 * @returns {Date} 계산된 날짜 객체
 */
export const addDays = (date, days) => {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
};
