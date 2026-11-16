import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { App } from './App';
import './styles/tokens.css';
import './styles/site.css';

const root = document.getElementById('root');

if (!root) {
  throw new Error('Wurzelelement fehlt');
}

createRoot(root).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
