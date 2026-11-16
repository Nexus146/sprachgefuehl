import type { ReactNode } from 'react';
import { stats } from '../data/content.generated';
import { Reveal } from './primitives';

const QUESTIONS: { question: string; answer: ReactNode }[] = [
  {
    question: 'Kostet die App etwas?',
    answer: 'Nein. Kein Abo, keine Käufe, keine Werbung, keine Konten-Pflicht.',
  },
  {
    question: 'Brauche ich ein Konto?',
    answer:
      'Nein. Ohne Anmeldung bleibt alles auf dem Gerät. Ein Konto ist nur für die Cloud-Sicherung da – der Fortschritt zieht sonst per Sicherungsdatei um.',
  },
  {
    question: 'Welche Android-Version brauche ich?',
    answer: `Android ${stats.minAndroid} oder neuer. Die Spracherkennung läuft auf Android 12+ auf dem Gerät, sonst über den Systemdienst.`
  },
  {
    question: 'Wie bekomme ich Updates?',
    answer:
      'Über die Releases-Seite oder mit Obtainium. Wichtig: drüberinstallieren, nicht deinstallieren – dann bleiben Fortschritt und Aufnahmen.',
  },
  {
    question: 'Was passiert mit meinen Aufnahmen?',
    answer:
      'Sie bleiben im privaten App-Ordner und lassen sich in „Meine Sätze“ einzeln löschen. Nur wenn du die Cloud-Sicherung einschaltest, werden sie übertragen.',
  },
  {
    question: 'Ist die App für Kinder?',
    answer:
      'Nein. Sie richtet sich an Erwachsene; es gibt bewusst keine Inhalte für Kinder unter 13 und keine Datenerhebung von Kindern.',
  },
];

export function Faq() {
  return (
    <section className="section section--divided" id="fragen">
      <div className="shell">
        <Reveal>
          <div className="section__head">
            <p className="kicker">Fragen</p>
            <h2 className="h2">Kurz beantwortet</h2>
          </div>
        </Reveal>

        <div className="faq">
          {QUESTIONS.map((item, index) => (
            <Reveal key={item.question} delay={index * 40}>
              <details className="faq__item">
                <summary className="faq__summary">
                  {item.question}
                  <span className="faq__plus" aria-hidden="true">
                    +
                  </span>
                </summary>
                <div className="faq__body">
                  <p>{item.answer}</p>
                </div>
              </details>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  );
}
