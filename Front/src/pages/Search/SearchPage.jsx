import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom'; // 뒤로 가기 기능을 위한 훅
import './SearchPage.css';

import searchIcon from '../../assets/search.svg';
import backIcon from '../../assets/left.svg'; // 왼쪽 화살표 이미지 임포트

const SearchPage = () => {
    const [activeTab, setActiveTab] = useState('disease'); // 현재 활성화된 탭 상태
    const navigate = useNavigate(); // 뒤로 가기 기능을 위한 훅

    const handleTabClick = (tab) => {
        setActiveTab(tab); // 탭 클릭 시 상태 변경
    };

    const handleBackClick = () => {
        navigate(-1); // 뒤로 가기 (이전 페이지로 이동)
    };

    return (
        <div className="searchpage-container">
            <div className="searchpage-header">
                <img
                    src={backIcon}
                    alt="Back Icon"
                    className="searchpage-back-button"
                    onClick={handleBackClick} // 클릭 시 뒤로 가기
                />
                <p className="searchpage-title">검색</p>
            </div>
            <div className="searchpage-tabs">
                <div
                    className={`searchpage-tab ${activeTab === 'disease' ? 'active' : ''}`}
                    onClick={() => handleTabClick('disease')}
                >
                    질병
                </div>
                <div
                    className={`searchpage-tab ${activeTab === 'medicine' ? 'active' : ''}`}
                    onClick={() => handleTabClick('medicine')}
                >
                    약품
                </div>
            </div>
            <div className="searchpage-bar">
                <img src={searchIcon} alt="Search Icon" className="searchpage-icon" />
                <input
                    type="text"
                    placeholder={`${activeTab === 'disease' ? '질병 검색' : '약품 검색'}`}
                    className="searchpage-input"
                />
            </div>
            <div className="searchpage-results">
                <p>검색결과 <span className="searchpage-highlight">0</span></p>
            </div>
        </div>
    );
};

export default SearchPage;
