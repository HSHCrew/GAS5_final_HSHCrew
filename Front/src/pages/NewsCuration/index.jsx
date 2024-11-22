import React from 'react';
import { useNavigate } from 'react-router-dom';
import './style.css';
import closeIconImg from '../../assets/close.svg'; // X 아이콘 경로

const NewsCurationList = () => {
    const navigate = useNavigate();

    const newsCurationData = [
        {
            id: 1,
            title: '조기 발병 비의존성 당뇨병과 치매 위험 증가',
            date: '2024-11-19',
            image: 'https://scx1.b-cdn.net/csz/news/800a/2018/1-diabetes.jpg',
        },
        {
            id: 2,
            title: '소득 수준에 따른 비의존성 당뇨병 사망률 격차',
            date: '2024-11-19',
            image: 'https://scx1.b-cdn.net/csz/news/800a/2024/diabetes-equipment.jpg',
        },
        {
            id: 3,
            title: '티르제파타이드, 비의존성 당뇨병 예방과 체중 감량 효과',
            date: '2024-11-19',
            image: 'https://scx1.b-cdn.net/csz/news/800a/2024/child-with-candy.jpg',
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
            <button className="news-close-button" onClick={handleCloseClick}>
                <img src={closeIconImg} alt="닫기" className="news-close-icon" />
            </button>

            <div className="news-curation-list">
                <h2>건강을 챙기는 뉴스 큐레이션</h2>
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
            </div>
        </div>
    );
};

export default NewsCurationList;
