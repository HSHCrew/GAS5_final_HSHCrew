import React from 'react';
import { useNavigate } from 'react-router-dom';

import floatingIcon from '../../../assets/light.svg';
import './Choice.css';

const Choice = ({ nextStep }) => {
  const navigate = useNavigate(); // 페이지 이동을 위한 훅 사용

  // 건강 노트 작성하기 버튼 클릭 핸들러
  const handleNoteClick = () => {
    nextStep();
  };

  const passHealthNote = () => {
    const confirmed = window.confirm(
        '건강노트를 작성하지 않으시겠습니까? \n* 다음에 작성 가능합니다.'
    );
    if (confirmed) {
      navigate('/home');
    }
  };

  return (
      <>
        <img
            src={floatingIcon}
            alt="Floating Icon"
            className="floating-icon"
        />
        {/* 아이콘을 카드 안에 배치 */}
        <p className="card-text">
          <span>건강 노트를 작성해 주세요!</span>
          <br />
          <br />
          <span>
              건강노트를 작성해 주시면 복약 관련 주의 사항과 뉴스 큐레이션
              추천에서 도움을 드릴 수 있어요!
          </span>
        </p>
        <div className="button green-button" onClick={handleNoteClick}>
          {/* 버튼 클릭 시 페이지 이동 */}
          <p className="button-text">건강 노트 작성하기</p>
        </div>
        <div className="button green-button" onClick={passHealthNote}>
          <p className="button-text">홈으로 가기</p>
        </div>
      </>
  );
};

export default Choice;
