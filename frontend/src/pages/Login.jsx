import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import './Login.css';

const LoginPage = ({ onLogin }) => {
  const navigate = useNavigate();
  const location = useLocation();
  
  const signupSuccess = location.state?.signupSuccess;
  
  const [email, setEmail] = useState('user@test.com');
  const [password, setPassword] = useState('user1234');
  const [error, setError] = useState('');

  const handleLogoClick = () => {
    navigate('/');
  };

  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await onLogin(email, password);
      navigate('/');
    } catch (err) {
      setError(err.message || '로그인에 실패했습니다.');
    }
  };

  return (
    <div className="loginContainer">
      <div className="whiteBox">
        <div 
          className="leftImage" 
          style={{ backgroundColor: '#000000', width: '100%', height: '100%' }}
        >
        </div>
        
        <div className="rightForm">
          <div 
            className="logo-placeholder" 
            onClick={handleLogoClick} 
            style={{ marginBottom: '20px', cursor: 'pointer', textAlign: 'center' }}
          >
            <span className="logo-text">AIfolio</span>
          </div>

          <form onSubmit={handleLoginSubmit}>
            <input
              type="text"
              placeholder="이메일"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
            
            <input
              type="password"
              placeholder="비밀번호"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />

            {signupSuccess && (
              <p style={{ color: 'green', fontSize: '0.85rem', margin: '4px 0' }}>
                회원가입이 완료되었습니다. 로그인해주세요.
              </p>
            )}
            {error && (
              <p style={{ color: '#dc2626', fontSize: '0.85rem', margin: '4px 0' }}>
                {error}
              </p>
            )}
            
            <button className="btnPrimary" type="submit">
              로그인
            </button>
          </form>
          
          <button className="btnSecondary" onClick={() => navigate('/signup')}>
            회원가입
          </button>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;
