import { useState, useEffect } from 'react'
import './App.css'

function App() {
  const [portfolios, setPortfolios] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 백엔드 API 호출 (우선 테스트용으로 userId=1 가정)
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
    <div style={{ padding: '40px', fontFamily: 'sans-serif', textAlign: 'center' }}>
      <h1>💼 내 포트폴리오 관리 시스템</h1>
      <p style={{ color: '#666' }}>백엔드 서버와 정상적으로 연결되었는지 확인하는 화면입니다.</p>
      <hr style={{ margin: '20px auto', maxWidth: '600px', border: '0.5px solid #eee' }} />
      
      {loading ? (
        <p>🔄 백엔드에서 데이터를 져오는 중...</p>
      ) : portfolios.length === 0 ? (
        <div style={{ padding: '30px', backgroundColor: '#f9f9f9', display: 'inline-block', borderRadius: '8px' }}>
          <p style={{ margin: 0, color: '#222', fontWeight: 'bold' }}>🟢 백엔드 연결 성공!</p>
          <p style={{ margin: '5px 0 0 0', color: '#666', fontSize: '14px' }}>H2 데이터베이스가 비어있어 아직 등록된 포트폴리오는 없습니다.</p>
        </div>
      ) : (
        <ul style={{ listStyle: 'none', padding: 0, maxWidth: '600px', margin: '0 auto', textAlign: 'left' }}>
          {portfolios.map((pf, index) => (
            <li key={index} style={{ padding: '15px', border: '1px solid #ddd', borderRadius: '8px', marginBottom: '10px' }}>
              <h3 style={{ margin: '0 0 10px 0' }}>{pf.title}</h3>
              <p style={{ margin: 0, color: '#555' }}>{pf.introduction}</p>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export default App
