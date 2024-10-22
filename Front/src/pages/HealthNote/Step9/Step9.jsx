import React from 'react';

import eveningIcon from '../../../assets/moon.svg';
import afternoonIcon from '../../../assets/sun.svg';
import morningIcon from '../../../assets/sunrise.svg';
import './Step9.css';

const Step9 = ({ mealTime, updateMealTime }) => {
    const handleMealTimeChange = (key, type, value) => {
        const temp = { ...mealTime[key], [type]: value };
        console.log(temp);
        updateMealTime({ ...mealTime, [key]: temp });
    };

    return (
        <>
            {/* 아침 */}
            <div className="step9-time-row">
                <div className="step9-time-icon-container">
                    <img
                        src={morningIcon}
                        alt="Morning Icon"
                        className="step9-icon"
                    />
                    <p className="step9-label">아침</p>
                </div>
                <div className="step9-time-select">
                    <select
                        className="step9-select"
                        value={mealTime.breakfast.hour}
                        onChange={(e) =>
                            handleMealTimeChange(
                                'breakfast',
                                'hour',
                                parseInt(e.target.value)
                            )
                        }>
                        {Array.from({ length: 24 }, (_, i) => (
                            <option key={i} value={i}>
                                {i < 10 ? `0${i}` : i}
                            </option>
                        ))}
                    </select>
                    <span className="step9-colon">:</span>
                    <select
                        className="step9-select"
                        value={mealTime.breakfast.minute}
                        onChange={(e) =>
                            handleMealTimeChange(
                                'breakfast',
                                'minute',
                                parseInt(e.target.value)
                            )
                        }>
                        {Array.from({ length: 60 }, (_, i) => (
                            <option key={i} value={i}>
                                {i < 10 ? `0${i}` : i}
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            {/* 점심 */}
            <div className="step9-time-row">
                <div className="step9-time-icon-container">
                    <img
                        src={afternoonIcon}
                        alt="Afternoon Icon"
                        className="step9-icon"
                    />
                    <p className="step9-label">점심</p>
                </div>
                <div className="step9-time-select">
                    <select
                        className="step9-select"
                        value={mealTime.lunch.hour}
                        onChange={(e) =>
                            handleMealTimeChange(
                                'lunch',
                                'hour',
                                parseInt(e.target.value)
                            )
                        }>
                        {Array.from({ length: 24 }, (_, i) => (
                            <option key={i} value={i}>
                                {i < 10 ? `0${i}` : i}
                            </option>
                        ))}
                    </select>
                    <span className="step9-colon">:</span>
                    <select
                        className="step9-select"
                        value={mealTime.lunch.minute}
                        onChange={(e) =>
                            handleMealTimeChange(
                                'lunch',
                                'minute',
                                parseInt(e.target.value)
                            )
                        }>
                        {Array.from({ length: 60 }, (_, i) => (
                            <option key={i} value={i}>
                                {i < 10 ? `0${i}` : i}
                            </option>
                        ))}
                    </select>
                </div>
            </div>

            {/* 저녁 */}
            <div className="step9-time-row">
                <div className="step9-time-icon-container">
                    <img
                        src={eveningIcon}
                        alt="Evening Icon"
                        className="step9-icon"
                    />
                    <p className="step9-label">저녁</p>
                </div>
                <div className="step9-time-select">
                    <select
                        className="step9-select"
                        value={mealTime.dinner.hour}
                        onChange={(e) =>
                            handleMealTimeChange(
                                'dinner',
                                'hour',
                                parseInt(e.target.value)
                            )
                        }>
                        {Array.from({ length: 24 }, (_, i) => (
                            <option key={i} value={i}>
                                {i < 10 ? `0${i}` : i}
                            </option>
                        ))}
                    </select>
                    <span className="step9-colon">:</span>
                    <select
                        className="step9-select"
                        value={mealTime.dinner.minute}
                        onChange={(e) =>
                            handleMealTimeChange(
                                'dinner',
                                'minute',
                                parseInt(e.target.value)
                            )
                        }>
                        {Array.from({ length: 60 }, (_, i) => (
                            <option key={i} value={i}>
                                {i < 10 ? `0${i}` : i}
                            </option>
                        ))}
                    </select>
                </div>
            </div>
        </>
    );
};

export default Step9;
