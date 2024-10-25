import { Logo } from '../../components/Logo.jsx';
import Searchbar from '../../components/Searchbar.jsx'; // Searchbar 컴포넌트 임포트
import { useNavigate } from 'react-router-dom';
import './style.css';

export const MainHeader = () => {
    const navigate = useNavigate();

    const handleSearchClick = () => {
        navigate('/searchpage');
    };

    return (
        <div className={'header-container'}>
            <div className={'header-left-container'}>
                <Logo />
            </div>
            <div className={'header-right-container'} onClick={handleSearchClick}>
                <Searchbar /> {/* Searchbar 컴포넌트 사용 */}
            </div>
        </div>
    );
};
