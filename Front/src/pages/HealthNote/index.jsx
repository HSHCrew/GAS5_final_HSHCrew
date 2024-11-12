import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

import altariLogo from '../../assets/altari-logo.svg';
import arrowIcon from '../../assets/arrow.svg';
import Choice from './Choice/Choice.jsx';
import Step1 from './Step1/Step1.jsx';
import Step2 from './Step2/Step2.jsx';
import Step3 from './Step3/Step3.jsx';
import Step4 from './Step4/Step4.jsx';
import Step5 from './Step5/Step5.jsx';
import Step6 from './Step6/Step6.jsx';
import Step7 from './Step7/Step7.jsx';
import Step8 from './Step8/Step8.jsx';
import Step9 from './Step9/Step9.jsx';
import './style.css';

function HealthNote() {
    const navigate = useNavigate();

    const [step, setStep] = useState(0);
    const [height, setHeight] = useState(170.0);
    const [weight, setWeight] = useState(50);
    const [rhValue, setRhValue] = useState('+');
    const [bloodType, setBloodType] = useState('A');
    const [chronicDisease, setChronicDisease] = useState([]);
    const [pastHistory, setPastHistory] = useState([]);
    const [familyHistory, setFamilyHistory] = useState([]);
    const [drugsAllergy, setDrugsAllergy] = useState([]);
    const [foodAllergy, setFoodAllergy] = useState([]);
    const [mealTime, setMealTime] = useState({
        breakfast: { hour: 8, minute: 0 },
        lunch: { hour: 12, minute: 0 },
        dinner: { hour: 18, minute: 30 },
    });

    const nextStep = () => {
        if (step >= 9) {
            const confirmed =
                window.confirm('건강노트 작성을 완료하시겠습니까?');
            console.log({
                height,
                weight,
                rhValue,
                bloodType,
                chronicDisease,
                pastHistory,
                familyHistory,
                drugsAllergy,
                foodAllergy,
                mealTime,
            });
            if (confirmed) {
                navigate('/home');
            }
        } else {
            setStep(step + 1);
        }
    };

    const previousStep = () => {
        setStep(step - 1);
    };

    return (
        <div className="container">
            <div className="inner-container">
                <div className="logo">
                    <img
                        src={altariLogo}
                        alt="Altari Logo"
                        className="logo-image"
                    />
                </div>
            </div>
            <div className="content-container">
                <div className="card">
                    {step === 0 ? (
                        <Choice nextStep={nextStep} />
                    ) : (
                        <>
                            {step === 1 ? (
                                <Step1
                                    height={height}
                                    updateHeight={setHeight}
                                />
                            ) : step === 2 ? (
                                <Step2
                                    weight={weight}
                                    updateWeight={setWeight}
                                />
                            ) : step === 3 ? (
                                <Step3
                                    rhValue={rhValue}
                                    updateRhValue={setRhValue}
                                    bloodType={bloodType}
                                    updateBloodType={setBloodType}
                                />
                            ) : step === 4 ? (
                                <Step4
                                    chronicDisease={chronicDisease}
                                    updateChronicDisease={setChronicDisease}
                                />
                            ) : step === 5 ? (
                                <Step5
                                    pastHistory={pastHistory}
                                    updatePastHistory={setPastHistory}
                                />
                            ) : step === 6 ? (
                                <Step6
                                    familyHistory={familyHistory}
                                    updateFamilyHistory={setFamilyHistory}
                                />
                            ) : step === 7 ? (
                                <Step7
                                    drugsAllergy={drugsAllergy}
                                    updateDrugsAllergy={setDrugsAllergy}
                                />
                            ) : step === 8 ? (
                                <Step8
                                    foodAllergy={foodAllergy}
                                    updateFoodAllergy={setFoodAllergy}
                                />
                            ) : step === 9 ? (
                                <Step9
                                    mealTime={mealTime}
                                    updateMealTime={setMealTime}
                                />
                            ) : (
                                setStep(9)
                            )}
                        </>
                    )}
                </div>

                {/* 하단 단계 및 화살표 - Step1부터 표시 */}
                {step > 0 && (
                    <div className="steps-footer">
                        <img
                            src={arrowIcon}
                            alt="Previous Arrow"
                            className="steps-prev-arrow"
                            style={{ transform: 'rotate(180deg)' }}
                            onClick={previousStep}
                        />
                        <p className="step1-step-indicator">
                            {step} / 9
                        </p>
                        <img
                            src={arrowIcon}
                            alt="Next Arrow"
                            className="steps-next-arrow"
                            onClick={nextStep}
                        />
                    </div>
                )}
            </div>
        </div>
    );
}

export default HealthNote;
