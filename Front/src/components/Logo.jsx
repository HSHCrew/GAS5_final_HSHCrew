import altariLogo from '../assets/altari-logo.svg';
import { StyledImage } from "./StyledTag.jsx";
import styled, { keyframes } from "styled-components";

// 스케일 업, 회전, 튕김 애니메이션 정의
const trendyAnimation = keyframes`
    0% {
        opacity: 0;
        transform: scale(0.5) rotate(-10deg);
    }
    50% {
        opacity: 0.7;
        transform: scale(1.1) rotate(10deg);
    }
    100% {
        opacity: 1;
        transform: scale(1) rotate(0deg);
    }
`;

// 애니메이션 적용한 컨테이너 스타일 정의
const AnimatedContainer = styled.div`
    height: 90%;
    width: 90%;
    margin-top: 250px;
    margin-left: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    animation: ${trendyAnimation} 1.8s ease-out;
`;

export const Logo = () => {
    return (
        <AnimatedContainer>
            <StyledImage src={altariLogo} alt="logo" height={'100%'} />
        </AnimatedContainer>
    );
};
