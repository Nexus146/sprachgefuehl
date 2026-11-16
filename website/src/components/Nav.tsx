import { useEffect, useState } from 'react';
import { REPO_URL } from '../data/links';

const LINKS = [
  { id: 'lernweg', label: 'Lernweg' },
  { id: 'kapitel', label: 'Kapitel' },
  { id: 'ueben', label: 'Üben' },
  { id: 'stand', label: 'Stand' },
  { id: 'wissenschaft', label: 'Wissenschaft' },
  { id: 'installation', label: 'Installieren' },
];

export function Nav() {
  const [active, setActive] = useState('lernweg');

  useEffect(() => {
    const sections = LINKS.map((link) => document.getElementById(link.id)).filter(
      (section): section is HTMLElement => section !== null,
    );
    if (!sections.length || typeof IntersectionObserver === 'undefined') return;

    const observer = new IntersectionObserver(
      (entries) => {
        const visible = entries
          .filter((entry) => entry.isIntersecting)
          .sort((a, b) => b.intersectionRatio - a.intersectionRatio)[0];
        if (visible) setActive(visible.target.id);
      },
      { rootMargin: '-30% 0px -55% 0px', threshold: [0, 0.2, 0.6, 1] },
    );

    sections.forEach((section) => observer.observe(section));
    return () => observer.disconnect();
  }, []);

  return (
    <header className="nav">
      <div className="shell nav__inner">
        <a className="brand" href="/">
          <img className="brand__icon" src="/icon-512.png" alt="" width={512} height={512} />
          Sprachgefühl
        </a>

        <nav className="nav__links" aria-label="Abschnitte">
          {LINKS.map((link) => (
            <a
              key={link.id}
              className="nav__link"
              href={`#${link.id}`}
              aria-current={active === link.id ? 'true' : undefined}
            >
              {link.label}
            </a>
          ))}
        </nav>

        <div className="nav__actions">
          <a
            className="btn btn--primary btn--small"
            href={REPO_URL}
          >
            App laden
          </a>
        </div>
      </div>
    </header>
  );
}
