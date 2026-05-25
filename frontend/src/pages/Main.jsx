import React from 'react';
import './Main.css';
import BookmarkIcon from '../components/BookmarkIcon';

function Main() {
  return (
    <div className="main-page-wrapper">
      <div className="main-page-container">
        <div className="main-left-panel">
          <BookmarkIcon />
          <div className="image-placeholder-container">
            <div className="image-icon-wrapper">
              <img src="https://cdn-icons-png.flaticon.com/512/1160/1160358.png" alt="Card Placeholder" />
            </div>
          </div>
          <div className="navigation-arrows">
            <button className="arrow-left">‹</button>
            <button className="arrow-right">›</button>
          </div>
          <div className="panel-info">
            <h2 className="card-title">채용 공고 제목</h2>
            <div className="company-info-row">
              <span className="company-name">회사명</span>
              <span className="badge">신입/경력</span>
            </div>
            <div className="tags-container">
              <span className="tag-type">✓ 직군 1 ×</span>
              <span className="tag-type">✓ 직군 2 ×</span>
              <span className="tag-type">✓ 직군 3 ×</span>
            </div>
            <div className="tags-container skill-tags-row">
              <span className="tag-skill">✓ 업무스택 1 ×</span>
              <span className="tag-skill">✓ 업무스택 2 ×</span>
              <span className="tag-skill">✓ 업무스택 3 ×</span>
            </div>
          </div>
        </div>

        <div className="main-center-panel">
          <div className="map-placeholder">
            <div className="map-pin pin1">📍</div>
            <div className="map-pin pin2">📍</div>
            <div className="map-pin pin3">📍</div>
            <div className="map-pin pin4">📍</div>
            <div className="map-pin pin5">📍</div>
            
            <div className="map-popup">
              <BookmarkIcon isMini={true} />
              <h3 className="card-title">채용 공고 제목</h3>
              <div className="popup-info-row">
                <span className="company-name">회사명</span>
                <span className="badge">신입/경력</span>
              </div>
              <div className="tags-container">
                <span className="tag-type">✓ 직군 1 ×</span>
                <span className="tag-type">✓ 직군 2 ×</span>
                <span className="tag-type">✓ 직군 3 ×</span>
              </div>
              <div className="tags-container skill-tags-row">
                <span className="tag-skill">✓ 업무스택 1 ×</span>
                <span className="tag-skill">✓ 업무스택 2 ×</span>
              </div>
            </div>
          </div>
        </div>

        <div className="main-right-panel">
          <div className="profile-summary-card">
            <div className="profile-main-layout">
              <div className="profile-left-info-block">
                <div className="profile-avatar-3to4">
                  <img src="https://cdn-icons-png.flaticon.com/512/1160/1160358.png" alt="Profile" />
                </div>
                <div className="profile-basic-info">
                  <h3>이름 / 나이</h3>
                  <h3>학력</h3>
                </div>
              </div>
              <div className="profile-skills-column-box">
                <h4>업무스택</h4>
                <div className="profile-skills-vertical-list">
                  <span className="tag-skill">✓ 업무스택 1 ×</span>
                  <span className="tag-skill">✓ 업무스택 2 ×</span>
                  <span className="tag-skill">✓ 업무스택 3 ×</span>
                </div>
              </div>
            </div>
            <div className="profile-details-box">
              <h4>경력</h4>
              <h4>기타사항</h4>
            </div>
          </div>

          <div className="mini-job-grid">
            {[1, 2, 3, 4].map((item) => (
              <div key={item} className="mini-job-card">
                <BookmarkIcon isMini={true} />
                <h3 className="card-title">채용 공고 제목</h3>
                <div className="company-info-row">
                  <span className="company-name">회사명</span>
                  <span className="badge">신입/경력</span>
                </div>
                <div className="tags-container">
                  <span className="tag-type">✓ 직군 1 ×</span>
                  <span className="tag-type">✓ 직군 2 ×</span>
                  <span className="tag-type">✓ 직군 3 ×</span>
                </div>
                <div className="tags-container skill-tags-row">
                  <span className="tag-skill">✓ 업무스택 1 ×</span>
                  <span className="tag-skill">✓ 업무스택 2 ×</span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Main;