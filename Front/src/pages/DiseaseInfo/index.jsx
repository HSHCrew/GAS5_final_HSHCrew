import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import apiClient from '../../api/apiClient'; // apiClient 사용
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import './style.css';

import backIcon from '../../assets/left.svg';

const DiseaseInfo = () => {
    const { id } = useParams(); // URL에서 질병 ID 가져오기
    const [diseaseData, setDiseaseData] = useState(null);
    const [activeTab, setActiveTab] = useState('definition'); // 초기 탭을 'definition'으로 설정
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const navigate = useNavigate(); // 뒤로 가기 기능

    // 뒤로가기 핸들러
    const handleBackClick = () => {
        navigate(-1); // 이전 페이지로 이동
    };

    // 탭 전환 핸들러
    const handleTabClick = (tab) => {
        setActiveTab(tab);
    };

    // 질병 정보 API 호출
    useEffect(() => {
        const fetchDiseaseData = async () => {
            setLoading(true);
            setError(null);

            try {
                const response = await apiClient.get(`/altari/disease-info/${id}`);
                setDiseaseData(response.data);
            } catch (err) {
                setError('질병 정보를 가져오는 데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };

        fetchDiseaseData();
    }, [id]);

    return (
        <div className="disease-info-container">
            <div className="disease-info-header">
                <img
                    src={backIcon}
                    alt="Back Icon"
                    className="disease-info-back-arrow"
                    onClick={handleBackClick}
                />
                <div className="disease-info-header-text">
                    <p className="disease-info-name">{diseaseData?.diseaseName || '질병 이름'}</p>
                    <p className="disease-info-classification">
                        {diseaseData?.classification || '분류 정보 없음'}
                    </p>
                </div>
            </div>
            <div className="disease-info-tabs">
                <div
                    className={`disease-info-tab ${activeTab === 'definition' ? 'active' : ''}`}
                    onClick={() => handleTabClick('definition')}
                >
                    정의
                </div>
                <div
                    className={`disease-info-tab ${activeTab === 'medication' ? 'active' : ''}`}
                    onClick={() => handleTabClick('medication')}
                >
                    약물 주의사항
                </div>
                <div
                    className={`disease-info-tab ${activeTab === 'lifestyle' ? 'active' : ''}`}
                    onClick={() => handleTabClick('lifestyle')}
                >
                    생활 주의사항
                </div>
            </div>
            <div className="disease-info-content-container">
                {loading ? (
                    <div className="loading-spinner"></div>
                ) : error ? (
                    <p className="disease-info-error">{error}</p>
                ) : (
                    <div className="disease-info-content">
                        {activeTab === 'definition' && (
                            <>
                                <p className="disease-info-content-title">질병 정의</p>
                                <ReactMarkdown remarkPlugins={[remarkGfm]} className="markdown-container">
                                    {diseaseData?.diseaseDefinition || '정의 정보 없음'}
                                </ReactMarkdown>
                            </>
                        )}
                        {activeTab === 'medication' && (
                            <>
                                <p className="disease-info-content-title">약물 주의사항</p>
                                <ReactMarkdown remarkPlugins={[remarkGfm]} className="markdown-container">
                                    {diseaseData?.medicationAttention || '약물 주의사항 정보 없음'}
                                </ReactMarkdown>
                            </>
                        )}
                        {activeTab === 'lifestyle' && (
                            <>
                                <p className="disease-info-content-title">생활 주의사항</p>
                                <ReactMarkdown remarkPlugins={[remarkGfm]} className="markdown-container">
                                    {diseaseData?.lifeAttention || '생활 주의사항 정보 없음'}
                                </ReactMarkdown>
                            </>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

export default DiseaseInfo;