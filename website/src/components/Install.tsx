import { stats } from '../data/content.generated';
import { OBTAINIUM_URL, REPO_URL } from '../data/links';
import { Reveal } from './primitives';

const STEPS = [
  {
    title: 'Release öffnen und herunterladen',
    text: 'Im Repository die Releases öffnen und die Datei sprachgefuehl-*.apk wählen. Der Weg bleibt für alle Updates derselbe.',
  },
  {
    title: 'Installation aus dieser Quelle erlauben',
    text: 'Antippen, auf Nachfrage „Trotzdem herunterladen“ und „Installation aus dieser Quelle erlauben“. Play Protect warnt bei Apps außerhalb des Stores – das ist normal: Mehr Details → Trotzdem installieren.',
  },
  {
    title: 'Drüberinstallieren, nicht deinstallieren',
    text: 'Updates kommen als neue APK oder über Obtainium. Wer drüberinstalliert, behält Fortschritt und Aufnahmen.',
  },
];

export function Install() {
  return (
    <section className="section section--divided" id="installation">
      <div className="shell">
        <Reveal>
          <div className="section__head">
            <p className="kicker">Installation</p>
            <h2 className="h2">Direkt aus dem Projekt</h2>
            <p className="lead">
              Sprachgefühl erscheint als signierte APK. Der Link bleibt für alle Updates derselbe –
              ein Store ist dafür nicht nötig.
            </p>
          </div>
        </Reveal>

        <div className="install">
          <div>
            <div className="install__steps">
              {STEPS.map((step, index) => (
                <Reveal key={step.title} delay={index * 70}>
                  <div className="install-step">
                    <span className="install-step__num" aria-hidden="true">
                      {index + 1}
                    </span>
                    <div>
                      <h3>{step.title}</h3>
                      <p>{step.text}</p>
                    </div>
                  </div>
                </Reveal>
              ))}
            </div>

            <div className="button-row" style={{ marginTop: '24px' }}>
              <a className="btn btn--primary" href={REPO_URL}>
                Zum Repository
              </a>
              <a className="btn btn--ghost" href={OBTAINIUM_URL}>
                Obtainium
              </a>
            </div>
          </div>

          <div className="install__side">
            <Reveal delay={80}>
              <div className="card">
                <h3>Was du brauchst</h3>
                <ul className="req">
                  <li>
                    Android <strong>{stats.minAndroid} oder neuer</strong>
                  </li>
                  <li>
                    Preis <strong>kostenlos</strong>
                  </li>
                  <li>
                    Konto <strong>freiwillig</strong>
                  </li>
                  <li>
                    Internet <strong>nur für die Cloud-Sicherung</strong>
                  </li>
                </ul>
              </div>
            </Reveal>

            <Reveal delay={140}>
              <div className="card">
                <h3>Gut zu wissen</h3>
                <p style={{ fontSize: '16px', color: 'var(--muted)' }}>
                  Ohne Konto arbeitet die App vollständig offline. Beim Gerätewechsel zieht der
                  Fortschritt per Sicherungsdatei um – oder über ein optionales Konto.
                </p>
              </div>
            </Reveal>
          </div>
        </div>
      </div>
    </section>
  );
}
