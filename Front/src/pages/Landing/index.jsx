import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {Logo} from "../../components/Logo.jsx";

const Landing = () => {
    const navigate = useNavigate();

    // 기존 로그인 된 경우에 대한 처리 필요
    useEffect(() => {
        setTimeout(() => navigate('/signIn'), 2000);
    }, []);

    return <div><Logo /></div>
};

export default Landing;
