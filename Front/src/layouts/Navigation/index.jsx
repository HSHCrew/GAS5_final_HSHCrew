import React, { useState } from 'react';
import './style.css';
import { Link } from 'react-router-dom';
import Popup from '../Popup/index.jsx';

import HomeIcon from '../../assets/Home.svg';
import MedicationIcon from '../../assets/Planner.svg';
import ChatbotIcon from '../../assets/Chatbot.svg';
import UserIcon from '../../assets/Person.svg';

const Navigation = () => {
    const [isPopupVisible, setPopupVisible] = useState(false);
 
    const togglePopup = () => {
       setPopupVisible(!isPopupVisible);
    };

   return (
       <nav className="navigation-bar">
           <div className="navigation-item">
               <Link to={'/home'}>
                   <img src={HomeIcon} alt="Home" className="navigation-icon" />
                   <p>홈</p>
               </Link>
           </div>
           <div className="navigation-item">
               <Link to={'/medicationManagement'}>
                   <img src={MedicationIcon} alt="Medication Management" className="navigation-icon" />
                   <p>복약관리</p>
               </Link>
           </div>
           <div className="navigation-item">
                <div className="navigation-plus-button" onClick={togglePopup}>
                    <span className="navigation-plus-icon">+</span>
                </div>
           </div>
           <div className="navigation-item">
               <Link to={'/chatting'}>
                   <img src={ChatbotIcon} alt="Chatbot" className="navigation-icon" />
                   <p>챗봇</p>
               </Link>
           </div>
           <div className="navigation-item">
               <Link to={'/setting'}>
                   <img src={UserIcon} alt="User" className="navigation-icon" />
                   <p>내정보</p>
               </Link>
           </div>
            {/* Popup 컴포넌트 호출 */}
           <Popup isVisible={isPopupVisible} closePopup={() => setPopupVisible(false)} />
       </nav>
   );
};

export default Navigation;
