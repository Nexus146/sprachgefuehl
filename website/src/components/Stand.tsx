import { Garden, gardenStage } from './Garden';
import { Reveal, useInView, useScrollProgress } from './primitives';

const AREAS = [
  { name: 'Hören', value: 68, stage: 'Du baust auf' },
  { name: 'Grammatik', value: 54, stage: 'Du baust auf' },
  { name: 'Bausteine', value: 61, stage: 'Du baust auf' },
  { name: 'Sprechen', value: 47, stage: 'Wir fangen an' },
  { name: 'Verknüpfen', value: 39, stage: 'Wir fangen an' },
];

const RING_RADIUS = 46;
const RING_LENGTH = 2 * Math.PI * RING_RADIUS;

export function Stand() {
  const { ref: gardenRef, progress } = useScrollProgress<HTMLDivElement>();
  const { ref: barsRef, inView: barsVisible } = useInView<HTMLUListElement>();
  const { ref: ringRef, inView: ringVisible } = useInView<HTMLDivElement>();
  const stage = gardenStage(progress);

  return (
    <section className="section section--divided" id="stand">
      <div className="shell">
        <Reveal>
          <div className="section__head">
            <p className="kicker">Stand</p>
            <h2 className="h2">Fortschritt in Worten, nicht in Punkten</h2>
            <p className="lead">
              Der Garten zeigt, dass etwas wächst. Das Sprachprofil zeigt, wo du stehst – in fünf
              Bereichen, mit Stufen wie „Du baust auf“ statt einer Zahl.
            </p>
          </div>
        </Reveal>

        <div className="stand">
          <div className="stand__garden" ref={gardenRef}>
            <div className="card-head">
              <h3>Dein Sprachgarten</h3>
              <span className="badge">Beispiel</span>
            </div>
            <div className="garden-stage">
              <Garden progress={progress} />
              <p className="garden-stage__label">{stage}</p>
              <p className="garden-stage__note">
                8 Runden · 132 Minuten · 8 aktive Tage. Du siehst nur, was war – kein Tag ist
                Pflicht.
              </p>
            </div>
          </div>

          <div className="stand__side">
            <div className="card">
              <div className="card-head">
                <h3>Sprachprofil</h3>
                <span className="badge">Beispielprofil</span>
              </div>
              <ul className="bars" ref={barsRef}>
                {AREAS.map((area) => (
                  <li key={area.name}>
                    <div className="bar-row__head">
                      <span>{area.name}</span>
                      <span className="bar-row__stage">{area.stage}</span>
                    </div>
                    <div className="bar">
                      <div
                        className="bar__fill"
                        style={{ width: barsVisible ? `${area.value}%` : '0%' }}
                      />
                    </div>
                  </li>
                ))}
              </ul>
            </div>

            <div className="card ring-card" ref={ringRef}>
              <div className="ring">
                <svg viewBox="0 0 112 112" aria-hidden="true">
                  <circle className="ring__track" cx="56" cy="56" r={RING_RADIUS} fill="none" strokeWidth="10" />
                  <circle
                    className="ring__value"
                    cx="56"
                    cy="56"
                    r={RING_RADIUS}
                    fill="none"
                    strokeWidth="10"
                    strokeDasharray={RING_LENGTH}
                    strokeDashoffset={ringVisible ? 0 : RING_LENGTH}
                  />
                </svg>
                <span className="ring__label">
                  10 von 10
                  <small>Minuten</small>
                </span>
              </div>
              <div>
                <h3>Ruhiges Tagesende</h3>
                <p>
                  Ist das Ziel erreicht, sagt die App „Für heute reicht es“. Die Minuten sind
                  gemessen, nicht geschätzt – und der Ring füllt sich nicht über das Ziel hinaus.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
