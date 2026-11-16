import { useEffect, useState } from 'react';
import { Phone } from './Phone';
import { Reveal } from './primitives';

const SLIDES = [
  {
    tag: 'Lektion',
    title: 'Der Weg nach vorn ist der einzige',
    text: 'In einer Lektion verschwindet die Tab-Leiste. Du hörst, verstehst, sprichst – und der Weiter-Knopf wartet, bis die Aufgabe gelöst ist.',
    src: '/screenshots/lektion.png',
    alt: 'Lektionsbildschirm mit Hörtext, Modellsatz und großen Bedienelementen',
  },
  {
    tag: 'Üben',
    title: 'Oben steht, was gerade ansteht',
    text: 'Oben steht, was gerade ansteht – fällige Sätze, wackelige Stellen – und welche Runde dazu passt. Jede Karte nennt die Bereiche, die sie trainiert.',
    src: '/screenshots/ueben.png',
    alt: 'Übungsbildschirm mit vier Übungsarten und der Angabe, was sie trainieren',
  },
  {
    tag: 'Profil',
    title: 'Fünf Bereiche, vier Stufen – in Worten',
    text: 'Hören, Grammatik, Bausteine, Sprechen, Verknüpfen. Jeder Bereich nennt eine Stufe und den nächsten Schritt, keine Prozentzahl, keine Note.',
    src: '/screenshots/sprachprofil.png',
    alt: 'Sprachprofil mit fünf Bereichen und ihren Stufen in Worten',
  },
  {
    tag: 'Sammlung',
    title: 'Was du gesagt hast, bleibt da',
    text: 'Alle Wendungen an einem Ort – durchsuchbar, nach „sicher“ und „wackelig“ gefiltert und am Stück anhörbar. Eigene Aufnahmen lassen sich einzeln löschen.',
    src: '/screenshots/sammlung.png',
    alt: 'Sammlung mit gesammelten Sätzen, Suche, Filtern und der Stufe je Satz',
  },
];

export function PhoneTour() {
  const [active, setActive] = useState(0);

  useEffect(() => {
    const blocks = Array.from(document.querySelectorAll<HTMLElement>('[data-tour-block]'));
    if (!blocks.length || typeof IntersectionObserver === 'undefined') return;

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (!entry.isIntersecting) return;
          const index = Number((entry.target as HTMLElement).dataset.tourBlock);
          if (!Number.isNaN(index)) setActive(index);
        });
      },
      { rootMargin: '-45% 0px -45% 0px' },
    );

    blocks.forEach((block) => observer.observe(block));
    return () => observer.disconnect();
  }, []);

  return (
    <section className="section section--divided" id="ansicht">
      <div className="shell">
        <Reveal>
          <div className="section__head">
            <p className="kicker">Echte Aufnahmen</p>
            <h2 className="h2">So sieht die App aus</h2>
            <p className="lead">
              Echte Aufnahmen aus der App – keine nachgebaute Attrappe. Kleinigkeiten können sich
              mit Updates ändern.
            </p>
          </div>
        </Reveal>

        <div className="tour">
          <div className="tour__stage" aria-live="off">
            <div className="tour__stack">
              {SLIDES.map((slide, index) => (
                <div
                  key={slide.src}
                  className={`tour__shot ${index === active ? 'tour__shot--active' : ''}`.trim()}
                  aria-hidden={index !== active}
                >
                  <Phone src={slide.src} alt={slide.alt} />
                </div>
              ))}
            </div>
          </div>

          <div className="tour__blocks">
            {SLIDES.map((slide, index) => (
              <div key={slide.src} className="tour__block" data-tour-block={index}>
                <div className="tour__phone-mobile">
                  <Phone src={slide.src} alt={slide.alt} />
                </div>
                <span className="tour__block-tag">{slide.tag}</span>
                <h3>{slide.title}</h3>
                <p>{slide.text}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
