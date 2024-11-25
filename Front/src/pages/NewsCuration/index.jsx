import React from 'react';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './style.css';
import apiClient from '../../api/apiClient';
import closeIconImg from '../../assets/close.svg'; // X 아이콘 경로

const NewsCurationList = () => {
    const navigate = useNavigate();
    const [newsCurationData, setNewsCurationData] = useState([]);
    const [loading, setLoading] = useState(true);
    const username = localStorage.getItem('username') || sessionStorage.getItem('username');

    useEffect(() => {
        if (username) {
            fetchNewsCurationData();
        } else {
            console.error('사용자 정보를 찾을 수 없습니다.');
            navigate('/signIn');
        }
    }, [username, navigate]);

    const fetchNewsCurationData = async () => {
        try {
            setLoading(true); // 데이터 로딩 시작
            const response = await apiClient.get(`/altari/newsCuration/${username}`);
            
            const formattedData = response.data.map(item => ({
                id: item.newsCurationId,
                title: item.koreanCurationContent.split('\n')[0].replace('Title: ', ''),
                summary: item.disease?.diseaseDefinition || '건강 관련 큐레이션 컨텐츠',
                date: new Date(item.generatedAt).toISOString().split('T')[0],
                image: getImageByDisease(item.disease?.diseaseName)
            }));
            
            setNewsCurationData(formattedData);
        } catch (error) {
            console.error('뉴스 큐레이션 데이터 가져오기 실패:', error);
            if (error.response) {
                console.error('서버 응답:', error.response.data);
            }
        } finally {
            setLoading(false); // 데이터 로딩 완료
        }
    };

    // 질병별 이미지 매핑 함수
    const getImageByDisease = (diseaseName) => {
        const imageMap = {
            '당뇨병': 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=800',
            // 다른 질병들에 대한 이미지 매핑 추가
            'default': 'https://cdn.jamanetwork.com/UI/app/img/covers/psych.jpg'
        };
        return imageMap[diseaseName] || imageMap.default;
    };

    const handleCurationClick = (id) => {
        navigate(`/news-curation/${id}`);
    };

    const handleCloseClick = () => {
        navigate(-1);
    };

    return (
        <div className="news-curation-container">
            <button className="news-close-button" onClick={handleCloseClick}>
                <img src={closeIconImg} alt="닫기" className="news-close-icon" />
            </button>
    
            <div className="news-curation-list">
                <h2>건강을 챙기는 뉴스 큐레이션</h2>
                {loading ? (
                    <div className="loading-container">
                        <div className="loading-spinner"></div>
                        <p>뉴스 큐레이션을 불러오는 중입니다...</p>
                    </div>
                ) : (
                    <ul>
                        {newsCurationData.map((curation) => (
                            <li key={curation.id} onClick={() => handleCurationClick(curation.id)}>
                                <div className="news-image" style={{ backgroundImage: `url(${curation.image})` }}></div>
                                <div className="news-content">
                                    <h3>{curation.title}</h3>
                                    <p>{curation.summary}</p>
                                    <span className="news-date">{curation.date}</span>
                                </div>
                            </li>
                        ))}
                    </ul>
                )}
            </div>
        </div>
    );
};

export default NewsCurationList;