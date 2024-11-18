import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import './style.css';
import closeIconImg from '../../../assets/close.svg'; // X 아이콘 경로

const NewsCurationDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const curationData = {
        1: { 
            title: '건강한 식습관, 이렇게 시작하세요!', 
            content: `새해 결심을 위한 건강한 식단 가이드입니다. 올바른 식습관은 건강한 삶을 유지하는 데 필수적입니다. 
                      이 큐레이션에서는 신선한 채소와 과일의 중요성, 균형 잡힌 식단을 유지하는 방법, 정크 푸드 피하기 등을 포함한 
                      다양한 건강한 식습관을 소개합니다.` 
        },
        2: { 
            title: '2024년 꼭 알아야 할 운동 트렌드', 
            content: `운동 트렌드는 매년 새롭게 변하고 있으며, 2024년에도 새로운 운동 방식이 주목받고 있습니다. 
                      HIIT(고강도 인터벌 트레이닝)와 같은 운동이 많은 인기를 끌고 있으며, 웨어러블 디바이스를 활용한 
                      맞춤형 운동 계획 또한 중요한 트렌드입니다. 이 큐레이션에서는 최신 피트니스 운동과 장비에 대해 소개합니다.` 
        },
        3: { 
            title: '마음 챙김과 스트레스 관리의 중요성', 
            content: `현대 사회에서 스트레스 관리와 마음 챙김은 필수적입니다. 이 큐레이션에서는 간단한 명상법과 
                      일상에서 마음을 안정시키는 방법을 다룹니다. 일상에서 실천할 수 있는 마음 챙김 기술은 스트레스 
                      완화뿐만 아니라 심리적 안정감을 제공합니다.` 
        },
    };
    
    const curation = curationData[id];

    if (!curation) {
        return <p>큐레이션을 찾을 수 없습니다.</p>;
    }

    const handleCloseClick = () => {
        navigate(-1); // 뒤로가기
    };

    return (
        <div className="news-curation-detail-container">
            {/* 닫기 버튼을 상단 오른쪽 외곽에 배치 */}
            <button className="news-curation-detail-close-button" onClick={handleCloseClick}>
                <img src={closeIconImg} alt="닫기" className="news-curation-detail-close-icon" />
            </button>

            <div className="news-curation-detail">
                <h2>{curation.title}</h2>
                <p>{curation.content}</p>
            </div>
        </div>
    );
};

export default NewsCurationDetail;
