import React from 'react';
import { useNavigate } from 'react-router-dom';
import './style.css';
import closeIconImg from '../../assets/close.svg'; // X 아이콘 경로

const NewsCurationList = () => {
    const navigate = useNavigate();

    const newsCurationData = [
        {
            id: 1,
            title: '건강한 식습관, 이렇게 시작하세요!',
            summary: '새해 결심을 위한 건강한 식단 가이드.',
            date: '2024-01-01',
            tag: '건강식',
            image: 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=800',
        },
        {
            id: 2,
            title: '2024년 꼭 알아야 할 운동 트렌드',
            summary: '운동 트렌드와 최신 피트니스 가이드를 확인하세요.',
            date: '2024-01-15',
            tag: '운동',
            image: 'https://images.unsplash.com/photo-1579758629937-8a1e7efbaf7d?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=800',
        },
        {
            id: 3,
            title: '마음 챙김과 스트레스 관리의 중요성',
            summary: '건강한 마음을 위한 간단한 명상법.',
            date: '2024-01-20',
            tag: '마음챙김',
            image: 'https://images.unsplash.com/photo-1556761175-4b46a572b786?crop=entropy&cs=tinysrgb&fit=max&fm=jpg&q=80&w=800',
        },
    ];

    const handleCurationClick = (id) => {
        navigate(`/news-curation/${id}`);
    };

    const handleCloseClick = () => {
        navigate(-1); // 뒤로가기
    };

    return (
        <div className="news-curation-container">
            {/* 닫기 버튼을 news-curation-list 바깥에 배치 */}
            <button className="close-button" onClick={handleCloseClick}>
                <img src={closeIconImg} alt="닫기" className="close-icon" />
            </button>

            <div className="news-curation-list">
                <h2>건강을 챙기는 뉴스 큐레이션</h2>
                <ul>
                    {newsCurationData.map((curation) => (
                        <li key={curation.id} onClick={() => handleCurationClick(curation.id)}>
                            <div className="news-image" style={{ backgroundImage: `url(${curation.image})` }}></div>
                            <div className="news-content">
                                <span className="news-tag">{curation.tag}</span>
                                <h3>{curation.title}</h3>
                                <p>{curation.summary}</p>
                                <span className="news-date">{curation.date}</span>
                            </div>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
};

export default NewsCurationList;
