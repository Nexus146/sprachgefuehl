import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { Datenschutz } from './pages/Datenschutz';
import './styles/tokens.css';
import './styles/site.css';

const root = document.getElementById('root');

if (!root) {
  throw new Error('Wurzelelement fehlt');
}

createRoot(root).render(
  <StrictMode>
    <Datenschutz />
  </StrictMode>,
);
