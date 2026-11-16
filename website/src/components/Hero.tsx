import { useEffect, useState } from 'react';
import { stats } from '../data/content.generated';
import { REPO_URL } from '../data/links';
import { Garden } from './Garden';
import { Phone } from './Phone';
import { Reveal, prefersReducedMotion } from './primitives';

export function Hero() {
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    if (prefersReducedMotion()) {
      setProgress(0.72);
      return;
    }
    let frame = 0;
    const start = performance.now();
    const target = 0.72;
    const tick = (now: number) => {
      const t = Math.min(1, (now - start) / 1500);
      setProgress(target * (1 - Math.pow(1 - t, 3)));
      if (t < 1) frame = requestAnimationFrame(tick);
    };
    frame = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(frame);
  }, []);

  return (
    <section className="hero" id="start">
      <div className="shell">
        <div className="hero__grid">
          <div className="hero__copy">
            <Reveal>
              <p className="kicker">Android-App · kostenlos und offline</p>
            </Reveal>

            <Reveal delay={70}>
              <h1 className="hero__title">
                Englisch sprechen, <em>ruhig geübt</em>.
              </h1>
            </Reveal>

            <Reveal delay={140}>
              <p className="hero__lead">
                Jede Lektion beginnt mit einem Alltagsgespräch, erklärt eine Form auf Deutsch und
                lässt dich selbst sprechen. Kein Zeitdruck, keine Herzen, keine Rangliste – ein
                Viertel voller Begegnungen, das mit dir wächst.
              </p>
            </Reveal>

            <Reveal delay={210}>
              <div className="button-row">
                <a className="btn btn--primary" href={REPO_URL}>
                  App laden
                </a>
                <a className="btn btn--ghost" href="#lernweg">
                  So funktioniert&rsquo;s
                </a>
              </div>
            </Reveal>

            <Reveal delay={280}>
              <ul className="hero__meta">
                <li>Android {stats.minAndroid} oder neuer</li>
                <li>kostenlos</li>
                <li>offline nutzbar</li>
                <li>Konto freiwillig</li>
              </ul>
            </Reveal>
          </div>

          <div className="hero__stage">
            <div className="hero__arch" aria-hidden="true" />
            <div className="hero__phone">
              <Phone
                src="/screenshots/start.png"
                alt="Startbildschirm der App mit der Empfehlung „Weiter bei Schritt 6 von 17“ und dem Weg durchs Kapitel"
                priority
              />
            </div>
            <div className="hero__garden">
              <Garden progress={progress} />
              <p className="hero__garden-label">
                Der Sprachgarten wächst mit jeder Runde – und verwelkt nie.
              </p>
            </div>
          </div>
        </div>

        <Reveal delay={120}>
          <div className="facts">
            <div className="fact">
              <span className="fact__value">{stats.chapters}</span>
              <span className="fact__label">Kapitel im Viertel</span>
            </div>
            <div className="fact">
              <span className="fact__value">{stats.situations}</span>
              <span className="fact__label">Situationen aus dem Alltag</span>
            </div>
            <div className="fact">
              <span className="fact__value">{stats.lessons}</span>
              <span className="fact__label">Lektionen mit Sprechaufgabe</span>
            </div>
          </div>
        </Reveal>
      </div>
    </section>
  );
}
