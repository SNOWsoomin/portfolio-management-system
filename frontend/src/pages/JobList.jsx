import React from 'react';
import './JobList.css';
import BookmarkIcon from '../components/BookmarkIcon';

function JobList() {
  const jobs = Array.from({ length: 11 }, (_, i) => i + 1);

  return (
    <div className="job-list-page">
      <div className="list-container">
        {jobs.map((job) => (
          <div key={job} className="job-list-row">
            <div className="row-left">
              <span className="job-company">회사명</span>
              <span className="badge">신입/경력</span>
              <span className="job-title">채용 공고 제목</span>
            </div>
            <div className="row-right">
              <div className="tags-container">
                <span className="tag-type">✓ 직군 1 ×</span>
                <span className="tag-type">✓ 직군 2 ×</span>
                <span className="tag-type">✓ 직군 3 ×</span>
              </div>
              <div className="tags-container">
                <span className="tag-skill">✓ 업무스택 1 ×</span>
                <span className="tag-skill">✓ 업무스택 2 ×</span>
              </div>
              <BookmarkIcon isMini={true} />
            </div>
          </div>
        ))}
      </div>

      <div className="pagination">
        <button className="pag-btn">‹</button>
        <button className="pag-btn">1</button>
        <button className="pag-btn active">2</button>
        <button className="pag-btn">3</button>
        <span className="pag-ellipsis">...</span>
        <button className="pag-btn">8</button>
        <button className="pag-btn">›</button>
      </div>
    </div>
  );
}

export default JobList;