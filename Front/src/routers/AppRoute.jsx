import { useCallback, useEffect, useMemo, useState } from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';

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
import OnMedication from '../pages/Prescription/OnMedication/index.jsx'
import EndMedication from '../pages/Prescription/EndMedication/index.jsx'


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
                path: '/medicineinfo',
            },
            {
                key: 'OnMedication',
                name: 'OnMedication',
                element: <OnMedication />,
                path: '/onMedication',
            },
            {
                key: 'EndMedication',
                name: 'EndMedication',
                element: <EndMedication />,
                path: '/endMedication',
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
];

const AppRoute = () => {
    const [menuList, setMenuList] = useState([]);

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
        <BrowserRouter>
            <Routes>{menuList.length && menuRoutes}</Routes>
        </BrowserRouter>
    );
};

export default AppRoute;
