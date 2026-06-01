import React from 'react';
import { Link } from 'react-router-dom';
import './Header.css';

function Header() {
  return (
    <header className="main-header">
      <Link to="/" className="logo-placeholder" style={{ textDecoration: 'none' }}>
        <span className="logo-text">AIfolio</span>
      </Link>
      <div className="search-bar-container">
        <input type="text" placeholder="Search" className="search-input" />
        <button className="search-clear-btn">×</button>
      </div>
      <nav className="header-nav">
        <Link to="/jobs"><button>공고 확인하기</button></Link>
        <Link to="/resume"><button>내 이력서</button></Link>
        <button>AI 첨삭</button>
      </nav>
      <div className="auth-buttons">
        <Link to="/login" className="login-btn">Login</Link>
        <Link to="/resume" className="mypage-btn">MyPage</Link>
      </div>
    </header>
  );
}

export default Header;