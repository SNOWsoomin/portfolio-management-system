const API_BASE_URL = 'http://localhost:8080/api';

function getToken() {
  return localStorage.getItem('accessToken');
}

async function request(path, options = {}) {
  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(getToken() ? { Authorization: `Bearer ${getToken()}` } : {}),
    ...options.headers,
  };
  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });
  const payload = await response.json().catch(() => ({}));
  if (!response.ok || payload.success === false) {
    throw new Error(payload.error || payload.message || '요청 처리에 실패했습니다.');
  }
  return payload.data;
}

export const api = {
  login: (email, password) => request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  }),
  signup: ({ email, password, name }) => request('/auth/signup', {
    method: 'POST',
    body: JSON.stringify({ email, password, name }),
  }),
  me: () => request('/users/me'),
  mySkills: () => request('/users/me/skills'),
  addMySkill: ({ skillId, level }) => request('/users/me/skills', {
    method: 'POST',
    body: JSON.stringify({ skillId: Number(skillId), level }),
  }),
  deleteMySkill: (skillId) => request(`/users/me/skills/${skillId}`, {
    method: 'DELETE',
  }),
  skills: () => request('/skills'),
  portfolios: () => request('/portfolios/me'),
  savePortfolio: (portfolioId, payload) => request(portfolioId ? `/portfolios/${portfolioId}` : '/portfolios', {
    method: portfolioId ? 'PUT' : 'POST',
    body: JSON.stringify(payload),
  }),
  diagnoseResume: (payload) => request('/ai-review/diagnose', {
    method: 'POST',
    body: JSON.stringify(payload),
  }),
  fullReviewResume: (payload) => request('/ai-review/full', {
    method: 'POST',
    body: JSON.stringify(payload),
  }),
  polishResume: (payload) => request('/ai-review/polish', {
    method: 'POST',
    body: JSON.stringify(payload),
  }),
  projects: () => request('/projects/me'),
  createProject: (payload) => request('/projects', {
    method: 'POST',
    body: JSON.stringify(payload),
  }),
  deleteProject: (projectId) => request(`/projects/${projectId}`, {
    method: 'DELETE',
  }),
  jobs: () => request('/jobs'),
  crawlStatus: () => request('/jobs/crawl-status'),
  matches: () => request('/jobs/matches'),
  crawlJobKorea: (keyword = '개발자', limit = 30) => request(`/admin/jobs/crawl/jobkorea?keyword=${encodeURIComponent(keyword)}&limit=${limit}`, {
    method: 'POST',
  }),
};
