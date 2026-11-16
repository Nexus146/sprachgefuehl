import { Reveal } from './primitives';

export function Craft() {
  return (
    <section className="section">
      <div className="shell">
        <Reveal>
          <div className="section__head">
            <p className="kicker">Stimme, Schrift, Daten</p>
            <h2 className="h2">Groß genug, ruhig genug, gemessen</h2>
            <p className="lead">
              Die App ist für Erwachsene gedacht, die ohne Druck lernen – auch mit großer Schrift,
              im Dunkeln und mit wenig Geduld für kleine Knöpfe.
            </p>
          </div>
        </Reveal>

        <div className="craft">
          <Reveal delay={40}>
            <article className="card">
              <h3>Eine Stimme, eine Einstellung</h3>
              <p>
                Langsam, normal oder zügig – die Wahl gilt für alle Modellsätze. In Üben, Lektion
                und Prüfung gibt es zusätzlich einen „Langsam“-Knopf, damit ein einzelner Satz auch
                ohne Umbau erreichbar bleibt.
              </p>
            </article>
          </Reveal>
          <Reveal delay={100}>
            <article className="card">
              <h3>Große Schrift in drei Stufen</h3>
              <p>
                Der Inhalt bleibt in einer ruhigen Spalte. Bei großer Schrift stapeln sich Karten
                und Ringe, statt Text zu quetschen – auch auf dem Tablet.
              </p>
            </article>
          </Reveal>
          <Reveal delay={160}>
            <article className="card">
              <h3>Kontraste als Test</h3>
              <p>
                Hell und dunkel mit eigenen Gartentönen: mindestens 4,5:1 für Text, 3:1 für Kanten,
                Symbole und Knöpfe sind auf Mindestkontrast geprüft. Automatische Tests bewachen
                die Farben beider Darstellungen.
              </p>
            </article>
          </Reveal>
        </div>
      </div>
    </section>
  );
}
