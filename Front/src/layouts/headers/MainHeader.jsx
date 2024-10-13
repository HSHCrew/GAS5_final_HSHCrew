import { UserProfile } from '../../components/UserProfile.jsx';
import './style.css';

export const MainHeader = () => {
    return (
        <div className={'header-container'}>
            <div className={'header-left-container'}>LOGO</div>
            <div className={'header-right-container'}>
                <UserProfile />
            </div>
        </div>
    );
};
