import React, { useCallback, useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './App.css';
import Header from './components/Header';
import Footer from './components/Footer';
import Main from './pages/Main';
import JobList from './pages/JobList';
import Resume from './pages/Resume';
import Login from './pages/Login';
import Signup from './pages/Signup';
import { api } from './api';

function App() {
  const [authUser, setAuthUser] = useState(() => {
    const raw = localStorage.getItem('authUser');
    return raw ? JSON.parse(raw) : null;
  });
  const [loading, setLoading] = useState(true);
  const [dashboard, setDashboard] = useState({
    me: null,
    skills: [],
    jobs: [],
    matches: [],
    portfolios: [],
    projects: [],
    crawlStatus: null,
  });
  const [notice, setNotice] = useState('');

  const loadDashboard = useCallback(async () => {
    if (!localStorage.getItem('accessToken')) {
      setLoading(false);
      return;
    }
    setLoading(true);
    setNotice('');
    try {
      const [me, skills, jobs, matches, portfolios, projects, crawlStatus] = await Promise.all([
        api.me(),
        api.mySkills(),
        api.jobs(),
        api.matches(),
        api.portfolios(),
        api.projects(),
        api.crawlStatus(),
      ]);
      setDashboard({ me, skills, jobs, matches, portfolios, projects, crawlStatus });
      setAuthUser({ userName: me.name, role: me.role });
      localStorage.setItem('authUser', JSON.stringify({ userName: me.name, role: me.role }));
    } catch (error) {
      setNotice(error.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  useEffect(() => {
    if (!localStorage.getItem('accessToken')) return undefined;
    const timer = window.setInterval(() => {
      loadDashboard();
    }, 60_000);
    return () => window.clearInterval(timer);
  }, [loadDashboard, authUser]);

  const handleLogin = async (email, password) => {
    const data = await api.login(email, password);
    localStorage.setItem('accessToken', data.accessToken);
    const user = { userName: data.userName, role: data.role };
    localStorage.setItem('authUser', JSON.stringify(user));
    setAuthUser(user);
    await loadDashboard();
    return data;
  };

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('authUser');
    setAuthUser(null);
    setDashboard({ me: null, skills: [], jobs: [], matches: [], portfolios: [], projects: [], crawlStatus: null });
  };

  return (
    <Router>
      <div className="app-container">
        <Header authUser={authUser} onLogout={handleLogout} />
        
        <main className="content-container">
          <Routes>
            <Route path="/" element={<Main data={dashboard} loading={loading} notice={notice} onDemoLogin={() => handleLogin('user@test.com', 'user1234')} />} />
            <Route path="/jobs" element={<JobList data={dashboard} authUser={authUser} onRefresh={loadDashboard} />} />
            <Route path="/resume" element={<Resume data={dashboard} loading={loading} onSave={loadDashboard} />} />
            <Route path="/login" element={<Login onLogin={handleLogin} />} />
            <Route path="/signup" element={<Signup />}/>
          </Routes>
        </main>
        
        <Footer />
      </div>
    </Router>
  );
}

export default App;
