import { Reveal } from './primitives';

const FORMATS = [
  'Baustein neu',
  'Nachsprechen',
  'Grammatikstelle',
  'Noticing',
  'Selbsteinschätzung',
  'Frei sprechen',
  'Dialog hören',
  'Hörverstehen vertiefen',
  'Satz bauen',
  'Verbindungswort wählen',
  'Lücke sprechen',
  'Übertragen',
  'Sätze verbinden',
  'Klangpaar',
  'Schnelles Sprechen',
  'Kurze Nachricht',
  'Antwort auf die Zeile',
  'Satzende wählen',
  'Gespräch ordnen',
];

export function Practice() {
  return (
    <section className="section section--divided" id="ueben">
      <div className="shell">
        <Reveal>
          <div className="section__head">
            <p className="kicker">Üben</p>
            <h2 className="h2">Viele Arten, dasselbe zu können</h2>
            <p className="lead">
              Jede Runde stellt die Aufgabe anders – nicht immer dieselbe Folge gleicher Schritte.
              Geübt wird zuerst, was fällig ist, dann Wackeliges, dann Geschaffenes: nie ein Thema,
              das noch nicht dran war.
            </p>
          </div>
        </Reveal>

        <Reveal>
          <p className="hint" style={{ margin: '0 0 12px' }}>
            Zum Beispiel:
          </p>
          <ul className="chips">
            {FORMATS.map((format) => (
              <li key={format}>{format}</li>
            ))}
          </ul>
        </Reveal>

        <div className="practice__grid">
          <Reveal delay={60}>
            <article className="card">
              <h3>Tagesplan mit Fokus</h3>
              <p>
                Der schwächste Bereich mit mindestens zwei Belegen bestimmt, was heute kommt:
                fünf Minuten sprechen, wiederholen, Grammatik – oder die nächste Lektion.
              </p>
            </article>
          </Reveal>
          <Reveal delay={120}>
            <article className="card">
              <h3>Standortbestimmung mit Pause</h3>
              <p>
                Fünf Blöcke mit adaptiver Treppe, jederzeit unterbrechbar. Danach bietet die
                Startseite an, an der angefangenen Prüfung weiterzumachen.
              </p>
            </article>
          </Reveal>
          <Reveal delay={180}>
            <article className="card">
              <h3>Wiederholen, wie es passt</h3>
              <p>
                Sprechen oder tippen, wenn es gerade laut ist. Am Ende gibt es „noch eine Runde“ –
                und eine angefangene Runde zählt auch beim Verlassen.
              </p>
            </article>
          </Reveal>
        </div>
      </div>
    </section>
  );
}
