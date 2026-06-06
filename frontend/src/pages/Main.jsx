import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import './Main.css';
import BookmarkIcon from '../components/BookmarkIcon';
import JobMap from '../components/JobMap';
import backendCardImage from '../assets/backend-developer-card.png';

const DEFAULT_PROFILE_INFO = {
  age: '24',
  education: ['동국대학교 컴퓨터공학과 재학', 'OO고등학교 졸업'],
  career: ['포트폴리오 관리 시스템 백엔드 구현', '팀 프로젝트 협업 대시보드 개발'],
};

function readProfileInfo() {
  try {
    const stored = localStorage.getItem('resumeProfileInfo');
    return stored ? { ...DEFAULT_PROFILE_INFO, ...JSON.parse(stored) } : DEFAULT_PROFILE_INFO;
  } catch {
    return DEFAULT_PROFILE_INFO;
  }
}

function skillNames(job) {
  return (job?.skills || []).map((skill) => skill.name).slice(0, 4);
}

function Main({ data, loading, notice, onDemoLogin }) {
  const [featuredJobId, setFeaturedJobId] = useState(null);
  const [profileInfo] = useState(readProfileInfo);
  const { me, skills, jobs, matches, projects, portfolios } = data;
  const matchMap = Object.fromEntries(matches.map((match) => [match.jobPostId, match]));
  const rankedJobs = jobs
    .map((job) => ({ ...job, matchRate: matchMap[job.id]?.matchRate ?? 0 }))
    .sort((a, b) => b.matchRate - a.matchRate);
  const selectedIndex = Math.max(0, rankedJobs.findIndex((job) => job.id === featuredJobId));
  const featuredJob = rankedJobs.length ? rankedJobs[selectedIndex] : null;
  const featuredNumber = rankedJobs.length ? selectedIndex + 1 : 0;
  const profilePortfolio = portfolios[0];
  const crawlStatus = data.crawlStatus;
  const crawledAt = crawlStatus?.lastCrawledAt
    ? new Intl.DateTimeFormat('ko-KR', { hour: '2-digit', minute: '2-digit' }).format(new Date(crawlStatus.lastCrawledAt))
    : '자동 수집 대기';

  const moveFeatured = (direction) => {
    if (!rankedJobs.length) return;
    const nextIndex = (selectedIndex + direction + rankedJobs.length) % rankedJobs.length;
    setFeaturedJobId(rankedJobs[nextIndex].id);
  };

  return (
    <div className="main-page-wrapper">
      <div className="main-page-container">
        <div className="main-left-panel">
          <BookmarkIcon />
          <div className="image-placeholder-container">
            <img className="job-photo" src={backendCardImage} alt="노트북으로 개발 작업 중인 모습" />
          </div>
          <div className="navigation-arrows">
            <button className="arrow-left" type="button" onMouseDown={() => moveFeatured(-1)} aria-label="이전 추천 공고">‹</button>
            <button className="arrow-right" type="button" onMouseDown={() => moveFeatured(1)} aria-label="다음 추천 공고">›</button>
          </div>
          <div className="panel-info">
            {featuredJob && <span className="featured-order">MATCH #{featuredNumber}</span>}
            <h2 className="card-title">{featuredJob?.title || '로그인 후 추천 공고 확인'}</h2>
            <div className="company-info-row">
              <span className="company-name">{featuredJob?.companyName || 'AIfolio'}</span>
              <span className="badge">{featuredJob ? `${featuredJob.matchRate}% 매칭` : '데모 계정 준비'}</span>
            </div>
            <div className="tags-container">
              <span className="tag-type">✓ {featuredJob?.position || '채용공고 매칭'}</span>
              {featuredJob?.sourceName && <span className="tag-type">✓ {featuredJob.sourceName} 수집</span>}
              <span className="tag-type">✓ 부족 기술 분석</span>
            </div>
            <div className="tags-container skill-tags-row">
              {skillNames(featuredJob).map((skill) => <span className="tag-skill" key={skill}>✓ {skill}</span>)}
            </div>
            {!me && <button className="integrated-cta" onClick={onDemoLogin}>테스트 계정으로 데이터 보기</button>}
          </div>
        </div>

        <div className="main-center-panel">
          <div className="map-placeholder">
            <JobMap jobs={rankedJobs} selectedJob={featuredJob} onSelect={(index) => setFeaturedJobId(rankedJobs[index]?.id)} />
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
                  <h3>{me?.name || '로그인 필요'} / {profileInfo.age ? `${profileInfo.age}세` : '-'}</h3>
                  <div className="profile-mini-section">
                    <strong>학력</strong>
                    <span>{(profileInfo.education || []).slice(0, 2).join(' · ') || '학력 정보 입력 필요'}</span>
                  </div>
                </div>
              </div>
              <div className="profile-skills-column-box">
                <h4>업무스택</h4>
                <div className="profile-skills-vertical-list">
                  {skills.slice(0, 5).map((skill) => <span className="tag-skill" key={skill.skillId}>✓ {skill.name}</span>)}
                </div>
              </div>
            </div>
            <div className="profile-details-box">
              <div className="profile-detail-row">
                <strong>경력</strong>
                <div className="profile-detail-tags">
                  {(profileInfo.career || []).slice(0, 3).map((career) => (
                    <span className="tag-type" key={career}>✓ {career}</span>
                  ))}
                </div>
              </div>
              <div className="profile-detail-row">
                <strong>기타사항</strong>
                <div className="profile-stat-row">
                  <span>프로젝트 {projects.length}개</span>
                  <span>보유 기술 {skills.length}개</span>
                  <span>{profilePortfolio?.title || '포트폴리오 준비 중'}</span>
                </div>
              </div>
              {notice && <p className="integrated-notice">{notice}</p>}
              {loading && <p className="integrated-notice">백엔드 데이터를 불러오는 중...</p>}
            </div>
          </div>

          <div className="mini-job-grid">
            {rankedJobs.slice(0, 4).map((job) => (
              <button
                key={job.id}
                type="button"
                className={`mini-job-card ${featuredJob?.id === job.id ? 'active' : ''}`}
                onClick={() => setFeaturedJobId(job.id)}
              >
                <BookmarkIcon isMini />
                <h3 className="card-title">{job.title}</h3>
                <div className="company-info-row">
                  <span className="company-name">{job.companyName}</span>
                  <span className="badge">{job.matchRate}%</span>
                </div>
                <div className="tags-container">
                  <span className="tag-type">✓ {job.position}</span>
                  {job.sourceName && <span className="tag-type">✓ {job.sourceName}</span>}
                </div>
                <div className="tags-container skill-tags-row">
                  {skillNames(job).slice(0, 2).map((skill) => <span className="tag-skill" key={skill}>✓ {skill}</span>)}
                </div>
              </button>
            ))}
          </div>
          <Link className="integrated-link" to="/jobs">전체 공고와 매칭률 보기</Link>
        </div>
      </div>
    </div>
  );
}

export default Main;
