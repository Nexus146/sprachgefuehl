import { Fragment, useState } from 'react';
import { Reveal } from './primitives';

const ENTRIES = {
  could: {
    word: 'could',
    meaning: '„könnte“ – die höfliche Form von „can“.',
    note: 'Im Laden und am Telefon klingt sie weicher und weniger fordernd.',
  },
  have: {
    word: 'have',
    meaning: '„bekommen“ – hier nicht „besitzen“.',
    note: 'Die Wendung ist „Could I have …?“. Für Besitz fragt man „Do you have …?“',
  },
  glass: {
    word: 'glass',
    meaning: '„ein Glas“ – das Trinkgefäß.',
    note: 'Die Brille heißt „glasses“: fast dasselbe Wort, aber im Plural.',
  },
  water: {
    word: 'water',
    meaning: '„Wasser“ – still water ist ohne Kohlensäure.',
    note: 'Im Restaurant fragt man oft: „Still or sparkling?“',
  },
  please: {
    word: 'please',
    meaning: '„bitte“ – am Satzende freundlich, nicht unterwürfig.',
    note: 'Zusammen mit „Could“ braucht es kein zweites „bitte“.',
  },
} as const;

type EntryKey = keyof typeof ENTRIES;

type Token = { word: string; entry?: EntryKey; tail?: string };

const TOKENS: Token[] = [
  { word: 'Could', entry: 'could' },
  { word: 'I' },
  { word: 'have', entry: 'have' },
  { word: 'a' },
  { word: 'glass', entry: 'glass' },
  { word: 'of' },
  { word: 'water', entry: 'water', tail: ',' },
  { word: 'please', entry: 'please', tail: '?' },
];

export function GlossaryDemo() {
  const [selected, setSelected] = useState<EntryKey | null>(null);
  const entry = selected ? ENTRIES[selected] : null;

  return (
    <section className="section section--divided" id="woerter">
      <div className="shell">
        <div className="section__head">
          <Reveal>
            <p className="kicker">Wörter antippen</p>
          </Reveal>
          <Reveal delay={60}>
            <h2 className="h2">Bekannte Wörter sind gepunktet</h2>
          </Reveal>
          <Reveal delay={120}>
            <p className="lead">
              In Modellsätzen unterstreicht die App nur Wörter, für die sie wirklich eine Bedeutung
              hat. Tippe hier auf eines – in der App spricht die Stimme es zusätzlich vor.
            </p>
          </Reveal>
        </div>

        <Reveal delay={60}>
          <div className="gloss-card">
            <p className="gloss-sentence">
              {TOKENS.map((token, index) => (
                <Fragment key={`${token.word}-${index}`}>
                  {index > 0 ? ' ' : null}
                  {token.entry ? (
                    <button
                      type="button"
                      className={`gloss ${selected === token.entry ? 'gloss--active' : ''}`.trim()}
                      aria-expanded={selected === token.entry}
                      onClick={() => setSelected(selected === token.entry ? null : token.entry!)}
                    >
                      {token.word}
                    </button>
                  ) : (
                    token.word
                  )}
                  {token.tail}
                </Fragment>
              ))}
            </p>

            <div className="gloss-panel" aria-live="polite">
              {entry ? (
                <>
                  <span className="gloss-panel__word">{entry.word}</span>
                  <p className="gloss-panel__meaning">{entry.meaning}</p>
                  <p className="gloss-panel__note">{entry.note}</p>
                </>
              ) : (
                <p className="gloss-panel__note">
                  Tippe ein gepunktetes Wort an – die Bedeutung erscheint hier.
                </p>
              )}
            </div>
          </div>
        </Reveal>
      </div>
    </section>
  );
}
