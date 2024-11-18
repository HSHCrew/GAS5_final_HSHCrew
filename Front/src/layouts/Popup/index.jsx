import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './style.css';

const Popup = ({ isVisible, closePopup }) => {
   if (!isVisible) return null;

   // 팝업 외부 클릭 시 닫힘
   useEffect(() => {
      const handleClickOutside = (event) => {
         if (event.target.classList.contains('popup-overlay')) {
            closePopup();
         }
      };
      document.addEventListener('click', handleClickOutside);
      return () => {
         document.removeEventListener('click', handleClickOutside);
      };
   }, [closePopup]);

   const navigate = useNavigate();

   const handleregistrationPrescription = () => {
      closePopup(); // 팝업 닫기
      navigate('/registrationPrescription'); // 페이지 이동
   };

   return (
      <div className="popup-overlay">
         <div className="popup" onClick={(e) => e.stopPropagation()}>
            <div className="popup-arrow"></div>
            <div className="popup-content" onClick={handleregistrationPrescription}>
               처방전 추가하기 ε＝┏(･ω･)┛
            </div>
         </div>
      </div>
   );
};

export default Popup;
