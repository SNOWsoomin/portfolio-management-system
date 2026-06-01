import React from 'react';
import { Link } from 'react-router-dom';
import './Footer.css';

function Footer() {
  return (
    <footer className="main-footer">
      <div className="footer-links">
        <Link to="/jobs"><button>공고 확인하기</button></Link>
        <Link to="/resume"><button>내 이력서</button></Link>
        <button>AI 첨삭</button>
      </div>
    </footer>
  );
}

export default Footer;