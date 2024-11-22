import React, { useState, useRef } from "react";
import { useNavigate } from "react-router-dom"; // 리액트 라우터에서 네비게이션 사용
import "./style.css";

function NewsCurationPopup() {
    const popupRef = useRef(null);
    const [isDragging, setIsDragging] = useState(false);
    const [position, setPosition] = useState({ x: 1150, y: 730 }); // 초기 위치
    const navigate = useNavigate(); // useNavigate 훅 사용

    const handleMouseDown = (e) => {
        setIsDragging(true);
        popupRef.current.dataset.offsetX = e.clientX - position.x;
        popupRef.current.dataset.offsetY = e.clientY - position.y;
    };

    const handleMouseMove = (e) => {
        if (!isDragging) return;

        // requestAnimationFrame을 사용하여 성능 최적화
        requestAnimationFrame(() => {
            const newX = e.clientX - parseFloat(popupRef.current.dataset.offsetX);
            const newY = e.clientY - parseFloat(popupRef.current.dataset.offsetY);

            setPosition({ x: newX, y: newY });
        });
    };

    const handleMouseUp = () => {
        setIsDragging(false);
    };

    const handleNavigate = () => {
        navigate("/news-curation"); // /news-curation 경로로 이동
    };

    return (
        <div
            ref={popupRef}
            className="news-popup-container"
            style={{ left: `${position.x}px`, top: `${position.y}px` }}
            onMouseDown={handleMouseDown}
            onMouseMove={handleMouseMove}
            onMouseUp={handleMouseUp}
            onMouseLeave={handleMouseUp} // 팝업 밖으로 커서가 나가도 드래그 중지
        >
            <div className="news-popup-icon" style={{ cursor: "default" }}>
                {/* 텍스트만 클릭 가능 */}
                <span
                    className="popup-text"
                    onClick={handleNavigate}
                    style={{ cursor: "pointer" }}
                >
                    뉴스
                </span>
                <i className="icon">📰</i>
            </div>
        </div>
    );
}

export default NewsCurationPopup;
