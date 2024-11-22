import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import './style.css';
import closeIconImg from '../../../assets/close.svg'; // X 아이콘 경로

const NewsCurationDetail = () => {
    const { id } = useParams();
    const navigate = useNavigate();

    const curationData = {
        1: {
            title: "조기 발병 비의존성 당뇨병과 치매 위험 증가",
            content: "NYU 로리 메이어스 간호대학의 연구에 따르면, 50세 이전에 비의존성 당뇨병 진단을 받은 환자는 치매 발병 위험이 두 배로 증가할 가능성이 있습니다. 진단 연령이 어릴수록 위험이 1.9%씩 증가하며, 비만이 이 위험을 더욱 높이는 것으로 나타났습니다. 이러한 연구 결과는 젊은 환자들에 대한 조기 개입과 생활습관 개선의 중요성을 강조합니다.",
            url: "https://medicalxpress.com/news/2024-11-earlier-diabetes-diagnosis-linked-dementia.html"
          },
          
          2: {
            title: "소득 수준에 따른 비의존성 당뇨병 사망률 격차",
            content: "JAMA Network Open에 발표된 연구에 따르면, 20~39세 비의존성 당뇨병 환자 중 저소득층은 고소득층에 비해 사망 위험이 약 3배 높은 것으로 나타났습니다. 이는 사회경제적 요인을 반영한 맞춤형 의료 접근과 건강 형평성을 위한 정책적 노력이 필요함을 보여줍니다.",
            url: "https://medicalxpress.com/news/2024-11-earlier-diabetes-diagnosis-linked-dementia.html"
          },
          
          3: {
            title: "티르제파타이드, 비의존성 당뇨병 예방과 체중 감량 효과",
            content: "The New England Journal of Medicine에 따르면, 티르제파타이드(Tirzepatide)는 비의존성 당뇨병으로의 진행을 억제하고 체중 감량을 동시에 제공하는 혁신적인 치료법으로 평가받고 있습니다. 이는 비만 및 전당뇨 환자에게 매우 유망한 선택지로 떠오르고 있으며, 환자 관리의 새로운 표준으로 자리잡을 가능성이 큽니다.",
            url: "https://medicalxpress.com/news/2024-11-earlier-diabetes-diagnosis-linked-dementia.html"
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
