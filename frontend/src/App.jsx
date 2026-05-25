import React, { useState } from 'react';
import './App.css';
import Header from './components/Header';
import Footer from './components/Footer';
import Main from './pages/Main';
import JobList from './pages/JobList';
import Resume from './pages/Resume';

function App() {
  const [currentPage, setCurrentPage] = useState('main');

  const renderPage = () => {
    switch (currentPage) {
      case 'main':
        return <Main />;
      case 'jobs':
        return <JobList />;
      case 'resume':
        return <Resume />;
      default:
        return <Main />;
    }
  };

  return (
    <div className="app-container">
      <Header setCurrentPage={setCurrentPage} />
      <main className="content-container">
        {renderPage()}
      </main>
      <Footer setCurrentPage={setCurrentPage} />
    </div>
  );
}

export default App;