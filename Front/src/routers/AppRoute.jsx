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
import Choice from '../pages/HealthNote/Choice/Choice.jsx';
import Step1 from '../pages/HealthNote/Step1/Step1.jsx';
import Step2 from '../pages/HealthNote/Step2/Step2.jsx';
import Step3 from '../pages/HealthNote/Step3/Step3.jsx';
import Step4 from '../pages/HealthNote/Step4/Step4.jsx';
import Step5 from '../pages/HealthNote/Step5/Step5.jsx';
import Step6 from '../pages/HealthNote/Step6/Step6.jsx';
import Step7 from '../pages/HealthNote/Step7/Step7.jsx';
import Step8 from '../pages/HealthNote/Step8/Step8.jsx';
import Step9 from '../pages/HealthNote/Step9/Step9.jsx';


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
                key: 'Choice',
                name: 'Choice',
                element: <Choice />,  
                path: '/healthnote/choice',
            },
            {
                key: 'Step1',
                name: 'Step1',
                element: <Step1 />,
                path: '/healthnote/step1',
            },
            {
                key: 'Step2',
                name: 'Step2',
                element: <Step2 />,
                path: '/healthnote/step2',
            },
            {
                key: 'Step3',
                name: 'Step3',
                element: <Step3 />,
                path: '/healthnote/step3',
            },
            {
                key: 'Step4',
                name: 'Step4',
                element: <Step4 />,
                path: '/healthnote/step4',
            },
            {
                key: 'Step5',
                name: 'Step5',
                element: <Step5 />,
                path: '/healthnote/step5',
            },
            {
                key: 'Step6',
                name: 'Step6',
                element: <Step6 />,
                path: '/healthnote/step6',
            },
            {
                key: 'Step7',
                name: 'Step7',
                element: <Step7 />,
                path: '/healthnote/step7',
            },
            {
                key: 'Step8',
                name: 'Step8',
                element: <Step8 />,
                path: '/healthnote/step8',
            },
            {
                key: 'Step9',
                name: 'Step9',
                element: <Step9 />,
                path: '/healthnote/step9',
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
