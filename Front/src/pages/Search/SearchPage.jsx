import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../../api/apiClient'; // apiClient 사용
import './SearchPage.css';

import searchIcon from '../../assets/search.svg';
import backIcon from '../../assets/left.svg';
import thermometerIcon from '../../assets/thermometer.svg'; // 질병 탭에 사용할 아이콘 추가

const SearchPage = () => {
    const [activeTab, setActiveTab] = useState('medicine'); // 초기 활성화된 탭을 'medicine'으로 설정
    const [searchQuery, setSearchQuery] = useState(''); // 검색어 상태
    const [results, setResults] = useState([]); // 검색 결과 상태
    const [loading, setLoading] = useState(false); // 로딩 상태
    const [error, setError] = useState(null); // 에러 상태
    const navigate = useNavigate(); // 뒤로 가기 기능

    // 탭 전환 핸들러
    const handleTabClick = (tab) => {
        setActiveTab(tab);
        setResults([]); // 탭 변경 시 검색 결과 초기화
        setSearchQuery(''); // 검색어 초기화
    };

    // 뒤로가기 핸들러
    const handleBackClick = () => {
        navigate(-1); // 이전 페이지로 이동
    };

    // 검색 결과 API 호출
    const fetchResults = async () => {
        if (!searchQuery) return; // 검색어가 없으면 호출하지 않음
        setLoading(true);
        setError(null); // 이전 에러 상태 초기화
        const endpoint = activeTab === 'disease' ? '/altari/disease/list' : '/altari/drug/list';

        try {
            const response = await apiClient.get(endpoint);
            const filteredResults = response.data.filter((item) =>
                (activeTab === 'disease' ? item.diseaseName : item.medicationName)
                    .toLowerCase()
                    .includes(searchQuery.toLowerCase())
            );
            setResults(filteredResults);
        } catch (err) {
            setError('검색 결과를 가져오는 데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 디바운스를 위한 타이머
    useEffect(() => {
        const timer = setTimeout(() => {
            fetchResults();
        }, 300); // 300ms 대기 후 API 호출

        return () => clearTimeout(timer); // 타이머 클리어
    }, [searchQuery, activeTab]);

    // 검색어 입력 핸들러
    const handleInputChange = (event) => {
        setSearchQuery(event.target.value);
    };

    // 검색어 초기화 핸들러
    const handleClearSearch = () => {
        setSearchQuery(''); // 검색어 초기화
        setResults([]); // 결과 초기화
    };

    // 결과 클릭 시 상세 페이지로 이동
    const handleResultClick = (id) => {
        const detailPath = activeTab === 'disease' ? `/diseaseinfo/${id}` : `/medicineinfo/${id}`;
        navigate(detailPath); // 상세 페이지로 이동
    };

    return (
        <div className="searchpage-container">
            <div className="searchpage-header">
                <img
                    src={backIcon}
                    alt="Back Icon"
                    className="searchpage-back-button"
                    onClick={handleBackClick}
                />
                <p className="searchpage-title">검색</p>
            </div>
            <div className="searchpage-tabs">
                <div
                    className={`searchpage-tab ${activeTab === 'medicine' ? 'active' : ''}`}
                    onClick={() => handleTabClick('medicine')}
                >
                    약
                </div>
                <div
                    className={`searchpage-tab ${activeTab === 'disease' ? 'active' : ''}`}
                    onClick={() => handleTabClick('disease')}
                >
                    질병
                </div>
            </div>
            <div className="searchpage-bar">
                <img src={searchIcon} alt="Search Icon" className="searchpage-icon" />
                <input
                    type="text"
                    placeholder={`${activeTab === 'disease' ? '질병 검색' : '약품 검색'}`}
                    className="searchpage-input"
                    value={searchQuery}
                    onChange={handleInputChange}
                />
                {searchQuery && (
                    <button className="searchpage-clear-button" onClick={handleClearSearch}>
                        X
                    </button>
                )}
            </div>
            <div className="searchpage-results">
            {loading ? (
                    <div className="loading-spinner"></div>
                ) : error ? (
                    <p className="searchpage-error">{error}</p>
                ) : results.length > 0 ? (
                    <ul className="searchpage-result-list">
                        {results.map((result) => (
                            <li
                                key={activeTab === 'disease' ? result.diseaseId : result.medicationId}
                                className="searchpage-result-item"
                                onClick={() =>
                                    handleResultClick(
                                        activeTab === 'disease' ? result.diseaseId : result.medicationId
                                    )
                                }
                            >
                                {activeTab === 'disease' ? (
                                    <div className="searchpage-disease-content">
                                        <img
                                            src={thermometerIcon}
                                            alt="Thermometer Icon"
                                            className="searchpage-disease-icon"
                                        />
                                        <span className="searchpage-disease-text">{result.diseaseName}</span>
                                    </div>
                                ) : (
                                    <div className="searchpage-result-content">
                                        {result.itemImage ? (
                                            <img
                                                src={result.itemImage}
                                                alt={result.medicationName}
                                                className="searchpage-result-image-horizontal"
                                            />
                                        ) : (
                                            <div className="searchpage-placeholder-image-horizontal">이미지 없음</div>
                                        )}
                                        <span className="searchpage-result-text">{result.medicationName}</span>
                                    </div>
                                )}
                            </li>
                        ))}
                    </ul>
                ) : (
                    <p>검색 결과가 없습니다.</p>
                )}

            </div>
        </div>
    );
};

export default SearchPage;
