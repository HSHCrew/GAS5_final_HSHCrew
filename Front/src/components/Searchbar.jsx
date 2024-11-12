import React from 'react';
import './Searchbar.css'; // 검색창에 대한 스타일 파일

import searchIcon from '../assets/search.svg'; // 이미지 파일 경로에 맞게 변경 필요

const Search = () => {
    return (
        <div className="search-bar">
            <img src={searchIcon} alt="Search" className="search-icon" />
            <input
                type="text"
                placeholder="질병, 약 검색"
                className="search-input"
            />
        </div>
    );
};

export default Search;
