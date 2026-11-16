import { datenschutzHtml } from '../data/datenschutz.generated';

export function Datenschutz() {
  return (
    <>
      <header className="nav">
        <div className="shell nav__inner">
          <a className="brand" href="/">
            <img className="brand__icon" src="/icon-512.png" alt="" width={512} height={512} />
            Sprachgefühl
          </a>
          <div className="nav__actions">
            <a className="btn btn--ghost btn--small" href="/">
              Zur Startseite
            </a>
          </div>
        </div>
      </header>

      <main className="doc">
        <div className="shell">
          <div className="doc__inner">
            <a className="doc__back" href="/">
              ← Zurück zur Startseite
            </a>

            {/* Der Text kommt aus docs/datenschutz.md – eine Quelle für App, Store-Link und Website. */}
            <article dangerouslySetInnerHTML={{ __html: datenschutzHtml }} />

            <div className="doc__note">
              <p>
                Diese Seite setzt keine Cookies ein, lädt keine Schriftarten oder Skripte von
                Dritten und verwendet keine Analyse-Werkzeuge.
              </p>
            </div>
          </div>
        </div>
      </main>
    </>
  );
}
