import { stats } from '../data/content.generated';
import { CountUp, Reveal } from './primitives';

const NUMBERS = [
  { label: 'Lektionen', value: stats.lessons },
  { label: 'Bausteine', value: stats.chunks },
  { label: 'Muster', value: stats.patterns },
  { label: 'Kann-Sätze', value: stats.canDo },
  { label: 'Situationen', value: stats.situations },
  { label: 'Erzählrahmen', value: stats.variants },
  { label: 'Fehlerkarten', value: stats.errorCards },
  { label: 'Minimalpaare', value: stats.minimalPairs },
];

export function Numbers() {
  return (
    <section className="numbers" aria-label="Zahlen zur App">
      <div className="shell">
        <ul className="numbers__grid">
          {NUMBERS.map((number, index) => (
            <li className="number" key={number.label}>
              <Reveal delay={index * 40}>
                <span className="number__value">
                  <CountUp value={number.value} />
                </span>
                <span className="number__label">{number.label}</span>
              </Reveal>
            </li>
          ))}
        </ul>
        <p className="numbers__note">
          {stats.chapters} Kapitel · {stats.checkup} Prüfaufgaben der Standortbestimmung ·{' '}
          {stats.personas} Personen im Viertel. Alle Zahlen stammen aus dem Inhalt der App.
        </p>
      </div>
    </section>
  );
}
