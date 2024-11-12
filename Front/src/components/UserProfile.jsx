import { useEffect, useState } from 'react';
import UserProfile_default from '../assets/UserProfile_default.png';
import { StyledImage } from './StyledTag.jsx';
import './style.css';

export const UserProfile = () => {
    const [userName, setUserName] = useState('');

    useEffect(() => {
        setUserName('사용자이름');
    }, []);

    return (
        <div className={'user-profile-container'}>
            <div className={'user-name'}>{userName}</div>
            <div className={'profile-image-container'}>
                <StyledImage
                    src={UserProfile_default}
                    alt={'User Profile'}
                    height={'100%'}
                    width={''}
                />
            </div>
        </div>
    );
};
