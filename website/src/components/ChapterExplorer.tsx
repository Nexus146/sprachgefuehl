import { useState } from 'react';
import { chapters, seasons, stats, world } from '../data/content.generated';
import { Reveal } from './primitives';

export function ChapterExplorer() {
  const [openId, setOpenId] = useState<string | null>(chapters[0]?.id ?? null);

  return (
    <section className="section section--divided" id="kapitel">
      <div className="shell">
        <Reveal>
          <div className="section__head">
            <p className="kicker">Dein Viertel</p>
            <h2 className="h2">{stats.chapters} Kapitel, ein Jahr im Viertel</h2>
            <p className="lead">
              {world.intro} Jede Situation hat bis zu {stats.maxVariants} Erzählrahmen: dieselben
              Bausteine, eine andere Geschichte – wer, wo und warum ändern sich, das Lernziel nicht.
            </p>
          </div>
        </Reveal>

        <Reveal>
          <ol className="seasons" aria-label="Die vier Jahreszeiten im Viertel">
            {seasons.map((season) => (
              <li className="season" key={season.id}>
                <span className="season__dot" aria-hidden="true" />
                <h3>{season.title}</h3>
                <p>{season.note}</p>
              </li>
            ))}
          </ol>
        </Reveal>

        <div className="chapters" style={{ marginTop: 'clamp(32px, 5vw, 56px)' }}>
          <div className="chapters__list">
            {chapters.map((chapter) => {
              const open = chapter.id === openId;
              return (
                <div key={chapter.id} className={`chapter ${open ? 'chapter--open' : ''}`.trim()}>
                  <button
                    type="button"
                    className="chapter__head"
                    aria-expanded={open}
                    aria-controls={`chapter-panel-${chapter.id}`}
                    onClick={() => setOpenId(open ? null : chapter.id)}
                  >
                    <span className="chapter__num" aria-hidden="true">
                      {String(chapter.order).padStart(2, '0')}
                    </span>
                    <span>
                      <span className="chapter__title">{chapter.title}</span>
                      <span className="chapter__subtitle">{chapter.subtitle}</span>
                    </span>
                    <span className="chapter__meta">
                      {chapter.situations} Situationen · {chapter.lessons} Lektionen
                    </span>
                    <span className="chapter__chev" aria-hidden="true">
                      +
                    </span>
                  </button>
                  <div className="chapter__panel" id={`chapter-panel-${chapter.id}`}>
                    <div className="chapter__panel-inner">
                      <ul className="chapter__situations">
                        {chapter.detail.map((situation) => (
                          <li key={situation.title}>
                            {situation.title}
                            <span>{situation.lessons} Lektionen</span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
}
