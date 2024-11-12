import React from 'react';
import Navigation from '../Navigation';

const FooterOnlyLayout = ({ children }) => {
    return (
        <div className="footer-only-layout">
            <main className="content">{children}</main>
            <Navigation /> {/* 하단 네비게이션만 표시 */}
        </div>
    );
};

export default FooterOnlyLayout;
