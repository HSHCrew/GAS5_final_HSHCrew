import React, { useEffect, useState } from 'react';

import heightIcon from '../../../assets/height.svg';
import './Step1.css';

const Step1 = ({ height, updateHeight }) => {
    // 자연수 부분 핸들러
    const handleHeightIntegerChange = (event) => {
        const v = parseInt(event.target.value);
        updateHeight(v + (height % 1) * 0.1);
    };

    // 소수점 부분 핸들러
    const handleHeightDecimalChange = (event) => {
        const v = parseInt(event.target.value);
        updateHeight(v * 0.1 + parseInt(height));
    };

    return (
        <>
            <img
                src={heightIcon}
                alt="Height Icon"
                className="step1-height-icon"
            />
            <p className="step1-card-text">키가 몇 cm 인가요?</p>

            {/* 입력창 */}
            <div className="step1-input-group">
                <select
                    value={parseInt(height)}
                    className="step1-input-box whole-box"
                    aria-label="Select whole number part of height"
                    onChange={handleHeightIntegerChange}>
                    {Array.from({ length: 51 }, (_, i) => (
                        <option key={i} value={150 + i}>
                            {150 + i}
                        </option>
                    ))}
                </select>
                <span className="step1-dot-text">.</span>
                {/* 소수점 고정 텍스트 */}
                <select
                    value={(height * 10) % 10}
                    className="step1-input-box decimal-box"
                    aria-label="Select decimal part of height"
                    onChange={handleHeightDecimalChange}>
                    {Array.from({ length: 10 }, (_, i) => (
                        <option key={i} value={i}>
                            {i}
                        </option>
                    ))}
                </select>
                <p className="step1-unit-text">cm</p>
            </div>
        </>
    );
};

export default Step1;
