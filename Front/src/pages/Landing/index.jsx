import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const Landing = () => {
    const navigate = useNavigate();

    // 진입시 home으로 이동
    // 로그인 하지 않은 경우에 대한 처리 필요
    useEffect(() => {
        navigate('/home');
    }, []);
};

export default Landing;
