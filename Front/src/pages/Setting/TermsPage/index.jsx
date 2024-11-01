import React from 'react';
import Header from '../../../components/Header';
import './style.css';

const TermsPage = () => {
  return (
    <div className="terms-container">
      <Header title="약관 보기" />
      
      <div className="terms-content">
        <section className="terms-section">
          <h2>1. 서문</h2>
          <p>
            본 약관은 HSH Crew (이하 "회사")가 제공하는 서비스의 이용과 관련된 조건과 내용을 규정합니다.
            본 약관에 동의함으로써, 사용자는 회사가 제공하는 서비스를 이용할 수 있습니다.
          </p>
        </section>

        <section className="terms-section">
          <h2>2. 용어 정의</h2>
          <p>
            "서비스"란 회사가 제공하는 웹사이트 및 모바일 애플리케이션을 통해 제공되는 모든 기능을 말합니다. <br />
            "회원"이란 본 약관에 동의하고 회사와 이용 계약을 체결한 자를 말합니다.
          </p>
        </section>

        <section className="terms-section">
          <h2>3. 서비스 이용 계약</h2>
          <p>
            회원은 회사가 정한 절차에 따라 본 약관에 동의하고 회원가입을 신청함으로써 이용 계약이 성립됩니다.
          </p>
        </section>

        <section className="terms-section">
          <h2>4. 개인정보 보호</h2>
          <p>
            회사는 회원의 개인정보를 보호하기 위해 최선을 다하며, 개인정보 처리와 관련한 자세한 내용은 회사의 개인정보 처리방침에 따릅니다.
          </p>
        </section>

        <section className="terms-section">
          <h2>5. 서비스의 제공 및 변경</h2>
          <p>
            회사는 회원에게 서비스를 제공하며, 필요한 경우 서비스의 전부 또는 일부를 변경할 수 있습니다.
          </p>
        </section>

        {/* 추가적인 약관 내용 섹션을 필요에 따라 추가 */}
        
        <footer className="terms-footer">
          <p>본 약관은 언제든지 변경될 수 있으며, 변경 시 공지를 통해 알려드립니다.</p>
          <p>© 2024 HSH Crew. All rights reserved.</p>
        </footer>
      </div>
    </div>
  );
};

export default TermsPage;
