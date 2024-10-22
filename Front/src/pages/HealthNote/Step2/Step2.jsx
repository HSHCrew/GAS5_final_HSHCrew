import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import altariLogo from '../../../assets/altari-logo.svg';
import arrowIcon from '../../../assets/arrow.svg';
import scaleIcon from '../../../assets/weight.svg';
import './Step2.css';

const Step2 = ({ weight, updateWeight }) => {
    // 자연수 부분 핸들러
    const handleWeightIntegerChange = (event) => {
        const v = parseInt(event.target.value);
        updateWeight(v + (weight % 1) * 0.1);
    };

    // 소수점 부분 핸들러
    const handleWeightDecimalChange = (event) => {
        const v = parseInt(event.target.value);
        updateWeight(v * 0.1 + parseInt(weight));
    };

    return (
        <>
            <img
                src={scaleIcon}
                alt="Scale Icon"
                className="step2-scale-icon"
            />
            <p className="step2-card-text">몸무게가 몇 kg 인가요?</p>

            {/* 입력창 */}
            <div className="step2-input-group">
                {/* 자연수 부분 */}
                <select
                    className="step2-input-box whole-box"
                    aria-label="Select whole number part of weight"
                    value={parseInt(weight)}
                    onChange={handleWeightIntegerChange}>
                    {Array.from({ length: 101 }, (_, i) => (
                        <option key={i} value={30 + i}>
                            {30 + i}
                        </option>
                    ))}
                </select>
                <span className="step2-dot-text">.</span>
                {/* 소수점 고정 텍스트 */}
                {/* 소수점 부분 */}
                <select
                    className="step2-input-box decimal-box"
                    aria-label="Select decimal part of weight"
                    value={(weight * 10) % 10}
                    onChange={handleWeightDecimalChange}>
                    {Array.from({ length: 10 }, (_, i) => (
                        <option key={i} value={i}>
                            {i}
                        </option>
                    ))}
                </select>
                <p className="step2-unit-text">kg</p>
            </div>
        </>
    );
};

export default Step2;
