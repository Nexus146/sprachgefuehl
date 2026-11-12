// Erweitert content.json um neue Pfade, Lektionen und Pool-Material.
// Aufruf: node tools/expand-content.js
// Idempotent: Einträge mit derselben ID werden ersetzt, nicht doppelt angehängt.
const fs = require("fs");
const path = require("path");

const file = path.join(__dirname, "..", "app", "src", "main", "assets", "content", "content.json");
const d = JSON.parse(fs.readFileSync(file, "utf8"));
const { paths, situations, lessons } = require("./new-lessons");
const { errorCards, minimalPairs, checkup } = require("./new-extras");
const { situations: situationsC, lessons: lessonsC } = require("./new-lessons-c");
const { lessons: lessonsNumbers } = require("./new-numbers");
const allSituations = [...situations, ...situationsC];
const allLessons = [...lessons, ...lessonsC, ...lessonsNumbers];

// Lektionen ohne Levelangabe folgen ihrer Position im Pfad: die erste Situation ist A1, die
// zweite A1–A2, ab der dritten A2. Ohne diese Einordnung hätte der Einstieg nach dem
// Standort-Check keine Grundlage und jeder startete wieder bei der ersten Lektion.
const situationsByPath = new Map();
d.situations.forEach((s) => {
  const list = situationsByPath.get(s.pathId) ?? [];
  list.push(s);
  situationsByPath.set(s.pathId, list);
});
const rankBySituation = new Map();
situationsByPath.forEach((list) => {
  list.sort((a, b) => (a.order ?? 0) - (b.order ?? 0));
  list.forEach((s, index) => rankBySituation.set(s.id, index));
});
// Die neuen Pfade und die Werkstatt bringen ihre Stufe selbst mit; alle anderen folgen ihrer
// Position im Pfad. Ohne diese Einordnung hätte der Einstieg keine Grundlage.
const authored = /^(arb|rei|woh|dig|spo|not|ws)_/;
d.lessons.forEach((lesson) => {
  if (authored.test(lesson.id)) return;
  const rank = rankBySituation.get(lesson.situationId) ?? 0;
  lesson.level = rank <= 0 ? "A1" : rank === 1 ? "A1–A2" : "A2";
});


function put(list, item) {
  const index = list.findIndex((entry) => entry.id === item.id);
  if (index >= 0) list[index] = item;
  else list.push(item);
}

function buildSteps(l) {
  const steps = [];
  const base = `${l.id}_s`;
  let n = 1;
  steps.push({
    id: base + n++,
    type: "DialogInput",
    title: "Hören & verstehen",
    promptDe: l.dialog.promptDe,
    lines: l.dialog.lines.map(([speaker, en, de]) => ({ speaker, en, de })),
  });
  steps.push({
    id: base + n++,
    type: "ComprehensionDeep",
    title: "Verstanden?",
    promptDe: "Hör noch einmal und antworte.",
    questions: l.questions,
  });
  steps.push({
    id: base + n++,
    type: "Noticing",
    title: "Die Form dahinter",
    promptEn: l.c1.en,
    promptDe: l.c1.de,
    explanationDe: l.noticing,
    note: l.noticingNote,
    pillarId: l.pillar,
    highlight: l.c1.slot || l.c1.en,
  });
  [l.c1, l.c2].forEach((chunk, index) => {
    steps.push({
      id: base + n++,
      type: "ChunkIntro",
      title: "Baustein",
      promptEn: chunk.en,
      promptDe: chunk.de,
      chunkId: `${l.id}_c${index + 1}`,
      patternId: l.pattern.id,
      target: chunk.en,
      tip: chunk.tip,
    });
  });
  steps.push({
    id: base + n++,
    type: "SpeakFree",
    title: "Freies Sprechen",
    promptDe: l.free.promptDe,
    target: l.free.target,
    accept: [l.free.target],
    requiredForm: l.free.requiredForm || [],
    model: l.free.target,
    hint: l.free.target,
  });
  steps.push({
    id: base + n++,
    type: "GrammarSpot",
    title: "Kurz zur Grammatik",
    promptDe: l.grammar.prompt,
    questions: [{ prompt: l.grammar.prompt, options: l.grammar.options, answer: 0, why: l.grammar.why }],
    options: [
      { text: l.grammar.options[0], correct: true },
      { text: l.grammar.options[1], correct: false },
    ],
    explanationDe: l.grammar.why,
    model: l.grammar.options[0],
    pillarId: l.pillar,
  });
  steps.push({
    id: base + n++,
    type: "SelfCheck",
    title: "Geschafft",
    promptDe: "Hat es geklappt? Sei ehrlich – das steuert die Wiederholung.",
    model: l.c2.en,
  });
  return steps;
}

// Situationen, die ohne Lektionen im Pfad standen: Sie würden als leere Kachel erscheinen.
const obsoleteSituations = ["buero", "meeting", "bewerbung", "zug", "ausflug", "wohnung", "umzug"];
d.situations = d.situations.filter((s) => !obsoleteSituations.includes(s.id));

const added = { paths: 0, situations: 0, lessons: 0, chunks: 0, patterns: 0, canDo: 0, errorCards: 0, minimalPairs: 0, checkup: 0 };

for (const p of paths) { put(d.paths, p); added.paths++; }
for (const s of allSituations) { put(d.situations, s); added.situations++; }
for (const l of allLessons) {
  put(d.lessons, {
    id: l.id,
    situationId: l.situationId,
    title: l.title,
    goal: l.goal,
    level: l.level || "A1",
    order: l.order,
    pillarId: l.pillar,
    connectorLevel: l.connectorLevel || 1,
    steps: buildSteps(l),
    chunkIds: [`${l.id}_c1`, `${l.id}_c2`],
    recycles: l.recycles || [],
    xpMinutes: 10,
  });
  added.lessons++;
  [l.c1, l.c2].forEach((chunk, i) => {
    put(d.chunks, {
      id: `${l.id}_c${i + 1}`,
      en: chunk.en,
      de: chunk.de,
      patternId: l.pattern.id,
      slot: chunk.slot || "",
      tip: chunk.tip,
      level: l.level || "A1",
      tags: [l.tag],
    });
    added.chunks++;
  });
  // Muster nur anlegen, wenn die ID frei ist: bestehende Muster behalten ihre Beispiele.
  if (!d.patterns.some((p) => p.id === l.pattern.id)) {
    d.patterns.push({
      id: l.pattern.id,
      template: l.pattern.template,
      de: l.pattern.de,
      pillarId: l.pillar,
      register: "neutral",
      chunks: [`${l.id}_c1`, `${l.id}_c2`],
      related: l.pattern.related || [],
    });
    added.patterns++;
  }
  put(d.canDo, { id: `${l.id}_can`, lessonId: l.id, text: l.goal });
  added.canDo++;
}
for (const c of errorCards) { put(d.errorCards, c); added.errorCards++; }
for (const m of minimalPairs) { put(d.minimalPairs, m); added.minimalPairs++; }

// Die übrigen Klangpaare bekommen eigene Werkstatt-Lektionen: Ohne sie wären die meisten der 47
// Paare nur in zufälligen Runden zu hören. Der Block steht erst hier, weil die Paare aus
// new-extras.js erst jetzt im Inhalt stehen.
{
  const covered = new Set();
  const wsMinimal = d.lessons.find((l) => l.id === "ws_minimal");
  (wsMinimal && wsMinimal.steps ? wsMinimal.steps : [])
    .filter((step) => step.type === "MinimalPair")
    .forEach((step) => covered.add(step.options.map((option) => option.text).join("|")));
  const rest = d.minimalPairs.filter((pair) => !covered.has([pair.a, pair.b].join("|")));
  const groupSize = 7;
  for (let g = 0; g * groupSize < rest.length; g++) {
    const group = rest.slice(g * groupSize, (g + 1) * groupSize);
    const lessonId = `ws_laute${g + 2}`;
    const steps = [];
    let n = 1;
    const push = (step) => steps.push({ id: `${lessonId}_s${n++}`, ...step });
    push({
      type: "Noticing",
      title: `Klangpaare ${g + 2}`,
      promptDe: "Wieder zwei Wörter, ein kleiner Unterschied – und eine andere Bedeutung.",
      explanationDe: "Hör zu, unterscheide, dann sprich selbst. Achte auf Länge, Stimmhaftigkeit und die Zungenposition.",
      model: `${group[0].a} – ${group[0].b}`,
    });
    for (const pair of group) {
      push({
        type: "MinimalPair",
        title: "Hör zu",
        promptDe: "Welches Wort hörst du?",
        target: pair.a,
        model: pair.b,
        options: [{ text: pair.a }, { text: pair.b }],
        tip: pair.tipDe,
        ipaA: pair.ipaA,
        ipaB: pair.ipaB,
      });
      push({
        type: "SpeakRepeat",
        title: "Sprich nach",
        promptDe: `Sprich beide Wörter: „${pair.a}“ – „${pair.b}“`,
        target: `${pair.a} – ${pair.b}`,
        accept: [pair.a, pair.b],
        tip: pair.tipDe,
      });
    }
    push({ type: "SelfCheck", title: "Fertig", promptDe: "Konntest du die Wörter unterscheiden?", model: `${group[0].a} – ${group[0].b}` });
    put(d.lessons, {
      id: lessonId,
      situationId: "ws_strategie",
      title: `Klangpaare ${g + 2}`,
      goal: "Ich kann ähnlich klingende Wörter unterscheiden.",
      level: "A2",
      order: 5 + g,
      pillarId: "modals_politeness",
      connectorLevel: 2,
      steps,
      chunkIds: [],
      recycles: [],
      xpMinutes: 7,
    });
    put(d.canDo, { id: `${lessonId}_can`, lessonId, text: "Ich kann weitere ähnlich klingende Wörter unterscheiden." });
    added.lessons++;
    added.canDo++;
  }
}
for (const c of checkup) { put(d.checkup, c); added.checkup++; }

// Verständnisfragen bekommen den Satz, auf den sie sich beziehen. Ohne ihn könnte ein Hörfehler
// nie als Wiederholungsfall mit Muster zurückkommen – er bliebe eine einmalige Rückmeldung.
// Der Block steht bewusst am Ende: Erst hier sind auch die neuen Lektionen eingebaut.
d.lessons.forEach((lesson) => {
  const first = (lesson.chunkIds || [])[0];
  if (!first) return;
  lesson.steps.forEach((step) => {
    if ((step.type === "ComprehensionDeep" || step.type === "DialogInput") && !step.chunkId) {
      step.chunkId = first;
    }
  });
});

d.version = (d.version || 1) + 1;
fs.writeFileSync(file, JSON.stringify(d, null, 2) + "\n", "utf8");
console.log(JSON.stringify(added));

// Prüfbogen für ALLE Situationen – auch die, die erst dieser Schritt ergänzt hat. Ohne ihn
// müsste man zum Gegenlesen 52 Stellen einzeln aus der JSON suchen.
function reviewSheet(bundle) {
  const lines = [
    '# Prüfbogen – alle Sätze und Bausteine',
    '',
    'Bitte gegenlesen: Stimmt das Englisch? Stimmt die deutsche Bedeutung? Ist der Ton freundlich?',
    '',
  ];
  const chunksById = new Map(bundle.chunks.map((chunk) => [chunk.id, chunk]));
  const patternsById = new Map(bundle.patterns.map((pattern) => [pattern.id, pattern]));
  const lessonsBySituation = new Map();
  for (const lesson of bundle.lessons) {
    const list = lessonsBySituation.get(lesson.situationId) ?? [];
    list.push(lesson);
    lessonsBySituation.set(lesson.situationId, list);
  }
  for (const situation of bundle.situations) {
    lines.push(`## ${situation.title} (${situation.pathId})`, '');
    if (situation.scene) lines.push(`Szene: ${situation.scene}`, '');
    if (situation.story) lines.push(situation.story, '');
    const lessons = (lessonsBySituation.get(situation.id) ?? [])
      .slice()
      .sort((a, b) => (a.order ?? 0) - (b.order ?? 0));
    if (!lessons.length) lines.push('_Noch keine Lektion._', '');
    for (const lesson of lessons) {
      lines.push(`### ${lesson.title}`, '', `Ziel: ${lesson.goal}`, '');
      const dialog = (lesson.steps || []).find((step) => step.type === 'DialogInput');
      if (dialog && (dialog.lines || []).length) {
        lines.push('| Sprecher | Englisch | Deutsch |', '|---|---|---|');
        for (const line of dialog.lines) lines.push(`| ${line.speaker} | ${line.en} | ${line.de} |`);
        lines.push('');
      }
      const own = (lesson.chunkIds || []).map((id) => chunksById.get(id)).filter(Boolean);
      if (own.length) {
        lines.push('Bausteine:', '');
        for (const chunk of own) {
          const pattern = patternsById.get(chunk.patternId);
          const hint = pattern ? ` (Muster: ${pattern.template})` : '';
          lines.push(`- **${chunk.en}** – ${chunk.de}${hint}`);
        }
        lines.push('');
      }
    }
  }
  return lines.join('\n');
}
fs.writeFileSync(path.join(__dirname, '..', 'docs', 'content-review.md'), reviewSheet(d) + '\n');
console.log('docs/content-review.md für', d.situations.length, 'Situationen geschrieben');

console.log(
  "version", d.version,
  "paths", d.paths.length,
  "situations", d.situations.length,
  "lessons", d.lessons.length,
  "chunks", d.chunks.length,
  "errorCards", d.errorCards.length,
  "minimalPairs", d.minimalPairs.length,
  "checkup", d.checkup.length
);
