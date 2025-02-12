import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import apiClient from '../../../api/apiClient';
import './style.css';
import closeIconImg from '../../../assets/close.svg';

const NewsCurationDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [curation, setCuration] = useState(null);
    const [loading, setLoading] = useState(true);
    const username = localStorage.getItem('username') || sessionStorage.getItem('username');

    useEffect(() => {
        fetchCurationDetail();
    }, [id]);

    const fetchCurationDetail = async () => {
        try {
            const response = await apiClient.get(`/altari/newsCuration/${username}`);
            const allCurations = response.data;
            
            // 전체 큐레이션 목록에서 해당 ID의 큐레이션 찾기
            const selectedCuration = allCurations.find(item => item.newsCurationId === parseInt(id));
            
            if (selectedCuration) {
                // API 응답 데이터를 UI에 맞게 변환
                const formattedCuration = {
                    title: selectedCuration.koreanCurationContent.split('\n')[0].replace('Title: ', ''),
                    content: selectedCuration.koreanCurationContent.split('\n').slice(1).join('\n'), // 제목을 제외한 내용
                    url: selectedCuration.url,
                    disease: selectedCuration.disease,
                    keyword: selectedCuration.keyword,
                    generatedAt: selectedCuration.generatedAt
                };
                
                setCuration(formattedCuration);
            }
        } catch (error) {
            console.error('큐레이션 상세 정보 가져오기 실패:', error);
            if (error.response) {
                console.error('서버 응답:', error.response.data);
            }
        } finally {
            setLoading(false);
        }
    };

    const handleCloseClick = () => {
        navigate(-1);
    };

    if (loading) {
        return <div className="loading-container">로딩 중...</div>;
    }

    if (!curation) {
        return <div className="not-found-container">큐레이션을 찾을 수 없습니다.</div>;
    }

    return (
        <div className="news-curation-detail-container">
            <button className="news-curation-detail-close-button" onClick={handleCloseClick}>
                <img src={closeIconImg} alt="닫기" className="news-curation-detail-close-icon" />
            </button>

            <div className="news-curation-detail">
                <h2>{curation.title}</h2>
                {curation.generatedAt && (
                    <p className="generation-date">
                        작성일: {new Date(curation.generatedAt).toLocaleDateString()}
                    </p>
                )}
                <div className="content">
                    {curation.content.split('\n').map((paragraph, index) => (
                        paragraph.trim() && <p key={index}>{paragraph}</p>
                    ))}
                </div>
                {curation.url && (
                    <div className="news-curation-link-wrapper">
                        <span className="reference-text">참고문헌: </span>
                        <a href={curation.url} target="_blank" rel="noopener noreferrer" className="news-curation-link">
                            {curation.url}
                        </a>
                    </div>
                )}
                {curation.disease && (
                    <div className="disease-info">
                        <h3>관련 질병 정보</h3>
                        <p><strong>질병명:</strong> {curation.disease.diseaseName}</p>
                        <p><strong>정의:</strong> {curation.disease.diseaseDefinition}</p>
                    </div>
                )}
            </div>
        </div>
    );
};

export default NewsCurationDetail;