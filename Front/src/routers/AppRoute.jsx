import { useCallback, useEffect, useMemo, useState } from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import Home from '../pages/Home/index.jsx';
import MainLayout from '../layouts/MainLayout/index.jsx';
import About from '../pages/About/index.jsx';
import Chatting from '../pages/Chatting/index.jsx';
import Setting from '../pages/Setting/index.jsx';
import Landing from '../pages/Landing/index.jsx';
import MedicationManagement from '../pages/MedicationManagement/index.jsx';
import RegistrationPrescription from '../pages/RegistrationPrescription/index.jsx';
import SignIn from "../pages/SignIn/index.jsx";
import SignUp from "../pages/SignUp/index.jsx";
import EmptyLayout from "../layouts/EmptyLayout/index.jsx";

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
            }
        ]
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
                            key={menu.key}
                        >
                            {createMenuRoutes(menu.childList)}
                        </Route>
                    ) : (
                        <Route
                            element={menu.element}
                            path={menu.path}
                            key={menu.key}
                        />
                    ),
                )}
            </>
        );
    }, []);

    const menuRoutes = useMemo(
        () => createMenuRoutes(menuList),
        [createMenuRoutes, menuList],
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
