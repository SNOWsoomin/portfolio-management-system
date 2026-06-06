import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Footer.css';

function Footer() {
  const navigate = useNavigate();

  const openAiReview = () => {
    sessionStorage.setItem('openAiReview', 'true');
    window.dispatchEvent(new Event('open-ai-review'));
    navigate('/resume');
  };

  return (
    <footer className="main-footer">
      <div className="footer-links">
        <Link to="/jobs"><button>공고 확인하기</button></Link>
        <Link to="/resume"><button>내 이력서</button></Link>
        <button type="button" onClick={openAiReview}>AI 첨삭</button>
      </div>
    </footer>
  );
}

export default Footer;
