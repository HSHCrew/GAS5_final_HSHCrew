import React from 'react';

import bloodIcon from '../../../assets/blood.svg';
import './Step3.css';

const Step3 = ({ rhValue, updateRhValue, bloodType, updateBloodType }) => {
    // Rh 값 변경 핸들러
    const handleRhChange = (event) => {
        updateRhValue(event.target.value);
    };

    // 혈액형 값 변경 핸들러
    const handleBloodTypeChange = (event) => {
        updateBloodType(event.target.value);
    };

    return (
        <>
            <img
                src={bloodIcon}
                alt="Blood Type Icon"
                className="step3-blood-icon"
            />
            <p className="step3-card-text">혈액형이 어떻게 되세요?</p>

            {/* Rh 값 선택 박스 */}
            <div className="step3-input-group">
                <select
                    className="step3-input-box"
                    value={rhValue}
                    onChange={handleRhChange}>
                    <option value="Rh+">Rh+</option>
                    <option value="Rh-">Rh-</option>
                </select>

                {/* 혈액형 선택 박스 */}
                <select
                    className="step3-input-box"
                    value={bloodType}
                    onChange={handleBloodTypeChange}>
                    <option value="A">A</option>
                    <option value="B">B</option>
                    <option value="O">O</option>
                    <option value="AB">AB</option>
                </select>
            </div>
        </>
    );
};

export default Step3;
