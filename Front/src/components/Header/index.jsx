import React from 'react';
import { useNavigate } from 'react-router-dom';
import backIcon from '../../assets/left.svg';
import './style.css';

const Header = ({ title }) => {
  const navigate = useNavigate();

  const handleBackClick = () => {
    navigate(-1); // 뒤로 가기
  };

  return (
    <header className="header">
      <img
        src={backIcon}
        alt="Back Icon"
        className="back-button"
        onClick={handleBackClick}
      />
      <p className="header-title">{title}</p>
    </header>
  );
};

export default Header;
