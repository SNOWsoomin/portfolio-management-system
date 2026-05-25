import React from 'react';
import './Header.css';

function Header({ setCurrentPage }) {
  return (
    <header className="main-header">
      <div className="logo-placeholder" onClick={() => setCurrentPage('main')}>
        <span className="logo-text">?</span>
      </div>
      <div className="search-bar-container">
        <input type="text" placeholder="Search" className="search-input" />
        <button className="search-clear-btn">×</button>
      </div>
      <nav className="header-nav">
        <button onClick={() => setCurrentPage('jobs')}>공고 확인하기</button>
        <button onClick={() => setCurrentPage('resume')}>내 이력서</button>
        <button>AI 첨삭</button>
      </nav>
      <div className="auth-buttons">
        <button className="login-btn">Login</button>
        <button className="mypage-btn">MyPage</button>
      </div>
    </header>
  );
}

export default Header;