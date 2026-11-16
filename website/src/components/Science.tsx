import { Reveal } from './primitives';

const PRINCIPLES = [
  {
    title: 'Verstehbares Input',
    text: 'Dialoge mit Bild und Situation. Deutsch ist Hilfe, nicht Umweg.',
  },
  {
    title: 'Noticing',
    text: 'Die Zielform wird vor dem Üben markiert und mit der eigenen Sprache kontrastiert.',
  },
  {
    title: 'Abrufübung und Spacing',
    text: 'Ein FSRS-inspirierter Scheduler auf gesprochene Bausteine, in wechselnden Kontexten.',
  },
  {
    title: 'Prozeduralisierung',
    text: 'Fünf Stufen: erkennen, mit Stütze, ohne Stütze, flüssig, übertragen.',
  },
  {
    title: 'Korrektur mit Reparatur',
    text: 'Prompt, eigene Reparatur, Erklärung, Transfer – und später ein Retest.',
  },
  {
    title: 'Task-based Learning',
    text: 'Jede Lektion hat ein kommunikatives Ziel, nicht nur eine Übung.',
  },
  {
    title: 'Aussprache',
    text: 'Mehrere Stimmen, Minimalpaare, ehrliches Drei-Stufen-Feedback statt Prozentzahl.',
  },
];

const CITATIONS = [
  'Li 2010',
  'Lyster & Saito 2010',
  'Ellis, Loewen & Erlam 2006',
  'Nassaji 2011',
  'Bryfonski & McKay 2019',
  'Kang, Sok & Han 2019',
  'Uchihara et al. 2025',
  'Nakata 2017',
  'Gomez-Hernandez et al. 2023',
];

export function Science() {
  return (
    <section className="section section--divided" id="wissenschaft">
      <div className="shell">
        <Reveal>
          <div className="section__head">
            <p className="kicker">Wissenschaftlich gebaut</p>
            <h2 className="h2">Sieben Prinzipien, kein Versprechen</h2>
            <p className="lead">
              Die Aufgaben folgen etablierter Zweitsprachenforschung. Was die App nicht kann, steht
              darunter genauso deutlich.
            </p>
          </div>
        </Reveal>

        <div className="principles">
          {PRINCIPLES.map((principle, index) => (
            <Reveal key={principle.title} delay={index * 50}>
              <article className="principle">
                <span className="principle__num" aria-hidden="true">
                  {String(index + 1).padStart(2, '0')}
                </span>
                <h3>{principle.title}</h3>
                <p>{principle.text}</p>
              </article>
            </Reveal>
          ))}
        </div>

        <Reveal>
          <ul className="citations" aria-label="Belege">
            {CITATIONS.map((citation) => (
              <li key={citation}>{citation}</li>
            ))}
          </ul>
        </Reveal>

        <Reveal delay={80}>
          <div className="limits">
            <h3>Grenzen, ehrlich gesagt</h3>
            <ul>
              <li>
                Spracherkennung hängt vom Gerät ab und versteht ältere Stimmen unterschiedlich gut.
                Deshalb gibt es nie ein blockierendes „falsch“, sondern drei Stufen und einen
                Selbstcheck als Rückfallebene.
              </li>
              <li>
                TTS-Stimmen klingen je nach Handy anders. Fehlt eine englische Stimme, zeigt die App
                einen Einrichtungshinweis.
              </li>
              <li>
                Die Inhalte sind fachlich strukturiert, aber nicht von einer Lehrkraft redigiert.
                Ein Prüfbogen fürs Gegenlesen liegt im Projekt.
              </li>
              <li>
                Die App ersetzt kein echtes Gespräch und keinen Kurs. Sie übt den Moment davor.
              </li>
            </ul>
          </div>
        </Reveal>
      </div>
    </section>
  );
}
