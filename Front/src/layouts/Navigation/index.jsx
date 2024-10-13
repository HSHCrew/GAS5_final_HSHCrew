import './style.css';
import { Link } from 'react-router-dom';

const Navigation = () => {
    return (
        <nav
            style={{
                position: 'relative',
                backgroundColor: 'inherit',
                height: '70px', // safe-area-inset-bottom 높이 적절하게 조절
            }}
        >
            <div className={'navigation'}>
                <div>
                    <Link to={'/home'}>홈</Link>
                </div>
                <div>
                    <Link to={'/medicationManagement'}>복약관리</Link>
                </div>
                <div>
                    <Link to={'/registrationPrescription'}>+</Link>
                </div>
                <div>
                    <Link to={'/chatting'}>챗봇</Link>
                </div>
                <div>
                    <Link to={'/setting'}>설정</Link>
                </div>
            </div>
        </nav>
    );
};

export default Navigation;
