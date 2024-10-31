import './style.css';
import { Link } from 'react-router-dom';
import HomeIcon from '../../assets/Home.svg';
import MedicationIcon from '../../assets/Planner.svg';
import ChatbotIcon from '../../assets/Chatbot.svg';
import UserIcon from '../../assets/Person.svg';

const Navigation = () => {
   return (
       <nav className="navigation-bar">
           <div className="navigation-item">
               <Link to={'/home'}>
                   <img src={HomeIcon} alt="Home" className="icon" />
                   <p>홈</p>
               </Link>
           </div>
           <div className="navigation-item">
               <Link to={'/medicationManagement'}>
                   <img src={MedicationIcon} alt="Medication Management" className="icon" />
                   <p>복약관리</p>
               </Link>
           </div>
           <div className="navigation-item">
               <Link to={'/registrationPrescription'}>
                   <div className="plus-button">
                       <span className="plus-icon">+</span>
                   </div>
               </Link>
           </div>
           <div className="navigation-item">
               <Link to={'/chatting'}>
                   <img src={ChatbotIcon} alt="Chatbot" className="icon" />
                   <p>챗봇</p>
               </Link>
           </div>
           <div className="navigation-item">
               <Link to={'/setting'}>
                   <img src={UserIcon} alt="User" className="icon" />
                   <p>내정보</p>
               </Link>
           </div>
       </nav>
   );
};

export default Navigation;
