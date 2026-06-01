import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'; // 라우터 임포트
import './App.css';
import Header from './components/Header';
import Footer from './components/Footer';
import Main from './pages/Main';
import JobList from './pages/JobList';
import Resume from './pages/Resume';
import Login from './pages/Login';
import Signup from './pages/Signup';

function App() {
  const [portfolios, setPortfolios] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch('http://localhost:8080/api/portfolios?userId=1')
      .then(res => {
        if(!res.ok) throw new Error("서버 응답 실패");
        return res.json();
      })
      .then(data => {
        setPortfolios(data);
        setLoading(false);
      })
      .catch(err => {
        console.error("백엔드 통신 에러:", err);
        setLoading(false);
      });
  }, []);

  return (
    <Router>
      <div className="app-container">
        <Header /> 
        
        <main className="content-container">
          <Routes>
            <Route path="/" element={<Main />} />
            <Route path="/jobs" element={<JobList />} />
            <Route path="/resume" element={<Resume portfolios={portfolios} loading={loading} />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />}/>
          </Routes>
        </main>
        
        <Footer />
      </div>
    </Router>
  );
}

export default App;