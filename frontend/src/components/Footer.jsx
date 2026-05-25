import React from 'react';
import './Footer.css';

function Footer({ setCurrentPage }) {
  return (
    <footer className="main-footer">
      <div className="footer-links">
        <button onClick={() => setCurrentPage('jobs')}>공고 확인하기</button>
        <button onClick={() => setCurrentPage('resume')}>내 이력서</button>
        <button>AI 첨삭</button>
      </div>
    </footer>
  );
}

export default Footer;