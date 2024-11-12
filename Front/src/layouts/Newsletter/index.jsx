import React from 'react';
import ReactDOM from 'react-dom';
import './style.css';
import { useNavigate } from 'react-router-dom'; // React Router를 사용한 페이지 이동

function NewsCurationPopup() {
    const navigate = useNavigate();

    const handleClick = () => {
        navigate('/news-curation'); // 뉴스 큐레이션 페이지로 이동
    };

    return (
        <div className='news-popup-container'>
            <div className="news-popup-icon" onClick={handleClick}>
                <span className="popup-text">뉴스</span>
                <i className="icon">📰</i> {/* 뉴스 아이콘 (이모지로 대체 가능) */}
            </div>
        </div>
    );
}

export default NewsCurationPopup;
