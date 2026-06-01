import { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';

function MarkdownEditor() {
  const [text, setText] = useState('');

  const boxStyle = {
    flex: 1,
    padding: '20px',
    border: '2px solid black', 
    borderRadius: '20px',    
    fontSize: '16px',
    backgroundColor: '#ffffff', 
    color: '#000000',         
    overflow: 'auto',        
    boxSizing: 'border-box'    
  };

  const columnStyle = {
    flex: 1,
    display: 'flex',
    flexDirection: 'column', 
    gap: '10px'              
  };

  const titleStyle = {
    margin: '0',
    fontSize: '24px', 
    fontWeight: 'bold',
    color: '#333'
  };

  return (
    <div style={{ 
        display: 'flex', 
        height: '100vh', 
        padding: '20px', 
        gap: '20px',
        backgroundColor: '#f5f5f5'
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

        .markdown-preview th {
          background-color: #f0f0f0;
        }

        .markdown-preview ul {
          padding-left: 20px;
        }

        .markdown-preview input[type="checkbox"] {
          margin-right: 8px;
          cursor: pointer;
        }
      `}</style>

      <div style={columnStyle}>
        <h3 style={titleStyle}>작성</h3>
        <textarea
          style={{
              ...boxStyle, 
              resize: 'none', 
              outline: 'none' 
              }}
          value={text}
          onChange={(e) => setText(e.target.value)}
        />
      </div>

      <div style={columnStyle}>
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