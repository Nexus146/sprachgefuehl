import { RELEASES_URL, REPO_URL } from '../data/links';

export function Footer() {
  return (
    <footer className="footer">
      <div className="shell">
        <div className="footer__grid">
          <div>
            <div className="footer__brand">
              <img src="/icon-512.png" alt="" width={512} height={512} />
              Sprachgefühl
            </div>
            <p>
              Eine ruhige Englisch-App für Erwachsene: verstehen, Bausteine sammeln, selbst
              sprechen. Ein privates Projekt, kein Unternehmen.
            </p>
          </div>

          <div>
            <h3>Die App</h3>
            <ul className="footer__links">
              <li>
                <a href="#lernweg">Lernweg</a>
              </li>
              <li>
                <a href="#kapitel">Kapitel</a>
              </li>
              <li>
                <a href="#installation">Installation</a>
              </li>
              <li>
                <a href={REPO_URL}>Repository</a>
              </li>
            </ul>
          </div>

          <div>
            <h3>Rechtliches</h3>
            <ul className="footer__links">
              <li>
                <a href="/datenschutz">Datenschutz</a>
              </li>
              <li>
                <a href={`${REPO_URL}/blob/main/LICENSE`}>Lizenz (Apache-2.0)</a>
              </li>
              <li>
                <a href={REPO_URL}>Quellcode</a>
              </li>
            </ul>
          </div>

          <div>
            <h3>Kontakt</h3>
            <ul className="footer__links">
              <li>
                <a href={`${REPO_URL}/issues`}>Fehler melden</a>
              </li>
              <li>
                <a href={RELEASES_URL}>Alle Fassungen</a>
              </li>
            </ul>
          </div>
        </div>

        <div className="footer__legal">
          <span>Apache-2.0 · kostenlos</span>
          <span>Keine Cookies, keine Analytics, keine Werbung.</span>
          <span>Inhalte für Erwachsene.</span>
        </div>
      </div>
    </footer>
  );
}
