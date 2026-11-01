import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import {
  paths, pillars, connectorLevels, functions, errorCards, minimalPairs, placement,
  world, personas, storyMeta, situationPersonas,
} from './core.mjs';
import { situations1 } from './situations1.mjs';
import { situations2 } from './situations2.mjs';
import { situations3 } from './situations3.mjs';
import { situations4 } from './situations4.mjs';
import { situations5 } from './situations5.mjs';
import { variants } from './variants.mjs';
import { checkup } from './checkup.mjs';

const here = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(here, '..');
const situations = [...situations1, ...situations2, ...situations3, ...situations4, ...situations5];
const staffelZwei = new Set([...situations3, ...situations4].map((s) => s.id));
const pillarAliases = { function_cards: 'modals_politeness' };

const pillarById = new Map(pillars.map((p) => [p.id, p]));
const connectorById = new Map(connectorLevels.map((c) => [c.id, c]));

const lessons = [];
const chunks = [];
/** Which grammar pillar a chunk belongs to: the anchor for recycling. */
const chunkPillar = new Map();
const canDo = [];
const reviewLines = [];

/**
 * A third encounter per lesson: an earlier chunk of the same pattern comes back in a new
 * situation. Recycling beats a new word list – retrieval in a different context is what makes a
 * phrase available in a real conversation.
 */
function recycledChunk(ownIds, pillarId) {
  if (!pillarId) return null;
  return chunks.find((c) => chunkPillar.get(c.id) === pillarId && !ownIds.includes(c.id)) || null;
}

function wordsOf(sentence) {
  return sentence.replace(/\s+/g, ' ').trim().split(' ').filter(Boolean);
}

/**
 * Ein Platzhalter, der sich als Hinweis lohnt. „Sag es mit: ‚it‘“ oder „Setze ‚you‘ ein“ sagt
 * einer Lernenden nichts; solche Funktionswörter taugen nicht als inhaltliche Eselsbrücke.
 * Dann bleibt der Platzhalter im Satz, aber der deutsche Satz trägt den Hinweis.
 */
const cueStopwords = new Set([
  'i', 'you', 'he', 'she', 'it', 'we', 'they', 'me', 'him', 'her', 'us', 'them',
  'my', 'your', 'his', 'its', 'our', 'their', 'mine', 'yours',
  'this', 'that', 'these', 'those', 'there', 'here',
  'a', 'an', 'the', 'some', 'any', 'not', 'no', 'yes',
  'is', 'are', 'was', 'were', 'am', 'be', 'been', 'do', 'does', 'did',
  'to', 'of', 'in', 'on', 'at', 'for', 'with', 'and', 'but', 'or', 'so', 'if',
]);

function cueSlot(chunk) {
  const raw = (chunk.slot || '').trim();
  if (!raw) return '';
  return cueStopwords.has(raw.toLowerCase()) ? '' : raw;
}

for (const situation of situations) {
  reviewLines.push(`\n## ${situation.title} (${situation.pathId})\n`);
  reviewLines.push(`Szene: ${situation.scene}\n`);

  situation.lessons.forEach((lesson, index) => {
    const lessonId = `${situation.id}_l${index + 1}`;
    const pillarId = pillarAliases[lesson.pillarId] || lesson.pillarId;
    const pillar = pillarById.get(pillarId);
    if (!pillar) throw new Error(`Unbekannte Grammatik-Säule: ${pillarId} (${lessonId})`);
    const connectorId = `conn${lesson.connectorLevel}`;
    const connector = connectorById.get(connectorId);
    if (!connector) throw new Error(`Unbekannter Konnektor-Level: ${connectorId}`);

    const chunkIds = lesson.chunks.map((_, i) => `${lessonId}_c${i + 1}`);
    chunkIds.forEach((id) => chunkPillar.set(id, pillarId));
    lesson.chunks.forEach((chunk, i) => {
      chunks.push({
        id: chunkIds[i],
        en: chunk.en,
        de: chunk.de,
        patternId: chunk.patternId,
        slot: chunk.slot,
        tip: chunk.tip,
        level: situation.level,
        tags: [situation.pathId],
        template: chunk.template,
        templateDe: chunk.templateDe,
      });
    });

    const [dialogEn, dialogDe] = [0, 1];
    const steps = [];
    const add = (step) => steps.push({ id: `${lessonId}_s${steps.length + 1}`, ...step });

    add({
      type: 'DialogInput',
      title: 'Hören & verstehen',
      promptDe: situation.scene,
      lines: lesson.dialog.map(([speaker, en, de]) => ({ speaker, en, de })),
    });

    add({
      type: 'ComprehensionDeep',
      title: 'Verstanden?',
      promptDe: 'Hör noch einmal und antworte.',
      questions: lesson.questions,
    });

    add({
      type: 'Noticing',
      title: 'Die Form dahinter',
      promptEn: lesson.chunks[0].en,
      promptDe: lesson.chunks[0].de,
      explanationDe: [lesson.noticing, pillar.explanationDe].filter(Boolean).join(' '),
      note: `${pillar.metaphor} · ${pillar.title}`,
      pillarId,
      highlight: lesson.chunks[0].slot,
    });

    lesson.chunks.forEach((chunk, i) => {
      add({
        type: 'ChunkIntro',
        title: 'Baustein',
        promptEn: chunk.en,
        promptDe: chunk.de,
        chunkId: chunkIds[i],
        patternId: chunk.patternId,
        target: chunk.en,
        tip: chunk.tip,
      });
    });

    // „Sag es mit: ‚it‘“ hilft niemandem – Fürwörter und kleine Funktionswörter taugen nicht
    // als inhaltlicher Hinweis. Dann trägt der deutsche Satz den Hinweis.
    const slotSecond = cueSlot(lesson.chunks[1]);

    const recycled = recycledChunk(chunkIds, pillarId);
    if (recycled) {
      add({
        type: 'ChunkIntro',
        title: 'Wieder getroffen',
        promptEn: recycled.en,
        promptDe: recycled.de,
        chunkId: recycled.id,
        patternId: recycled.patternId,
        target: recycled.en,
        tip: recycled.tip,
      });
      add({
        type: 'SpeakRepeat',
        title: 'Noch einmal',
        promptDe: 'Dieser Baustein kommt wieder – sprich ihn in deinem Tempo.',
        target: recycled.en,
        accept: [recycled.en],
        chunkId: recycled.id,
        tip: recycled.tip,
      });
    }

    // Nicht jeder zweite Baustein hat einen Platzhalter ("How are you?"). Dann ist der deutsche
    // Satz der Hinweis, statt dass leere Anführungszeichen in der Lektion stehen.
    const cueDe = slotSecond ? `Sag es mit: „${slotSecond}“` : 'Sag den Satz auf Englisch.';
    const transferDe = slotSecond
      ? `Jetzt anders: Setze „${slotSecond}“ in die Situation und sprich den Satz.`
      : 'Sag den Satz jetzt in deiner eigenen Situation.';

    add({
      type: 'SpeakRepeat',
      title: 'Jetzt du',
      promptDe: 'Hör zu und sprich nach. Nicht perfekt – verständlich.',
      target: lesson.chunks[0].en,
      accept: [lesson.chunks[0].en],
      chunkId: chunkIds[0],
      tip: lesson.chunks[0].tip,
    });

    add({
      type: 'SpeakFillSlot',
      title: 'Bau den Satz',
      promptDe: cueDe,
      promptEn: lesson.chunks[1].template,
      target: lesson.chunks[1].en,
      patternId: lesson.chunks[1].patternId,
      chunkId: chunkIds[1],
      slots: slotSecond ? [slotSecond] : [],
      hint: lesson.chunks[1].de,
      model: lesson.chunks[1].en,
    });

    add({
      type: 'BuildSentence',
      title: 'Wortstellung',
      promptDe: 'Tippe die Wörter in der richtigen Reihenfolge an.',
      target: lesson.chunks[0].en,
      words: wordsOf(lesson.chunks[0].en),
      model: lesson.chunks[0].en,
    });

    add({
      type: 'ChooseConnector',
      title: 'Sätze verbinden',
      promptDe: 'Welches Verbindungswort passt?',
      promptEn: connector.examples[0].a,
      target: connector.examples[0].b,
      connectorId,
      options: connector.words.split(' · ').map((w) => ({ text: w })),
      explanationDe: connector.contrastDe,
      model: connector.examples[0].b,
    });

    add({
      type: 'SpeakFree',
      title: 'Freies Sprechen',
      promptDe: lesson.roleplay.promptDe,
      target: lesson.roleplay.model,
      accept: [lesson.roleplay.model],
      requiredForm: lesson.roleplay.expect,
      model: lesson.roleplay.model,
      hint: lesson.roleplay.model,
    });

    add({
      type: 'GrammarSpot',
      title: 'Kurz zur Grammatik',
      promptDe: lesson.quiz.prompt,
      questions: [lesson.quiz],
      options: lesson.quiz.options.map((text, i) => ({ text, correct: i === lesson.quiz.answer })),
      explanationDe: lesson.quiz.why,
      model: lesson.quiz.options[lesson.quiz.answer],
      pillarId,
    });

    add({
      type: 'TransferTask',
      title: 'Übertragen',
      promptDe: transferDe,
      target: lesson.chunks[1].en,
      model: lesson.chunks[1].en,
      patternId: lesson.chunks[1].patternId,
      slots: slotSecond ? [slotSecond] : [],
      accept: [lesson.chunks[1].en],
    });

    add({
      type: 'SelfCheck',
      title: 'Geschafft',
      promptDe: 'Hat es geklappt? Sei ehrlich – das steuert die Wiederholung.',
      model: lesson.chunks[1].en,
    });

    lessons.push({
      id: lessonId,
      situationId: situation.id,
      title: lesson.title,
      goal: lesson.goal,
      level: situation.level,
      order: index + 1,
      pillarId,
      connectorLevel: lesson.connectorLevel,
      steps,
      chunkIds,
      recycles: lesson.recycles || [],
      xpMinutes: 11,
    });
    canDo.push({ id: `${lessonId}_can`, lessonId, text: lesson.goal });

    reviewLines.push(`### Lektion ${index + 1}: ${lesson.title}\n`);
    reviewLines.push(`Ziel: ${lesson.goal}\n`);
    reviewLines.push('| Sprecher | Englisch | Deutsch |\n|---|---|---|\n');
    for (const [speaker, en, de] of lesson.dialog) reviewLines.push(`| ${speaker} | ${en} | ${de} |`);
    reviewLines.push('\nBausteine:\n');
    for (const chunk of lesson.chunks) reviewLines.push(`- **${chunk.en}** – ${chunk.de} (Muster: ${chunk.template})`);
    reviewLines.push('');
  });
}

/* ---------- Werkstatt: Grammatik, Konnektoren, Strategien ---------- */

const workshopSituations = [
  { id: 'ws_grammatik', pathId: 'werkstatt', title: 'Grammatik-Werkstatt', subtitle: 'Die zwölf Säulen', scene: 'Eine Form – ein Bild – ein Beispiel.', level: 'A1–A2', icon: 'grammar', order: 1 },
  { id: 'ws_konnektoren', pathId: 'werkstatt', title: 'Verbindungswörter', subtitle: 'Sätze verbinden', scene: 'Gelenke für deine Sätze.', level: 'A1–A2', icon: 'link', order: 2 },
  { id: 'ws_strategie', pathId: 'werkstatt', title: 'Klang & Strategien', subtitle: 'Hören, nachfragen, antworten', scene: 'Werkzeuge für echte Gespräche.', level: 'A1–A2', icon: 'spark', order: 3 },
];

for (const pillar of pillars) {
  const lessonId = `ws_${pillar.id}`;
  const steps = [];
  const add = (step) => steps.push({ id: `${lessonId}_s${steps.length + 1}`, ...step });
  add({
    type: 'Noticing', title: pillar.title, promptDe: pillar.metaphor,
    explanationDe: pillar.explanationDe, model: pillar.examples[0], pillarId: pillar.id,
  });
  add({
    type: 'GrammarSpot', title: 'Erkennen', promptDe: pillar.quiz[0].prompt,
    options: pillar.quiz[0].options.map((text, i) => ({ text, correct: i === pillar.quiz[0].answer })),
    explanationDe: pillar.quiz[0].why, model: pillar.quiz[0].options[pillar.quiz[0].answer], pillarId: pillar.id,
  });
  add({
    type: 'BuildSentence', title: 'Bauen', promptDe: 'Tippe die Wörter richtig an.',
    target: pillar.examples[0], words: wordsOf(pillar.examples[0]), model: pillar.examples[0],
  });
  add({
    type: 'SpeakRepeat', title: 'Sprechen', promptDe: 'Sprich den Satz nach.', target: pillar.examples[1],
    accept: [pillar.examples[1]], tip: 'Langsam beginnen, dann normal sprechen.',
  });
  add({
    type: 'SpeakFree', title: 'Eigener Satz', promptDe: 'Bilde jetzt einen eigenen Satz mit dieser Form.',
    target: pillar.examples[2], model: pillar.examples[2], hint: pillar.examples[2],
  });
  add({
    type: 'GrammarSpot', title: 'Prüfen', promptDe: pillar.quiz[1].prompt,
    options: pillar.quiz[1].options.map((text, i) => ({ text, correct: i === pillar.quiz[1].answer })),
    explanationDe: pillar.quiz[1].why, model: pillar.quiz[1].options[pillar.quiz[1].answer], pillarId: pillar.id,
  });
  add({ type: 'SelfCheck', title: 'Fertig', promptDe: 'Konntest du die Form selbst benutzen?', model: pillar.examples[2] });
  lessons.push({
    id: lessonId, situationId: 'ws_grammatik', title: pillar.title, goal: `Ich kann die Form „${pillar.title}“ erkennen und benutzen.`,
    level: 'A1–A2', order: pillar.order, pillarId: pillar.id, connectorLevel: 2, steps, chunkIds: [], xpMinutes: 7,
  });
  canDo.push({ id: `${lessonId}_can`, lessonId, text: `Ich kann „${pillar.title}“ in einem eigenen Satz benutzen.` });
}

for (const connector of connectorLevels) {
  const lessonId = `ws_${connector.id}`;
  const steps = [];
  const add = (step) => steps.push({ id: `${lessonId}_s${steps.length + 1}`, ...step });
  add({
    type: 'Noticing', title: connector.words, promptDe: connector.explanationDe,
    explanationDe: connector.contrastDe, model: connector.examples[0].b, connectorId: connector.id,
  });
  add({
    type: 'ConnectClauses', title: 'Verbinden', promptDe: 'Verbinde die beiden Sätze mit dem passenden Wort.',
    promptEn: `${connector.examples[0].a} ${connector.examples[0].b}`,
    target: connector.examples[0].b, connectorId: connector.id, model: connector.examples[0].b,
    options: connector.words.split(' · ').map((w) => ({ text: w })),
  });
  add({
    type: 'ChooseConnector', title: 'Auswählen', promptDe: 'Welches Verbindungswort passt?',
    promptEn: connector.examples[1].a, target: connector.examples[1].b, connectorId: connector.id,
    options: connector.words.split(' · ').map((w) => ({ text: w })), model: connector.examples[1].b,
  });
  add({
    type: 'SpeakRepeat', title: 'Sprechen', promptDe: 'Sprich den verbundenen Satz.',
    target: connector.examples[2].b, accept: [connector.examples[2].b], connectorId: connector.id,
  });
  add({
    type: 'SpeakFree', title: 'Eigener Satz',
    promptDe: 'Sag jetzt einen eigenen Satz mit diesem Verbindungswort.',
    target: connector.examples[2].b, model: connector.examples[2].b, hint: connector.contrastDe,
    connectorId: connector.id,
  });
  add({ type: 'SelfCheck', title: 'Fertig', promptDe: 'Hast du das Gelenk gehört und selbst benutzt?', model: connector.examples[2].b });
  lessons.push({
    id: lessonId, situationId: 'ws_konnektoren', title: `Stufe ${connector.level}: ${connector.words}`,
    goal: `Ich kann Sätze mit ${connector.words} verbinden.`, level: 'A1–A2', order: connector.level,
    pillarId: 'connectors_word_order', connectorLevel: connector.level, steps, chunkIds: [], xpMinutes: 6,
  });
  canDo.push({ id: `${lessonId}_can`, lessonId, text: `Ich kann mit ${connector.words} zwei Sätze verbinden.` });
}

/* Strategie-Lektion: Minimalpaare */
{
  const lessonId = 'ws_minimal';
  const steps = [];
  const add = (step) => steps.push({ id: `${lessonId}_s${steps.length + 1}`, ...step });
  add({ type: 'Noticing', title: 'Klangpaare', promptDe: 'Zwei Wörter, ein kleiner Unterschied – und eine andere Bedeutung.', explanationDe: 'Hör zu, unterscheide, dann sprich selbst. Der Unterschied liegt oft nur in der Länge oder in der Zungenposition.', model: 'ship – sheep' });
  for (const pair of minimalPairs.slice(0, 5)) {
    add({
      type: 'MinimalPair', title: 'Hör zu', promptDe: 'Welches Wort hörst du?', target: pair.a, model: pair.b,
      options: [{ text: pair.a }, { text: pair.b }], tip: pair.tipDe, ipaA: pair.ipaA, ipaB: pair.ipaB,
    });
    add({
      type: 'SpeakRepeat', title: 'Sprich nach', promptDe: `Sprich beide Wörter: „${pair.a}“ – „${pair.b}“`,
      target: `${pair.a} – ${pair.b}`, accept: [pair.a, pair.b], tip: pair.tipDe,
    });
  }
  add({ type: 'SelfCheck', title: 'Fertig', promptDe: 'Konntest du die Wörter unterscheiden?', model: 'ship – sheep' });
  lessons.push({
    id: lessonId, situationId: 'ws_strategie', title: 'Klangpaare hören', goal: 'Ich kann ähnlich klingende Wörter unterscheiden.',
    level: 'A1', order: 1, pillarId: 'modals_politeness', connectorLevel: 1, steps, chunkIds: [], xpMinutes: 6,
  });
  canDo.push({ id: `${lessonId}_can`, lessonId, text: 'Ich kann ähnlich klingende Wörter unterscheiden.' });
}

/* Strategie-Lektion: schnelles Sprechen */
{
  const lessonId = 'ws_fast';
  const fast = [
    ['I am going to', 'I’m gonna', 'I’m gonna visit my sister.'],
    ['I want to', 'I wanna', 'I wanna ask you something.'],
    ['I have got to', 'I gotta', 'I gotta go now.'],
    ['Did you', 'Didja', 'Didja see the film?'],
  ];
  const steps = [];
  const add = (step) => steps.push({ id: `${lessonId}_s${steps.length + 1}`, ...step });
  add({ type: 'Noticing', title: 'So wird wirklich gesprochen', promptDe: 'Im Alltag verschmelzen Wörter. Du musst sie nicht selbst so sprechen – aber verstehen.', explanationDe: '„going to“ klingt oft wie „gonna“, „want to“ wie „wanna“, „got to“ wie „gotta“, „did you“ wie „didja“. Das ist kein schlampiges Englisch, sondern normaler Sprachfluss.', model: 'I’m gonna call you.' });
  for (const [slow, fastForm, sentence] of fast) {
    add({
      type: 'UnderstandFastSpeech', title: 'Schnell gesprochen', promptEn: fastForm,
      promptDe: `Was heißt „${fastForm}“ ausgeschrieben?`, target: slow, model: sentence,
      explanationDe: `„${fastForm}“ = „${slow}“.`, options: [{ text: slow }, { text: fastForm }],
    });
    add({ type: 'SpeakRepeat', title: 'Nachsprechen', promptDe: 'Sprich den ganzen Satz einmal deutlich.', target: sentence, accept: [sentence] });
  }
  add({ type: 'SelfCheck', title: 'Fertig', promptDe: 'Hast du die schnellen Formen erkannt?', model: 'I’m gonna call you.' });
  lessons.push({
    id: lessonId, situationId: 'ws_strategie', title: 'Schnell gesprochen', goal: 'Ich kann verschmolzene Formen im Alltag verstehen.',
    level: 'A2', order: 2, pillarId: 'modals_politeness', connectorLevel: 3, steps, chunkIds: [], xpMinutes: 6,
  });
  canDo.push({ id: `${lessonId}_can`, lessonId, text: 'Ich kann „gonna“, „wanna“ und „gotta“ verstehen.' });
}

/* Strategie-Lektion: kurze Nachrichten */
{
  const lessonId = 'ws_whatsapp';
  const steps = [];
  const add = (step) => steps.push({ id: `${lessonId}_s${steps.length + 1}`, ...step });
  add({ type: 'Noticing', title: 'Kurze Nachrichten', promptDe: 'Nachrichten sind kurz, freundlich und oft ohne Punkt.', explanationDe: 'In Nachrichten fehlen oft Wörter. Wichtig ist der Ton: kurz ist nicht unfreundlich. Ein „Thanks!“ am Ende macht fast alles wärmer.', model: 'See you at six! Thanks!' });
  add({
    type: 'WhatsAppReply', title: 'Antworten', promptEn: 'Hi! Are we still meeting at six?',
    promptDe: 'Deine Enkelin fragt, ob ihr euch um sechs trefft. Antworte mündlich.', target: 'Yes, see you at six!',
    accept: ['yes', 'six'], model: 'Yes, see you at six!',
  });
  add({
    type: 'WhatsAppReply', title: 'Absagen', promptEn: 'Can you come a bit later?',
    promptDe: 'Du kannst nicht später. Antworte freundlich.', target: 'Sorry, I can’t. Six is better for me.',
    accept: ['sorry', 'six'], model: 'Sorry, I can’t. Six is better for me.',
  });
  add({
    type: 'SpeakRepeat', title: 'Kurz und freundlich', promptDe: 'Sprich die Antwort einmal deutlich.',
    target: 'Yes, see you at six!', accept: ['yes', 'six'],
  });
  add({
    type: 'SpeakFree', title: 'Selbst schreiben', promptDe: 'Sag mündlich: „Danke, bis morgen!“',
    target: 'Thanks, see you tomorrow!', accept: ['thanks', 'tomorrow'], model: 'Thanks, see you tomorrow!',
  });
  add({ type: 'SelfCheck', title: 'Fertig', promptDe: 'Konntest du kurz und freundlich antworten?', model: 'Yes, see you at six!' });
  lessons.push({
    id: lessonId, situationId: 'ws_strategie', title: 'Kurze Nachrichten', goal: 'Ich kann kurz und freundlich auf Nachrichten antworten.',
    level: 'A2', order: 3, pillarId: 'modals_politeness', connectorLevel: 2, steps, chunkIds: [], xpMinutes: 6,
  });
  canDo.push({ id: `${lessonId}_can`, lessonId, text: 'Ich kann kurz und freundlich auf eine Nachricht antworten.' });
}

/* ---------- Muster aus Chunks ableiten ---------- */

const patternMap = new Map();
for (const chunk of chunks) {
  if (!chunk.patternId) continue;
  if (!patternMap.has(chunk.patternId)) {
    patternMap.set(chunk.patternId, {
      id: chunk.patternId,
      template: chunk.template,
      de: chunk.templateDe,
      register: 'neutral',
      chunks: [],
      related: [],
      pillarId: lessons.find((l) => l.chunkIds.includes(chunk.id))?.pillarId || '',
    });
  }
  patternMap.get(chunk.patternId).chunks.push(chunk.id);
}
const patterns = [...patternMap.values()];
for (const p of patterns) {
  p.related = patterns.filter((o) => o.id !== p.id && o.pillarId === p.pillarId).map((o) => o.id);
}

const bundle = {
  version: 1,
  world,
  personas: personas.map((persona) => ({
    ...persona,
    situations: Object.entries(situationPersonas)
      .filter(([situationId, ids]) =>
        ids.includes(persona.id) || (variants[situationId] || []).some((v) => v.personaId === persona.id))
      .map(([situationId]) => situationId),
  })),
  paths: [...paths, { id: 'werkstatt', title: 'Werkstatt', subtitle: 'Grammatik, Gelenke, Strategien', icon: 'grammar', order: 9 }],
  situations: [...situations.map((s) => {
    const meta = storyMeta[s.id] || {};
    return {
      id: s.id, pathId: s.pathId, title: s.title, subtitle: s.subtitle, scene: s.scene,
      level: s.level, icon: s.icon, order: s.order,
      place: s.place || meta.place || '',
      season: s.season || meta.season || '',
      story: s.story || meta.story || '',
      personaIds: s.personaIds || situationPersonas[s.id] || [],
      variants: variants[s.id] || [],
    };
  }), ...workshopSituations],
  lessons,
  pillars,
  connectorLevels,
  functions,
  errorCards,
  minimalPairs,
  patterns,
  chunks: chunks.map(({ template, templateDe, ...rest }) => rest),
  canDo,
  placement,
  checkup,
};

/* ---------- Validierung ---------- */

const problems = [];
const ids = new Set();
for (const item of [...lessons, ...chunks, ...patterns, ...errorCards, ...situations, ...workshopSituations]) {
  if (ids.has(item.id)) problems.push(`Doppelte ID: ${item.id}`);
  ids.add(item.id);
}
for (const lesson of lessons) {
  if (!lesson.steps.length) problems.push(`Lektion ohne Schritte: ${lesson.id}`);
  if (!lesson.steps.some((s) => ['SpeakRepeat', 'SpeakFree', 'SpeakFillSlot', 'TransferTask'].includes(s.type))) {
    problems.push(`Lektion ohne Sprechaufgabe: ${lesson.id}`);
  }
  const situation = bundle.situations.find((s) => s.id === lesson.situationId);
  if (!situation) problems.push(`Lektion ohne Situation: ${lesson.id}`);
}
for (const chunk of chunks) {
  if (!chunk.de) problems.push(`Chunk ohne Deutsch: ${chunk.id}`);
  if (chunk.patternId && !patterns.find((p) => p.id === chunk.patternId)) problems.push(`Chunk ohne Muster: ${chunk.id}`);
}
for (const card of errorCards) {
  for (const field of ['observation', 'whyDe', 'correctEn', 'anchor', 'nextStep']) {
    if (!card[field]) problems.push(`Fehlerkarte ${card.id} ohne ${field}`);
  }
  if (card.checklist.length < 2) problems.push(`Fehlerkarte ${card.id} ohne Checkliste`);
}

/* Kontinuität: dieselbe Welt, dieselben Leute, dieselben Bausteine. */
const personaIds = new Set(bundle.personas.map((p) => p.id));
for (const situation of bundle.situations) {
  if (situation.id.startsWith('ws_')) continue;
  if (!situation.personaIds.length) problems.push(`Situation ohne Figur: ${situation.id}`);
  for (const id of situation.personaIds) {
    if (!personaIds.has(id)) problems.push(`Unbekannte Figur ${id} in ${situation.id}`);
  }
  if (!situation.place) problems.push(`Situation ohne Ort: ${situation.id}`);
}
for (const persona of bundle.personas) {
  if (persona.situations.length < 2) problems.push(`Figur ${persona.id} tritt nur einmal auf`);
}
const allChunkIds = new Set(chunks.map((c) => c.id));
for (const lesson of lessons) {
  for (const id of lesson.recycles || []) {
    if (!allChunkIds.has(id)) problems.push(`Wiederaufnahme zeigt auf unbekannten Baustein: ${lesson.id} -> ${id}`);
    if (lesson.chunkIds.includes(id)) problems.push(`Wiederaufnahme im selben Baustein: ${lesson.id} -> ${id}`);
  }
  if (staffelZwei.has(lesson.situationId) && lesson.recycles.length < 2) {
    problems.push(`Staffel 2 ohne zwei Wiederaufnahmen: ${lesson.id}`);
  }
}
for (const situation of [...situations3, ...situations4]) {
  if (!situation.lessons.every((lesson) => lesson.recycles.length >= 2)) {
    problems.push(`Staffel-2-Situation ${situation.id} ohne Continuity`);
  }
}
/* Varianten: gleiches Ziel, anderer Rahmen. */
const variantIds = new Set();
for (const situation of situations) {
  const list = variants[situation.id] || [];
  if (list.length < 2) problems.push(`Situation mit weniger als zwei Varianten: ${situation.id}`);
  for (const variant of list) {
    if (variantIds.has(variant.id)) problems.push(`Doppelte Varianten-ID: ${variant.id}`);
    variantIds.add(variant.id);
    if (!variant.story || !variant.greetingEn) problems.push(`Variante ohne Geschichte/Gruß: ${variant.id}`);
    if (!personaIds.has(variant.personaId)) problems.push(`Variante mit unbekannter Figur: ${variant.id}`);
  }
}

/* Prüfpool: jeder Bereich braucht Aufgaben in jeder Stufe. */
const checkupSkills = ['hoeren', 'grammatik', 'bausteine', 'sprechen', 'verknuepfen'];
const checkupIds = new Set();
for (const item of checkup) {
  if (checkupIds.has(item.id)) problems.push(`Doppelte Prüfaufgabe: ${item.id}`);
  checkupIds.add(item.id);
  if (!checkupSkills.includes(item.skill)) problems.push(`Prüfaufgabe mit unbekanntem Bereich: ${item.id}`);
  if (![0, 1, 2, 3].includes(item.level)) problems.push(`Prüfaufgabe mit unbekannter Stufe: ${item.id}`);
  if (item.type !== 'speak' && !item.options?.length) problems.push(`Prüfaufgabe ohne Auswahl: ${item.id}`);
  if (item.type === 'speak' && !item.model) problems.push(`Sprechaufgabe ohne Modell: ${item.id}`);
  if (item.type !== 'speak' && (item.answer === undefined || item.answer >= item.options.length)) {
    problems.push(`Prüfaufgabe ohne gültige Antwort: ${item.id}`);
  }
  if (!item.promptDe) problems.push(`Prüfaufgabe ohne Aufgabe: ${item.id}`);
}
for (const skill of checkupSkills) {
  for (const level of [0, 1, 2]) {
    const count = checkup.filter((i) => i.skill === skill && i.level === level).length;
    if (count < 3) problems.push(`Zu wenige Prüfaufgaben für ${skill} Stufe ${level}: ${count}`);
  }
}

if (problems.length) {
  console.error(problems.join('\n'));
  process.exit(1);
}

const outDir = path.join(root, 'app', 'src', 'main', 'assets', 'content');
fs.mkdirSync(outDir, { recursive: true });
const outFile = path.join(outDir, 'content.json');
fs.writeFileSync(outFile, JSON.stringify(bundle));
console.log(`content.json: ${(fs.statSync(outFile).size / 1024).toFixed(0)} KB`);
console.log(`Situationen: ${bundle.situations.length}, Lektionen: ${lessons.length}, Chunks: ${chunks.length}, Muster: ${patterns.length}, Fehlerkarten: ${errorCards.length}, Figuren: ${bundle.personas.length}, Varianten: ${Object.values(variants).flat().length}, Prüfaufgaben: ${checkup.length}`);

const reviewDir = path.join(root, 'docs');
fs.mkdirSync(reviewDir, { recursive: true });
fs.writeFileSync(path.join(reviewDir, 'content-review.md'), `# Prüfbogen – alle Sätze und Bausteine\n\nBitte gegenlesen: Stimmt das Englisch? Stimmt die deutsche Bedeutung? Ist der Ton freundlich?\n${reviewLines.join('\n')}`);
console.log('docs/content-review.md geschrieben');
