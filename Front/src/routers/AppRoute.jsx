import { useCallback, useEffect, useMemo, useState } from 'react';
import { BrowserRouter, Route, Routes, useLocation } from 'react-router-dom';

import FooterOnlyLayout from '../layouts/FooterOnlyLayout/index.jsx';
import EmptyLayout from '../layouts/EmptyLayout/index.jsx';
import MainLayout from '../layouts/MainLayout/index.jsx';
import Chatting from '../pages/Chatting/index.jsx';
import HealthNote from '../pages/HealthNote/index.jsx';
import Home from '../pages/Home/index.jsx';
import Landing from '../pages/Landing/index.jsx';
import MedicationManagement from '../pages/MedicationManagement/index.jsx';
import RegistrationPrescription from '../pages/RegistrationPrescription/index.jsx';
import Setting from '../pages/Setting/index.jsx';
import SignIn from '../pages/SignIn/index.jsx';
import SignUp from '../pages/SignUp/index.jsx';
import Search from '../pages/Search/SearchPage.jsx';
import MedicineInfo from '../pages/MedicineInfo/index.jsx';
import DiseaseInfo from '../pages/DiseaseInfo/index.jsx';
import PrescriptionDetail from '../pages/Prescription/index.jsx';
import UserInfo from '../pages/Setting/UserInfo/index.jsx';
import HealthNoteProfile from '../pages/Setting/HealthNoteProfile/index.jsx';
import TermsPage from '../pages/Setting/TermsPage/index.jsx';
import SetAlarm from '../pages/Setting/SetAlarm/index.jsx';
import NewsCurationPopup from '../layouts/Newsletter/index.jsx';
import NewsCurationList from '../pages/NewsCuration/index.jsx';
import NewsCurationDetail from '../pages/NewsCuration/Curation/index.jsx';

// KakaoCallback 컴포넌트를 추가로 임포트
import KakaoCallback from '../pages/KakaoCallback';

const staticMenuRoute = [
    {
        key: 'MobileLayout',
        name: 'MobileLayout',
        element: <EmptyLayout />,
        path: null,
        childList: [
            {
                key: 'Landing',
                name: 'Landing',
                element: <Landing />,
                path: '/',
            },
            {
                key: 'SignIn',
                name: 'SignIn',
                element: <SignIn />,
                path: '/signIn',
            },
            {
                key: 'SignUp',
                name: 'SignUp',
                element: <SignUp />,
                path: '/signUp',
            },
            {
                key: 'KakaoCallback', // 카카오 콜백 경로 추가
                name: 'KakaoCallback',
                element: <KakaoCallback />,
                path: '/kakao/callback',
            },
            {
                key: 'HealthNote',
                name: 'HealthNote',
                element: <HealthNote />,
                path: '/healthNote',
            },
            {
                key: 'Searchpage',
                name: 'Searchpage',
                element: <Search />,
                path: '/searchpage',
            },
            {
                key: 'MedicineInfo',
                name: 'MedicineInfo',
                element: <MedicineInfo />,
                path: '/medicineinfo/:id',
            },
            {
                key: 'DiseaseInfo',
                name: 'DiseaseInfo',
                element: <DiseaseInfo />,
                path: '/diseaseinfo/:id',
            },
            {
                key: 'PrescriptionDetail',
                name: 'PrescriptionDetail',
                element: <PrescriptionDetail />,
                path: '/prescriptionDetail/:id',
            },
            {
                key: 'UserInfo',
                name: 'UserInfo',
                element: <UserInfo />,
                path: '/userInfo',
            },
            {
                key: 'HealthNoteProfile',
                name: 'HealthNoteProfile',
                element: <HealthNoteProfile />,
                path: '/healthNoteProfile',
            },
            {
                key: 'TermsPage',
                name: 'TermsPage',
                element: <TermsPage />,
                path: '/termsPage',
            },
            {
                key: 'SetAlarm',
                name: 'SetAlarm',
                element: <SetAlarm />,
                path: '/setAlarm',
            },
            {
                key: 'NewsCurationList',
                name: 'NewsCurationList',
                element: <NewsCurationList />,
                path: '/news-curation',
            },
            {
                key: 'NewsCurationDetail',
                name: 'NewsCurationDetail',
                element: <NewsCurationDetail />,
                path: '/news-curation/:id',
            },
        ],
    },
    {
        key: 'MobileLayout',
        name: 'MobileLayout',
        element: <MainLayout />,
        path: null,
        childList: [
            {
                key: 'Home',
                name: 'Home',
                element: <Home />,
                path: '/home',
            },
            {
                key: 'MedicationManagement',
                name: 'MedicationManagement',
                element: <MedicationManagement />,
                path: '/medicationManagement',
            },
            {
                key: 'RegistrationPrescription',
                name: 'RegistrationPrescription',
                element: <RegistrationPrescription />,
                path: '/registrationPrescription',
            },
            {
                key: 'Chatting',
                name: 'Chatting',
                element: <Chatting />,
                path: '/chatting',
            },
            {
                key: 'Setting',
                name: 'Setting',
                element: <Setting />,
                path: '/setting',
            },
        ],
    },
    {
        key: 'FooterOnlyLayout',
        name: 'FooterOnlyLayout',
        element: <FooterOnlyLayout />,
        path: null,
        childList: [
            {
                key: 'UserInfo',
                name: 'UserInfo',
                element: <UserInfo />,
                path: '/userInfo',
            },
        ],
    },
];

const AppRoute = () => {
    const [menuList, setMenuList] = useState([]);
    const location = useLocation();

    const createMenuRoutes = useCallback((menus) => {
        return (
            <>
                {menus.map((menu) =>
                    menu?.childList?.length ? (
                        <Route
                            element={menu.element}
                            path={menu.path}
                            key={menu.key}>
                            {createMenuRoutes(menu.childList)}
                        </Route>
                    ) : (
                        <Route
                            element={menu.element}
                            path={menu.path}
                            key={menu.key}
                        />
                    )
                )}
            </>
        );
    }, []);

    const menuRoutes = useMemo(
        () => createMenuRoutes(menuList),
        [createMenuRoutes, menuList]
    );

    const getMenuList = useCallback(() => {
        setMenuList(staticMenuRoute);
    }, []);

    useEffect(() => {
        getMenuList();
    }, [getMenuList, setMenuList]);
    
    return (
        <>
            <Routes>{menuList.length && menuRoutes}</Routes>
            {location.pathname === '/home' && <NewsCurationPopup />}
        </>
    );
};

export default AppRoute;
