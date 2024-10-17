import { UserProfile } from '../../components/UserProfile.jsx';
import './style.css';
import {Logo} from "../../components/Logo.jsx";

export const MainHeader = () => {
    return (
        <div className={'header-container'}>
            <div className={'header-left-container'}>
                <Logo />
            </div>
            <div className={'header-right-container'}>
                <UserProfile />
            </div>
        </div>
    );
};
