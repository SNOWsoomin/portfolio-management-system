import React, { useEffect, useMemo, useRef, useState } from 'react';
import './Resume.css';
import { api } from '../api';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';

const DEFAULT_ACTIVITIES = ['GitHub 협업', 'Docker 기초 배포'];
const DEFAULT_PROFILE_INFO = {
  age: '24',
  education: ['동국대학교 컴퓨터공학과 재학', 'OO고등학교 졸업'],
  career: ['포트폴리오 관리 시스템 백엔드 구현', '팀 프로젝트 협업 대시보드 개발'],
};
const LEVEL_LABELS = {
  BEGINNER: '초급',
  INTERMEDIATE: '중급',
  ADVANCED: '고급',
};

function readStoredJson(key, fallback) {
  try {
    const stored = localStorage.getItem(key);
    return stored ? { ...fallback, ...JSON.parse(stored) } : fallback;
  } catch {
    return fallback;
  }
}

function RemovableTag({ children, onRemove, dark = false }) {
  return (
    <span className={dark ? 'tag-skill editable-tag' : 'tag-type editable-tag'}>
      ✓ {children}
      {onRemove && (
        <button className="tag-remove-btn no-pdf" type="button" onClick={onRemove} aria-label={`${children} 삭제`}>
          삭제
        </button>
      )}
    </span>
  );
}

function Resume({ data, loading, onSave }) {
  const portfolio = data.portfolios[0];
  const pdfRef = useRef(null);
  const [allSkills, setAllSkills] = useState([]);
  const [markdownContent, setMarkdownContent] = useState('');
  const [introduction, setIntroduction] = useState('');
  const [message, setMessage] = useState('');
  const [exporting, setExporting] = useState(false);
  const [aiOpen, setAiOpen] = useState(false);
  const [aiLoading, setAiLoading] = useState(false);
  const [aiStep, setAiStep] = useState('confirm');
  const [aiDiagnosis, setAiDiagnosis] = useState(null);
  const [aiPolish, setAiPolish] = useState(null);
  const [aiError, setAiError] = useState('');
  const [skillForm, setSkillForm] = useState({ skillId: '', level: 'INTERMEDIATE' });
  const [projectForm, setProjectForm] = useState({
    title: '',
    description: '',
    roleDescription: '',
    startDate: '',
    endDate: '',
    githubUrl: '',
    deployUrl: '',
    skillIds: [],
  });
  const [activities, setActivities] = useState(() => {
    const stored = localStorage.getItem('resumeActivities');
    return stored ? JSON.parse(stored) : DEFAULT_ACTIVITIES;
  });
  const [activityInput, setActivityInput] = useState('');
  const [profileInfo, setProfileInfo] = useState(() => readStoredJson('resumeProfileInfo', DEFAULT_PROFILE_INFO));
  const [profileInputs, setProfileInputs] = useState({ education: '', career: '' });

  const skillOptions = useMemo(() => {
    const owned = new Set(data.skills.map((skill) => skill.skillId));
    return allSkills.filter((skill) => !owned.has(skill.id));
  }, [allSkills, data.skills]);

  useEffect(() => {
    setMarkdownContent(portfolio?.markdownContent || '');
    setIntroduction(portfolio?.introduction || '');
  }, [portfolio]);

  useEffect(() => {
    api.skills().then(setAllSkills).catch((error) => setMessage(error.message));
  }, []);

  useEffect(() => {
    localStorage.setItem('resumeActivities', JSON.stringify(activities));
  }, [activities]);

  useEffect(() => {
    localStorage.setItem('resumeProfileInfo', JSON.stringify(profileInfo));
  }, [profileInfo]);

  useEffect(() => {
    const open = () => {
      setAiOpen(true);
      setAiStep('confirm');
      setAiDiagnosis(null);
      setAiPolish(null);
      setAiError('');
      sessionStorage.removeItem('openAiReview');
    };
    window.addEventListener('open-ai-review', open);
    if (sessionStorage.getItem('openAiReview') === 'true') {
      open();
    }
    return () => window.removeEventListener('open-ai-review', open);
  }, []);

  const refresh = async (successMessage) => {
    await onSave();
    setMessage(successMessage);
  };

  const save = async () => {
    setMessage('');
    try {
      await api.savePortfolio(portfolio?.id, {
        title: portfolio?.title || `${data.me?.name || '나'}의 기술 포트폴리오`,
        introduction,
        markdownContent,
        isPublic: true,
      });
      await refresh('포트폴리오 저장 완료');
    } catch (error) {
      setMessage(error.message);
    }
  };

  const addSkill = async () => {
    if (!skillForm.skillId) {
      setMessage('추가할 기술을 선택하세요.');
      return;
    }
    try {
      await api.addMySkill(skillForm);
      setSkillForm({ skillId: '', level: 'INTERMEDIATE' });
      await refresh('기술 스택 추가 완료');
    } catch (error) {
      setMessage(error.message);
    }
  };

  const deleteSkill = async (skillId) => {
    try {
      await api.deleteMySkill(skillId);
      await refresh('기술 스택 삭제 완료');
    } catch (error) {
      setMessage(error.message);
    }
  };

  const toggleProjectSkill = (skillId) => {
    setProjectForm((current) => {
      const exists = current.skillIds.includes(skillId);
      return {
        ...current,
        skillIds: exists
          ? current.skillIds.filter((id) => id !== skillId)
          : [...current.skillIds, skillId],
      };
    });
  };

  const createProject = async () => {
    if (!projectForm.title.trim()) {
      setMessage('프로젝트명을 입력하세요.');
      return;
    }
    try {
      await api.createProject({
        ...projectForm,
        title: projectForm.title.trim(),
        portfolioId: portfolio?.id || null,
        startDate: projectForm.startDate || null,
        endDate: projectForm.endDate || null,
        skillIds: projectForm.skillIds,
      });
      setProjectForm({
        title: '',
        description: '',
        roleDescription: '',
        startDate: '',
        endDate: '',
        githubUrl: '',
        deployUrl: '',
        skillIds: [],
      });
      await refresh('프로젝트 추가 완료');
    } catch (error) {
      setMessage(error.message);
    }
  };

  const deleteProject = async (projectId) => {
    try {
      await api.deleteProject(projectId);
      await refresh('프로젝트 삭제 완료');
    } catch (error) {
      setMessage(error.message);
    }
  };

  const addActivity = () => {
    const next = activityInput.trim();
    if (!next) return;
    if (!activities.includes(next)) {
      setActivities((current) => [...current, next]);
    }
    setActivityInput('');
  };

  const removeActivity = (activity) => {
    setActivities((current) => current.filter((item) => item !== activity));
  };

  const updateAge = (age) => {
    const cleaned = age.replace(/[^\d]/g, '').slice(0, 3);
    setProfileInfo((current) => ({ ...current, age: cleaned }));
  };

  const addProfileItem = (field) => {
    const next = profileInputs[field].trim();
    if (!next) return;
    setProfileInfo((current) => {
      const list = current[field] || [];
      return list.includes(next) ? current : { ...current, [field]: [...list, next] };
    });
    setProfileInputs((current) => ({ ...current, [field]: '' }));
  };

  const removeProfileItem = (field, value) => {
    setProfileInfo((current) => ({
      ...current,
      [field]: (current[field] || []).filter((item) => item !== value),
    }));
  };

  const exportPdf = async () => {
    if (!pdfRef.current || exporting) return;
    setExporting(true);
    setMessage('');
    pdfRef.current.classList.add('pdf-exporting');
    try {
      const canvas = await html2canvas(pdfRef.current, {
        scale: 2,
        backgroundColor: '#ffffff',
        useCORS: true,
      });
      const imageData = canvas.toDataURL('image/png');
      const pdf = new jsPDF('p', 'mm', 'a4');
      const pageWidth = pdf.internal.pageSize.getWidth();
      const pageHeight = pdf.internal.pageSize.getHeight();
      const imageWidth = pageWidth;
      const imageHeight = (canvas.height * imageWidth) / canvas.width;

      let heightLeft = imageHeight;
      let position = 0;
      pdf.addImage(imageData, 'PNG', 0, position, imageWidth, imageHeight);
      heightLeft -= pageHeight;

      while (heightLeft > 0) {
        position -= pageHeight;
        pdf.addPage();
        pdf.addImage(imageData, 'PNG', 0, position, imageWidth, imageHeight);
        heightLeft -= pageHeight;
      }

      const safeName = (data.me?.name || 'portfolio').replace(/[\\/:*?"<>|]/g, '_');
      pdf.save(`${safeName}_portfolio.pdf`);
      setMessage('PDF 내보내기 완료');
    } catch (error) {
      setMessage(`PDF 내보내기 실패: ${error.message}`);
    } finally {
      pdfRef.current?.classList.remove('pdf-exporting');
      setExporting(false);
    }
  };

  const currentAiPayload = () => ({
    introduction,
    markdownContent,
  });

  const closeAiReview = () => {
    setAiOpen(false);
    setAiLoading(false);
    setAiStep('confirm');
    setAiDiagnosis(null);
    setAiPolish(null);
    setAiError('');
  };

  const runAiDiagnosis = async () => {
    setAiLoading(true);
    setAiError('');
    try {
      const result = await api.fullReviewResume(currentAiPayload());
      setAiDiagnosis(result);
      setAiPolish(result);
      setAiStep('diagnosis');
    } catch (error) {
      setAiError(error.message);
      setAiStep('error');
    } finally {
      setAiLoading(false);
    }
  };

  const runAiPolish = async () => {
    if (!aiPolish) {
      setAiError('AI 첨삭 결과를 불러오지 못했습니다. 다시 진단을 실행해주세요.');
      setAiStep('error');
      return;
    }
    setAiStep('polish');
  };

  const applyAiPolish = () => {
    if (!aiPolish) return;
    setIntroduction(aiPolish.improvedIntroduction || introduction);
    setMarkdownContent(aiPolish.improvedMarkdownContent || markdownContent);
    setMessage('AI 첨삭본이 입력창에 적용되었습니다. 최종 반영하려면 포트폴리오 저장을 눌러주세요.');
    closeAiReview();
  };

  return (
    <div className="resume-page">
      <div className="resume-action-bar">
        <div>
          <span className="eyebrow">PORTFOLIO BUILDER</span>
          <h2>포트폴리오 작성/수정</h2>
          <p>기술, 프로젝트, 활동 항목을 관리하고 제출용 PDF로 내보낼 수 있습니다.</p>
        </div>
        <div className="resume-action-buttons">
          <button className="resume-save-btn secondary" type="button" onClick={save}>저장</button>
          <button className="pdf-export-btn" type="button" onClick={exportPdf} disabled={exporting}>
            {exporting ? 'PDF 생성 중...' : 'PDF 내보내기'}
          </button>
        </div>
      </div>

      {loading && <p className="resume-message">데이터를 불러오는 중...</p>}
      {message && <p className="resume-message">{message}</p>}

      <div className="resume-layout-grid">
        <section className="resume-preview-panel">
          <div className="resume-profile-card" ref={pdfRef}>
            <div className="resume-header-flex">
              <div className="resume-avatar-box">PM</div>

              <div className="resume-details-grid">
                <div className="info-row">
                  <span className="info-label">이름</span>
                  <div className="info-content-blank">{data.me?.name || '로그인 필요'}</div>
                </div>
                <div className="info-row">
                  <span className="info-label">나이</span>
                  <div className="info-content-blank">{profileInfo.age ? `${profileInfo.age}세` : '-'}</div>
                </div>
                <div className="info-row">
                  <span className="info-label">학력</span>
                  <div className="info-tags-group">
                    {(profileInfo.education || []).map((education) => (
                      <RemovableTag key={education}>{education}</RemovableTag>
                    ))}
                  </div>
                </div>
                <div className="info-row">
                  <span className="info-label">경력</span>
                  <div className="info-tags-group">
                    {(profileInfo.career || []).map((career) => (
                      <RemovableTag key={career}>{career}</RemovableTag>
                    ))}
                  </div>
                </div>
              </div>
            </div>

            <div className="resume-bottom-section">
              <div className="bottom-info-block">
                <h3 className="section-title">활동 / 자격</h3>
                <div className="bottom-tags-container">
                  {activities.map((activity) => (
                    <RemovableTag dark key={activity} onRemove={() => removeActivity(activity)}>
                      {activity}
                    </RemovableTag>
                  ))}
                </div>
              </div>

              <div className="bottom-info-block">
                <h3 className="section-title">업무 스택</h3>
                <div className="bottom-tags-container">
                  {data.skills.map((skill) => (
                    <RemovableTag dark key={skill.skillId} onRemove={() => deleteSkill(skill.skillId)}>
                      {skill.name} · {LEVEL_LABELS[skill.level] || skill.level}
                    </RemovableTag>
                  ))}
                </div>
              </div>
            </div>

            <div className="pdf-project-section">
              <h3 className="section-title">프로젝트 경험</h3>
              <div className="pdf-project-list">
                {data.projects.map((project) => (
                  <div className="pdf-project-card" key={project.id}>
                    <button className="project-delete-btn no-pdf" type="button" onClick={() => deleteProject(project.id)}>삭제</button>
                    <strong>{project.title}</strong>
                    <span>{project.startDate || '-'} ~ {project.endDate || '-'}</span>
                    <p>{project.description || project.roleDescription || '프로젝트 설명이 없습니다.'}</p>
                    {project.githubUrl && <small>GitHub: {project.githubUrl}</small>}
                    <div className="bottom-tags-container">
                      {(project.skills || []).map((skill) => (
                        <span className="tag-type" key={`${project.id}-${skill.skillId || skill.id || skill.name}`}>✓ {skill.name}</span>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="pdf-markdown-preview">
              <h3 className="section-title">포트폴리오 본문</h3>
              <p className="pdf-introduction">{introduction || portfolio?.introduction || '자기소개 내용이 없습니다.'}</p>
              <div className="markdown-preview-box">
                <ReactMarkdown remarkPlugins={[remarkGfm]}>
                  {markdownContent || portfolio?.markdownContent || 'Markdown 포트폴리오 내용을 입력하면 이 영역이 PDF에 포함됩니다.'}
                </ReactMarkdown>
              </div>
            </div>
          </div>
        </section>

        <aside className="resume-editor-panel no-pdf">
          <div className="editor-card">
            <h3>기본 정보 수정</h3>
            <label className="profile-field-label" htmlFor="resume-age">나이</label>
            <input
              id="resume-age"
              className="profile-age-input"
              value={profileInfo.age}
              onChange={(e) => updateAge(e.target.value)}
              placeholder="예: 24"
            />
            <div className="profile-list-editor">
              <strong>학력</strong>
              <div className="inline-form compact-form">
                <input
                  value={profileInputs.education}
                  onChange={(e) => setProfileInputs((current) => ({ ...current, education: e.target.value }))}
                  placeholder="예: 동국대학교 컴퓨터공학과 재학"
                />
                <button type="button" onClick={() => addProfileItem('education')}>추가</button>
              </div>
              <div className="profile-editor-tags">
                {(profileInfo.education || []).map((education) => (
                  <RemovableTag key={education} onRemove={() => removeProfileItem('education', education)}>
                    {education}
                  </RemovableTag>
                ))}
              </div>
            </div>
            <div className="profile-list-editor">
              <strong>경력</strong>
              <div className="inline-form compact-form">
                <input
                  value={profileInputs.career}
                  onChange={(e) => setProfileInputs((current) => ({ ...current, career: e.target.value }))}
                  placeholder="예: OO사 인턴 6개월"
                />
                <button type="button" onClick={() => addProfileItem('career')}>추가</button>
              </div>
              <div className="profile-editor-tags">
                {(profileInfo.career || []).map((career) => (
                  <RemovableTag key={career} onRemove={() => removeProfileItem('career', career)}>
                    {career}
                  </RemovableTag>
                ))}
              </div>
            </div>
          </div>

          <div className="editor-card">
            <h3>기술 스택 추가</h3>
            <div className="inline-form">
              <select value={skillForm.skillId} onChange={(e) => setSkillForm((current) => ({ ...current, skillId: e.target.value }))}>
                <option value="">기술 선택</option>
                {skillOptions.map((skill) => (
                  <option value={skill.id} key={skill.id}>{skill.name} · {skill.category}</option>
                ))}
              </select>
              <select value={skillForm.level} onChange={(e) => setSkillForm((current) => ({ ...current, level: e.target.value }))}>
                <option value="BEGINNER">초급</option>
                <option value="INTERMEDIATE">중급</option>
                <option value="ADVANCED">고급</option>
              </select>
              <button type="button" onClick={addSkill}>추가</button>
            </div>
          </div>

          <div className="editor-card">
            <h3>활동 / 자격 추가</h3>
            <div className="inline-form">
              <input value={activityInput} onChange={(e) => setActivityInput(e.target.value)} placeholder="예: 정보처리기사 필기" />
              <button type="button" onClick={addActivity}>추가</button>
            </div>
          </div>

          <div className="editor-card">
            <h3>프로젝트 추가</h3>
            <div className="project-form-grid">
              <input value={projectForm.title} onChange={(e) => setProjectForm((current) => ({ ...current, title: e.target.value }))} placeholder="프로젝트명" />
              <input value={projectForm.roleDescription} onChange={(e) => setProjectForm((current) => ({ ...current, roleDescription: e.target.value }))} placeholder="담당 역할" />
              <input type="date" value={projectForm.startDate} onChange={(e) => setProjectForm((current) => ({ ...current, startDate: e.target.value }))} />
              <input type="date" value={projectForm.endDate} onChange={(e) => setProjectForm((current) => ({ ...current, endDate: e.target.value }))} />
              <input value={projectForm.githubUrl} onChange={(e) => setProjectForm((current) => ({ ...current, githubUrl: e.target.value }))} placeholder="GitHub URL" />
              <input value={projectForm.deployUrl} onChange={(e) => setProjectForm((current) => ({ ...current, deployUrl: e.target.value }))} placeholder="배포 URL" />
              <textarea value={projectForm.description} onChange={(e) => setProjectForm((current) => ({ ...current, description: e.target.value }))} placeholder="프로젝트 설명" />
            </div>
            <div className="skill-choice-grid">
              {allSkills.slice(0, 14).map((skill) => (
                <button
                  type="button"
                  key={skill.id}
                  className={projectForm.skillIds.includes(skill.id) ? 'selected' : ''}
                  onClick={() => toggleProjectSkill(skill.id)}
                >
                  {skill.name}
                </button>
              ))}
            </div>
            <button className="wide-action-btn" type="button" onClick={createProject}>프로젝트 추가</button>
          </div>
        </aside>
      </div>

      <div className="intro-letter-card">
        <h3>자기소개서 / 포트폴리오 Markdown</h3>
        <textarea className="intro-textarea" placeholder="한 줄 자기소개를 입력하세요." value={introduction} onChange={(e) => setIntroduction(e.target.value)} />
        <textarea className="intro-textarea markdown-area" placeholder="Markdown 포트폴리오 내용을 입력하세요." value={markdownContent} onChange={(e) => setMarkdownContent(e.target.value)} />
        <div className="editor-bottom-actions">
          <button className="resume-save-btn" type="button" onClick={save}>포트폴리오 저장</button>
          <button className="pdf-export-btn ghost" type="button" onClick={exportPdf} disabled={exporting}>
            {exporting ? 'PDF 생성 중...' : 'PDF 내보내기'}
          </button>
        </div>
      </div>

      {aiOpen && (
        <div className="ai-review-overlay no-pdf" role="dialog" aria-modal="true" aria-label="AI 첨삭">
          <div className="ai-review-modal">
            <div className="ai-review-head">
              <div>
                <span className="eyebrow">GEMINI AI REVIEW</span>
                <h3>AI 이력서 첨삭</h3>
              </div>
              <button className="ai-close-btn" type="button" onClick={closeAiReview} aria-label="AI 첨삭 닫기">닫기</button>
            </div>

            {aiStep === 'confirm' && (
              <div className="ai-review-section">
                <p className="ai-lead">현재 작성된 자기소개와 포트폴리오 본문, 프로젝트 경험, 기술 스택, 채용공고 부족 기술을 기준으로 AI 진단을 실행합니다.</p>
                <div className="ai-notice-box">
                  <strong>진행 순서</strong>
                  <span>1. AI가 현재 이력서의 문제점과 강점을 진단합니다.</span>
                  <span>2. 진단 결과를 보고 첨삭 예시본을 요청할 수 있습니다.</span>
                  <span>3. 첨삭본을 확인한 뒤 적용 여부를 직접 선택합니다.</span>
                </div>
                <div className="ai-actions">
                  <button type="button" className="resume-save-btn" onClick={runAiDiagnosis} disabled={aiLoading}>
                    {aiLoading ? 'AI 진단 중...' : 'AI 진단 실행'}
                  </button>
                  <button type="button" className="resume-save-btn secondary" onClick={closeAiReview}>취소</button>
                </div>
              </div>
            )}

            {aiStep === 'diagnosis' && aiDiagnosis && (
              <div className="ai-review-section">
                <p className="ai-lead">{aiDiagnosis.diagnosis}</p>
                <div className="ai-grid">
                  <div className="ai-result-card danger">
                    <h4>문제점</h4>
                    <ul>{aiDiagnosis.problems?.map((item) => <li key={item}>{item}</li>)}</ul>
                  </div>
                  <div className="ai-result-card">
                    <h4>강점</h4>
                    <ul>{aiDiagnosis.strengths?.map((item) => <li key={item}>{item}</li>)}</ul>
                  </div>
                  <div className="ai-result-card focus">
                    <h4>보완 방향</h4>
                    <ul>{aiDiagnosis.suggestedFocus?.map((item) => <li key={item}>{item}</li>)}</ul>
                  </div>
                </div>
                <div className="ai-actions">
                  <button type="button" className="resume-save-btn" onClick={runAiPolish} disabled={aiLoading}>
                    {aiLoading ? '첨삭 예시 생성 중...' : '첨삭 예시본 받기'}
                  </button>
                  <button type="button" className="resume-save-btn secondary" onClick={closeAiReview}>취소</button>
                </div>
              </div>
            )}

            {aiStep === 'polish' && aiPolish && (
              <div className="ai-review-section">
                <p className="ai-lead">{aiPolish.diagnosis}</p>
                <div className="ai-compare-grid">
                  <div className="ai-compare-card">
                    <h4>첨삭 전</h4>
                    <strong>자기소개</strong>
                    <p>{introduction || '작성된 자기소개가 없습니다.'}</p>
                    <strong>Markdown 본문</strong>
                    <pre>{markdownContent || '작성된 Markdown 본문이 없습니다.'}</pre>
                  </div>
                  <div className="ai-compare-card improved">
                    <h4>첨삭 후</h4>
                    <strong>자기소개</strong>
                    <p>{aiPolish.improvedIntroduction}</p>
                    <strong>Markdown 본문</strong>
                    <pre>{aiPolish.improvedMarkdownContent}</pre>
                  </div>
                </div>
                <div className="ai-result-card focus">
                  <h4>변경 요약</h4>
                  <ul>{aiPolish.changeSummary?.map((item) => <li key={item}>{item}</li>)}</ul>
                </div>
                <div className="ai-actions">
                  <button type="button" className="resume-save-btn" onClick={applyAiPolish}>첨삭본 적용</button>
                  <button type="button" className="resume-save-btn secondary" onClick={closeAiReview}>취소</button>
                </div>
              </div>
            )}

            {aiStep === 'error' && (
              <div className="ai-review-section">
                <div className="ai-result-card danger">
                  <h4>AI 첨삭 사용 불가</h4>
                  <p>{aiError || 'Gemini API 키가 설정되어 있지 않아 현재 AI 첨삭을 사용할 수 없습니다.'}</p>
                </div>
                <div className="ai-actions">
                  <button type="button" className="resume-save-btn secondary" onClick={closeAiReview}>확인</button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default Resume;
