import { useState, useRef } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

function MarkdownEditor() {
  const [text, setText] = useState('');
  const textareaRef = useRef(null); 

  const insertMarkdown = (prefix, suffix = '') => {
    const textarea = textareaRef.current;
    if (!textarea) return;

    const start = textarea.selectionStart;
    const end = textarea.selectionEnd;
    const selectedText = text.substring(start, end);
    
    const newText = text.substring(0, start) + prefix + selectedText + suffix + text.substring(end);
    setText(newText);

    setTimeout(() => {
      textarea.focus();
      textarea.setSelectionRange(start + prefix.length, end + prefix.length);
    }, 0);
  };

  const boxStyle = {
    width: '100%',             
    height: '600px',           
    padding: '20px',
    border: '2px solid black', 
    borderRadius: '20px',    
    fontSize: '16px',
    backgroundColor: '#ffffff', 
    color: '#000000',         
    overflow: 'auto',          
    boxSizing: 'border-box'    
  };

  const titleStyle = {
    margin: '0 0 10px 0',      
    fontSize: '24px', 
    fontWeight: 'bold',
    color: '#333',
    textAlign: 'left'
  };

  const buttonStyle = {
    padding: '6px 12px',
    marginRight: '8px',
    marginBottom: '10px',
    backgroundColor: '#333',
    color: 'white',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
    fontWeight: 'bold'
  };

  return (
    <div style={{ 
        display: 'flex', 
        flexDirection: 'column', 
        padding: '20px', 
        gap: '30px',             
        backgroundColor: '#f5f5f5',
        textAlign: 'left'
        }}>

      <style>{`
        .markdown-preview h1 { 
          font-size: 28px !important; 
          color: black !important; 
          margin: 0 0 16px 0 !important; 
          line-height: 1.3 !important; 
          }

        .markdown-preview h2 { 
          font-size: 22px !important; 
          color: black !important; 
          border-bottom: 1px solid #ccc; 
          padding-bottom: 8px; 
          margin-top: 24px; 
          }

        .markdown-preview table { 
          width: 100%; 
          border-collapse: collapse; 
          margin: 16px 0; 
          }

        .markdown-preview th, .markdown-preview td { 
          border: 1px solid #aaa; 
          padding: 8px; 
          text-align: left; 
          }
        .markdown-preview th { background-color: #f0f0f0; }
        .markdown-preview ul { padding-left: 20px; }
        .markdown-preview input[type="checkbox"] { margin-right: 8px; cursor: pointer; }
      `}</style>

      {/* 작성 구역 */}
      <div>
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'flex-end' 
          }}>

          <h3 style={titleStyle}>작성</h3>
          
          {/* 툴바 버튼 */}
          <div>
            <button style={buttonStyle} onClick={() => insertMarkdown('# ', '')}>H1 (대제목)</button>
            <button style={buttonStyle} onClick={() => insertMarkdown('## ', '')}>H2 (중제목)</button>
            <button style={buttonStyle} onClick={() => insertMarkdown('**', '**')}>B (굵게)</button>
            <button style={buttonStyle} onClick={() => insertMarkdown('- [ ] ', '')}>체크박스</button>
            <button style={buttonStyle} onClick={() => insertMarkdown('```\n', '\n```')}>코드블록</button>
            <button style={buttonStyle} onClick={() => insertMarkdown('[링크 이름](', ')')}>링크</button>
            <button style={buttonStyle} onClick={() => insertMarkdown('> ', '')}>인용구</button>
            <button style={buttonStyle} onClick={() => insertMarkdown('\n---\n\n', '')}>구분선</button>
          </div>
        </div>

        <textarea
          ref={textareaRef} 
          style={{ 
            ...boxStyle, 
            resize: 'none', 
            outline: 'none' 
          }}
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="툴바 버튼을 누르거나 마크다운 문법을 직접 입력해보세요"
        />
      </div>

      {/* 미리보기 구역 */}
      <div>
        <h3 style={titleStyle}>미리보기</h3>
        <div style={{ ...boxStyle }} className="markdown-preview">
          <ReactMarkdown remarkPlugins={[remarkGfm]}>
            {text}
          </ReactMarkdown>
        </div>
      </div>
      
    </div>
  );
}

export default MarkdownEditor;