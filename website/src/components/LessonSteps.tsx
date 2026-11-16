import { Reveal, useInView } from './primitives';

const STEPS = [
  {
    title: 'Hören und verstehen',
    text: 'Ein kurzer Dialog aus dem Alltag, in zwei Tempi. Die deutsche Hilfe ist da, wenn du sie brauchst – nicht als Umweg.',
  },
  {
    title: 'Die Form dahinter',
    text: 'Eine Grammatikstelle, auf Deutsch erklärt, mit dem Kontrast zu deiner eigenen Sprache.',
  },
  {
    title: 'Bausteine',
    text: 'Formelhafte Wendungen wie „Could I have …?“ – keine einzelnen Wörter, sondern fertige Sätze zum Mitnehmen.',
  },
  {
    title: 'Selbst sprechen',
    text: 'Nachsprechen, Lücken füllen, Rollenspiel – und den Satz in eine neue Situation übertragen.',
  },
  {
    title: 'Fehler verstehen',
    text: 'Sechs Teile statt rotem Kreuz: was passiert ist, warum, wie es richtig geht, Merkanker, Checkliste, nächster Schritt.',
  },
];

export function LessonSteps() {
  const { ref, inView } = useInView<HTMLDivElement>();

  return (
    <section className="section section--divided" id="lernweg">
      <div className="shell">
        <Reveal>
          <div className="section__head">
            <p className="kicker">Eine Lektion</p>
            <h2 className="h2">Fünf Züge, die sonst getrennt sind</h2>
            <p className="lead">
              Hören, Grammatik, Bausteine, Sprechen und Fehler gehören in den meisten Apps in
              verschiedene Rubriken. Hier stehen sie in einer Lektion – in dieser Reihenfolge.
            </p>
          </div>
        </Reveal>

        <div ref={ref} className={`steps-wrap ${inView ? 'is-in' : ''}`.trim()}>
          <div className="steps__rule" aria-hidden="true">
            <span />
          </div>
          <ol className="steps">
            {STEPS.map((step, index) => (
              <li key={step.title}>
                <article className="step" style={{ ['--delay' as string]: `${index * 90}ms` }}>
                  <span className="step__num" aria-hidden="true">
                    {index + 1}
                  </span>
                  <h3>{step.title}</h3>
                  <p>{step.text}</p>
                </article>
              </li>
            ))}
          </ol>
        </div>
      </div>
    </section>
  );
}
