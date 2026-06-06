import React from 'react';
import { Link } from 'react-router-dom';
import { useNavigate } from 'react-router-dom';
import './Header.css';

function Header({ authUser, onLogout }) {
  const navigate = useNavigate();

  const openAiReview = () => {
    sessionStorage.setItem('openAiReview', 'true');
    window.dispatchEvent(new Event('open-ai-review'));
    navigate('/resume');
  };

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
        <button type="button" onClick={openAiReview}>AI 첨삭</button>
      </nav>
      <div className="auth-buttons">
        {authUser ? (
          <>
            <span className="login-btn">{authUser.userName} / {authUser.role}</span>
            <button type="button" className="mypage-btn" onClick={onLogout}>Logout</button>
          </>
        ) : (
          <Link to="/login" className="login-btn">Login</Link>
        )}
        <Link to="/resume" className="mypage-btn">MyPage</Link>
      </div>
    </header>
  );
}

export default Header;
