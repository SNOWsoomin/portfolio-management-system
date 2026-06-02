import React from 'react';
import './Resume.css';

function Resume() {
  return (
    <div className="resume-page">
      <div className="resume-profile-card">
        <button className="add-info-btn">+</button>
        
        <div className="resume-header-flex">
          <div className="resume-avatar-box">👤</div>
          
          <div className="resume-details-grid">
            <div className="info-row">
              <span className="info-label">이름</span>
              <div className="info-content-blank"></div>
            </div>
            <div className="info-row">
              <span className="info-label">나이</span>
              <div className="info-content-blank"></div>
            </div>
            <div className="info-row">
              <span className="info-label">학력</span>
              <div className="info-tags-group">
                <span className="tag-type">✓ OO대학교 졸업 ×</span>
                <span className="tag-type">✓ OO고등학교 졸업 ×</span>
                <span className="tag-type">✓ OO대학원 석사 졸업 ×</span>
              </div>
            </div>
            <div className="info-row">
              <span className="info-label">경력</span>
              <div className="info-tags-group">
                <span className="tag-type">✓ OO사 인턴 6개월 ×</span>
                <span className="tag-type">✓ OO사 정직원 2년 6개월 ×</span>
              </div>
            </div>
          </div>
        </div>

        <div className="resume-bottom-section">
          <div className="bottom-info-block">
            <h3 className="section-title">자격증</h3>
            <div className="bottom-tags-container">
              <span className="tag-skill">✓ 자격증 1 ×</span>
              <span className="tag-skill">✓ 자격증 2 ×</span>
            </div>
          </div>
          
          <div className="bottom-info-block">
            <h3 className="section-title">업무 스택</h3>
            <div className="bottom-tags-container">
            </div>
          </div>
        </div>
      </div>

      <div className="intro-letter-card">
        <h3>자기소개서</h3>
        <textarea className="intro-textarea" placeholder="내용을 입력하세요."></textarea>
      </div>
    </div>
  );
}

export default Resume;