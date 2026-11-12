// Neue Inhalte, Teil 3: Beim Friseur – Termin, Wunsch, Bezahlen.
const L = (id, situationId, title, goal, order, pillar, tag, level, c1, c2, dialog, question, noticing, note, free, grammar, pattern) => ({
  id, situationId, title, goal, order, pillar, tag, level,
  c1: { en: c1[0], de: c1[1], slot: c1[2], tip: c1[3] },
  c2: { en: c2[0], de: c2[1], slot: c2[2], tip: c2[3] },
  dialog: { promptDe: dialog[0], lines: dialog[1] },
  questions: [{ prompt: question[0], options: question[1], answer: 0, why: question[2] }],
  noticing, noticingNote: note,
  free: { promptDe: free[0], target: free[1], requiredForm: free[2] || [] },
  grammar: { prompt: grammar[0], options: [grammar[1], grammar[2]], why: grammar[3] },
  pattern: { id: pattern[0], template: pattern[1], de: pattern[2] },
});

const situations = [
  {
    id: "friseur", pathId: "alltag", title: "Beim Friseur", subtitle: "Termin, Wunsch, Bezahlen",
    scene: "Ein Termin, ein Wunsch, ein Spiegel – Lena schneidet.", level: "A1", icon: "help",
    order: 10, place: "Friseur", personaIds: ["lena"], season: "sommer",
    story: "Lena schneidet seit Jahren Haare und kennt die Sätze, die dabei helfen.",
    variants: [
      { id: "friseur_kurz", story: "Du willst nur die Spitzen schneiden lassen.", scene: "Lena legt den Umhang um.", place: "Friseur", personaId: "lena", greetingEn: "How short should it be?", greetingDe: "Wie kurz soll es werden?", interestId: "" },
      { id: "friseur_termin", story: "Du brauchst kurzfristig einen Termin.", scene: "Lena schaut in den Kalender.", place: "Friseur", personaId: "lena", greetingEn: "Can you come on Friday?", greetingDe: "Können Sie am Freitag kommen?", interestId: "" },
      { id: "friseur_farbe", story: "Du möchtest wissen, was eine Farbe kostet.", scene: "Lena zeigt die Farbkarte.", place: "Friseur", personaId: "hana", greetingEn: "Do you want colour as well?", greetingDe: "Möchten Sie auch Farbe?", interestId: "" },
    ],
  },
];

const lessons = [
  L("friseur_l1", "friseur", "Einen Termin machen", "Ich kann höflich einen Termin vereinbaren.", 1, "conditionals_would_like", "friseur", "A1",
    ["I’d like to make an appointment.", "Ich möchte einen Termin machen.", "an appointment", "appointment = Termin."],
    ["Does Thursday morning work?", "Passt Donnerstagvormittag?", "Thursday morning", "work = passen (hier)."],
    ["Lena steht hinter dem Tresen.", [
      ["you", "I’d like to make an appointment.", "Ich möchte einen Termin machen."],
      ["lena", "Does Thursday morning work?", "Passt Donnerstagvormittag?"],
      ["you", "Thursday is good for me.", "Donnerstag passt mir gut."],
      ["lena", "See you then.", "Bis dann."],
    ]],
    ["Was möchte die Person?", ["Einen Termin machen", "Etwas abholen"], "„appointment“ = Termin."],
    "would like to + Grundform ist die höfliche Bitte; appointment = Termin, auch beim Arzt.",
    "Termin machen · would like to",
    ["Sag höflich, dass du einen Termin möchtest.", "I’d like to make an appointment.", ["appointment"]],
    ["Welche Frage ist höflich?", "Does Thursday morning work?", "Thursday morning you work?", "does + Grundform."],
    ["friseur_appointment", "I’d like to make ___", "Ich möchte ___ machen"]),

  L("friseur_l2", "friseur", "Den Wunsch beschreiben", "Ich kann sagen, wie ich es haben möchte.", 2, "comparison", "friseur", "A1",
    ["A little shorter, please.", "Etwas kürzer, bitte.", "shorter", "shorter = kürzer (Vergleich von short)."],
    ["Just the ends, please.", "Nur die Spitzen, bitte.", "the ends", "ends = Spitzen."],
    ["Lena hält die Schere schon bereit.", [
      ["you", "A little shorter, please.", "Etwas kürzer, bitte."],
      ["lena", "Just the ends?", "Nur die Spitzen?"],
      ["you", "Yes, just the ends.", "Ja, nur die Spitzen."],
      ["lena", "That will be easy.", "Das wird einfach."],
    ]],
    ["Wie soll es werden?", ["Etwas kürzer", "Deutlich länger"], "„shorter“ = kürzer."],
    "Vergleich: a little + -er heißt etwas …; just = nur.",
    "Wunsch · a little shorter",
    ["Sag, dass es etwas kürzer werden soll.", "A little shorter, please.", ["shorter"]],
    ["Wie bildet man den Vergleich?", "short → shorter", "short → more short", "Kurze Wörter bekommen -er."],
    ["friseur_shorter", "A little ___, please", "Etwas ___, bitte"]),

  L("friseur_l3", "friseur", "Bezahlen und Trinkgeld", "Ich kann fragen, was es kostet, und passend zahlen.", 3, "word_order_question", "friseur", "A1",
    ["How much is it?", "Wie viel kostet es?", "is", "how much is it = was kostet es."],
    ["Keep the change.", "Stimmt so.", "change", "change = Wechselgeld."],
    ["Der Spiegel ist wieder frei.", [
      ["you", "How much is it?", "Wie viel kostet es?"],
      ["lena", "Twenty euros.", "Zwanzig Euro."],
      ["you", "Here you are. Keep the change.", "Bitte schön. Stimmt so."],
      ["lena", "Thank you. See you next time.", "Danke. Bis zum nächsten Mal."],
    ]],
    ["Was kostet der Haarschnitt?", ["Zwanzig Euro", "Zwölf Euro"], "„twenty euros“ = zwanzig Euro."],
    "Preis fragen: How much is it? Beim Bezahlen: Here you are. Keep the change.",
    "Bezahlen · How much is it?",
    ["Frag nach dem Preis.", "How much is it?", ["how much"]],
    ["Welche Frage ist richtig?", "How much is it?", "How much it is?", "Frage: is vor it."],
    ["friseur_pay", "How much ___ it?", "Wie viel ___ es?"]),
];

module.exports = { situations, lessons };
