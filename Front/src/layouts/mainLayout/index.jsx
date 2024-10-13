import './style.css';
import { Outlet } from 'react-router-dom';
import LayoutContainer from '../LayoutContainer.jsx';
import { MainHeader } from '../headers/MainHeader.jsx';
import Navigation from '../Navigation/index.jsx';

const MainLayout = () => {
    return (
        <LayoutContainer>
            <MainHeader />
            <section className={'main-container'}>
                <Outlet />
            </section>
            <Navigation />
        </LayoutContainer>
    );
};

export default MainLayout;
