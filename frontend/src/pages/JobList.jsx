import React, { useMemo } from 'react';
import './JobList.css';
import BookmarkIcon from '../components/BookmarkIcon';

function formatDateTime(value) {
  if (!value) return '서버 시작 후 첫 자동 수집 대기 중';
  return new Intl.DateTimeFormat('ko-KR', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(value));
}

function jobDetailUrl(job) {
  if (job.sourceUrl) return job.sourceUrl;
  const query = encodeURIComponent(`${job.companyName} ${job.title} ${job.position || ''}`);
  return `https://www.jobkorea.co.kr/Search/?stext=${query}&tabType=recruit`;
}

function JobList({ data }) {
  const matchMap = useMemo(() => Object.fromEntries(data.matches.map((match) => [match.jobPostId, match])), [data.matches]);
  const jobs = useMemo(() => data.jobs
    .map((job) => ({ ...job, match: matchMap[job.id] }))
    .sort((a, b) => (b.match?.matchRate || 0) - (a.match?.matchRate || 0)), [data.jobs, matchMap]);
  const status = data.crawlStatus;
  const hasJobs = jobs.length > 0;

  return (
    <div className="job-list-page">
      <div className="job-toolbar">
        <div>
          <h2>채용공고 매칭 현황</h2>
          <p>서버가 일정 주기로 잡코리아 개발자 공고를 자동 수집하고, 사용자 기술 스택과 매칭률을 다시 계산합니다.</p>
        </div>
        <div className="crawler-status-box">
          <span className={`status-dot ${status?.running ? 'running' : ''}`} />
          <div>
            <strong>{status?.running ? '자동 수집 진행 중' : hasJobs ? '자동 수집 완료 / 다음 수집 대기 중' : '자동 수집 준비 중'}</strong>
            <p>{status?.lastCrawledAt ? `마지막 갱신: ${formatDateTime(status.lastCrawledAt)}` : `기본 공고 ${jobs.length}개 표시 중`}</p>
          </div>
        </div>
      </div>

      <div className="crawler-summary-grid">
        <div>
          <span>전체 공고</span>
          <strong>{status?.totalJobCount ?? jobs.length}개</strong>
        </div>
        <div>
          <span>최근 저장</span>
          <strong>{status?.savedCount ?? 0}개</strong>
        </div>
        <div>
          <span>최근 수집</span>
          <strong>{status?.fetchedCount ?? 0}개</strong>
        </div>
        <div>
          <span>검색 키워드</span>
          <strong>{status?.keyword || '개발자'}</strong>
        </div>
      </div>

      {status?.lastError && !hasJobs && <p className="job-message error">{status.lastError}</p>}

      <div className="list-container">
        {jobs.map((job) => (
          <div key={job.id} className="job-list-row">
            <div className="row-left">
              <span className="job-company">{job.companyName}</span>
              <span className="badge">{job.match?.matchRate || 0}%</span>
              <span className="job-title">{job.title}</span>
              {job.sourceName && <span className="badge source-badge">{job.sourceName}</span>}
            </div>
            <div className="row-right">
              <div className="tags-container">
                <span className="tag-type">✓ {job.position}</span>
                {(job.match?.missingSkills || []).slice(0, 2).map((skill) => (
                  <span className="tag-type missing-tag" key={skill}>부족 {skill}</span>
                ))}
              </div>
              <div className="tags-container">
                {(job.skills || []).slice(0, 4).map((skill) => (
                  <span className="tag-skill" key={skill.skillId}>✓ {skill.name}</span>
                ))}
              </div>
              <a className="source-link" href={jobDetailUrl(job)} target="_blank" rel="noreferrer">상세 공고</a>
              <BookmarkIcon isMini />
            </div>
          </div>
        ))}
      </div>

      <div className="pagination">
        <button className="pag-btn">‹</button>
        <button className="pag-btn active">총 {jobs.length}개</button>
        <button className="pag-btn">›</button>
      </div>
    </div>
  );
}

export default JobList;
