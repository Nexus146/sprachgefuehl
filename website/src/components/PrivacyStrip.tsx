import { Reveal } from './primitives';

export function PrivacyStrip() {
  return (
    <section className="section" id="ohne-konto">
      <div className="shell">
        <Reveal>
          <div className="privacy__panel">
            <div>
              <p className="kicker">Datenschutz</p>
              <h2>Ohne Konto verlässt nichts dein Handy.</h2>
              <p>
                Keine Werbung, keine Analyse, keine Crash-Berichte, keine Profile. Die
                Internet-Berechtigung existiert nur für die freiwillige Anmeldung und die
                Cloud-Sicherung – ohne Konto wird sie nicht genutzt.
              </p>
              <div className="button-row" style={{ marginTop: '20px' }}>
                <a className="btn btn--primary" href="/datenschutz">
                  Datenschutzerklärung lesen
                </a>
              </div>
            </div>

            <ul className="privacy__list">
              <li>Fortschritt und Aufnahmen bleiben im privaten App-Ordner.</li>
              <li>Löschen direkt in der App: Fortschritt zurücksetzen oder Konto löschen.</li>
              <li>Kein Verkauf, keine Weitergabe zu Werbezwecken.</li>
              <li>Verschlüsselte Übertragung nur bei aktivem Cloud-Sync.</li>
            </ul>
          </div>
        </Reveal>
      </div>
    </section>
  );
}
