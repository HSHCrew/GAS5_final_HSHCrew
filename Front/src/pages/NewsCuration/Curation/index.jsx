import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import './style.css';
import closeIconImg from '../../../assets/close.svg'; // X 아이콘 경로

const NewsCurationDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const curationData = {
        1: { 
            title: '알코올 사용 장애 치료에서 GLP-1 약물의 가능성',
            content: `
                비만과 당뇨병 치료제인 GLP-1 계열 약물(세마글루타이드, 리라글루타이드)이 알코올 사용 장애(AUD) 치료에도 효과를 보였다는 연구 결과입니다.
                
                주요 발견:
                - 세마글루타이드: 알코올 관련 입원 위험 36% 감소, 신체 질환 관련 입원 22% 감소.
                - 리라글루타이드: 알코올 관련 입원 위험 28% 감소, 신체 질환 관련 입원 21% 감소.
                - 기존 AUD 치료제인 날트렉손 대비 더 높은 효과를 보였습니다.
                
                이 연구는 AUD 치료에 새로운 가능성을 제시하며, 향후 추가 연구가 필요함을 강조합니다.
            `,
            url: 'https://jamanetwork.com/journals/jamapsychiatry/fullarticle/10.1001/jamapsychiatry.2024.3599'
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
                {curation.url && (
                    <div className="news-curation-link-wrapper">
                        <span className="reference-text">참고문헌: </span>
                        <a href={curation.url} target="_blank" rel="noopener noreferrer" className="news-curation-link">
                            {curation.url}
                        </a>
                    </div>
                )}
            </div>
        </div>
    );
};

export default NewsCurationDetail;
