import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Signup.css';
import { api } from '../api';

const SignupPage = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);

  const [formData, setFormData] = useState({
    // 1
    name: '',
    age: '',
    email: '',
    password: '',
    // 2
    education: '',
    experience: '', // 선택
    certification: '', // 선택
    techStack: '',
    //
    residence: '',
    desiredWorkplace: '',
    interestJob1: '',
    interestJob2: '', // 선택
    interestJob3: ''  // 선택
  });
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value
    }));
  };

  const handleLogoClick = () => {
    navigate('/');
  };

  const handleNextStep = (e) => {
    e.preventDefault();
    setStep((prev) => prev + 1);
  };

  const handlePrevStep = () => {
    setStep((prev) => prev - 1);
  };

  const handleSignupSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await api.signup({
        name: formData.name,
        email: formData.email,
        password: formData.password,
      });
      navigate('/login', { state: { signupSuccess: true } });
    } catch (err) {
      setError(err.message || '회원가입에 실패했습니다.');
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
            style={{ marginBottom: '10px', cursor: 'pointer', textAlign: 'center' }}
          >
            <span className="logo-text">AIfolio</span>
          </div>

          <div style={{ textAlign: 'center', marginBottom: '20px', color: '#666', fontSize: '0.9rem' }}>
            <span style={{ fontWeight: step === 1 ? 'bold' : 'normal', color: step === 1 ? '#000' : '#aaa' }}>1</span> {' > '}
            <span style={{ fontWeight: step === 2 ? 'bold' : 'normal', color: step === 2 ? '#000' : '#aaa' }}>2</span> {' > '}
            <span style={{ fontWeight: step === 3 ? 'bold' : 'normal', color: step === 3 ? '#000' : '#aaa' }}>3</span>
          </div>
          {error && <p style={{ color: '#dc2626', fontSize: '0.85rem', margin: '4px 0' }}>{error}</p>}

          {step === 1 && (
            <form onSubmit={handleNextStep}>
              <input
                type="text"
                name="name"
                placeholder="이름"
                value={formData.name}
                onChange={handleChange}
                required
              />
              <input
                type="number"
                name="age"
                placeholder="나이"
                value={formData.age}
                onChange={handleChange}
                required
              />
              <input
                type="email"
                name="email"
                placeholder="이메일"
                value={formData.email}
                onChange={handleChange}
                required
              />
              <input
                type="password"
                name="password"
                placeholder="비밀번호"
                value={formData.password}
                onChange={handleChange}
                required
              />
              <button className="btnPrimary" type="submit">다음 단계</button>
            </form>
          )}

          {step === 2 && (
            <form onSubmit={handleNextStep}>
              <input
                type="text"
                name="education"
                placeholder="학력"
                value={formData.education}
                onChange={handleChange}
                required
              />
              <input
                type="text"
                name="experience"
                placeholder="경력 (선택)"
                value={formData.experience}
                onChange={handleChange}
                // required 속성 제거함
              />
              <input
                type="text"
                name="certification"
                placeholder="자격증 (선택)"
                value={formData.certification}
                onChange={handleChange}
              />
              <input
                type="text"
                name="techStack"
                placeholder="업무 스택"
                value={formData.techStack}
                onChange={handleChange}
                required
              />
              <div style={{ display: 'flex', gap: '10px' }}>
                <button className="btnSecondary" type="button" onClick={handlePrevStep} style={{ flex: 1 }}>이전</button>
                <button className="btnPrimary" type="submit" style={{ flex: 1 }}>다음 단계</button>
              </div>
            </form>
          )}

          {step === 3 && (
            <form onSubmit={handleSignupSubmit}>
              <input
                type="text"
                name="residence"
                placeholder="거주지"
                value={formData.residence}
                onChange={handleChange}
                required
              />
              <input
                type="text"
                name="desiredWorkplace"
                placeholder="근로희망지"
                value={formData.desiredWorkplace}
                onChange={handleChange}
                required
              />
              <input
                type="text"
                name="interestJob1"
                placeholder="관심직군 1"
                value={formData.interestJob1}
                onChange={handleChange}
                required
              />
              <input
                type="text"
                name="interestJob2"
                placeholder="관심직군 2 (선택)"
                value={formData.interestJob2}
                onChange={handleChange}
              />
              <input
                type="text"
                name="interestJob3"
                placeholder="관심직군 3 (선택)"
                value={formData.interestJob3}
                onChange={handleChange}
              />
              <div style={{ display: 'flex', gap: '10px' }}>
                <button className="btnSecondary" type="button" onClick={handlePrevStep} style={{ flex: 1 }}>이전</button>
                <button className="btnPrimary" type="submit" style={{ flex: 1 }}>가입 완료</button>
              </div>
            </form>
          )}

          {step === 1 && (
            <button className="btnSecondary" onClick={() => navigate('/login')} style={{ marginTop: '10px' }}>
              이미 계정이 있으신가요? 로그인
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default SignupPage;
