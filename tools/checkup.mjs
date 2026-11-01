// Item bank for the standort check. Five skills, levels 0 (start) to 3 (free).
// Every item is authored, checked against the same content the lessons teach, and can be
// answered without typing: an older learner should never fight a keyboard during a check.

const I = (id, skill, level, type, promptDe, extra) => ({ id, skill, level, type, promptDe, ...extra });

const raw = [
  // ---- Hören ------------------------------------------------------------------------------
  I('h_0_1', 'hoeren', 0, 'listen', 'Was bedeutet der Satz?', {
    audioEn: 'I have a headache.',
    options: ['Ich habe Kopfschmerzen.', 'Ich habe Zahnschmerzen.'],
    answer: 0,
    whyDe: '„headache“ sind Kopfschmerzen, „toothache“ Zahnschmerzen.',
  }),
  I('h_0_2', 'hoeren', 0, 'listen', 'Was bedeutet der Satz?', {
    audioEn: 'She is my neighbour.',
    options: ['Sie ist meine Nachbarin.', 'Sie ist meine Tochter.'],
    answer: 0,
    whyDe: '„neighbour“ ist die Nachbarin, „daughter“ die Tochter.',
  }),
  I('h_0_3', 'hoeren', 0, 'listen', 'Was bedeutet der Satz?', {
    audioEn: 'The bus leaves at eight.',
    options: ['Der Bus fährt um acht.', 'Der Bus fährt um zehn.'],
    answer: 0,
    whyDe: '„eight“ ist acht, „ten“ ist zehn.',
  }),
  I('h_1_1', 'hoeren', 1, 'listen', 'Was bedeutet der Satz?', {
    audioEn: 'Could you speak a little slower, please?',
    options: ['Könnten Sie bitte etwas langsamer sprechen?', 'Könnten Sie bitte lauter sprechen?'],
    answer: 0,
    whyDe: '„slower“ heißt langsamer, „louder“ lauter.',
  }),
  I('h_1_2', 'hoeren', 1, 'listen', 'Was bedeutet der Satz?', {
    audioEn: 'I’ve had this cough for two weeks.',
    options: ['Ich habe diesen Husten seit zwei Wochen.', 'Ich hatte vor zwei Wochen Husten.'],
    answer: 0,
    whyDe: '„I have had … for two weeks“ beschreibt die Dauer bis heute.',
  }),
  I('h_1_3', 'hoeren', 1, 'listen', 'Was bedeutet der Satz?', {
    audioEn: 'Take the second street on the right.',
    options: ['Nehmen Sie die zweite Straße rechts.', 'Nehmen Sie die zweite Straße links.'],
    answer: 0,
    whyDe: '„on the right“ ist rechts, „on the left“ links.',
  }),
  I('h_2_1', 'hoeren', 2, 'listen', 'Was bedeutet der Satz?', {
    audioEn: 'If it rains tomorrow, we’ll stay at home.',
    options: ['Wenn es morgen regnet, bleiben wir zu Hause.', 'Wenn es morgen regnet, gehen wir spazieren.'],
    answer: 0,
    whyDe: '„stay at home“ heißt zu Hause bleiben.',
  }),
  I('h_2_2', 'hoeren', 2, 'listen', 'Was bedeutet der Satz?', {
    audioEn: 'I’d rather pay by card, if that’s all right.',
    options: ['Ich würde lieber mit Karte zahlen, wenn das in Ordnung ist.', 'Ich möchte bitte bar bezahlen.'],
    answer: 0,
    whyDe: '„I’d rather“ heißt ich würde lieber, „pay by card“ mit Karte zahlen.',
  }),
  I('h_2_3', 'hoeren', 2, 'listen', 'Was bedeutet der Satz?', {
    audioEn: 'The doctor said I should come back next week.',
    options: ['Die Ärztin sagte, ich soll nächste Woche wiederkommen.', 'Die Ärztin kommt nächste Woche zu mir.'],
    answer: 0,
    whyDe: '„I should come back“ heißt ich soll wiederkommen.',
  }),
  I('h_3_1', 'hoeren', 3, 'listen', 'Was bedeutet der Satz?', {
    audioEn: 'I was about to leave when the phone rang.',
    options: ['Ich wollte gerade gehen, als das Telefon klingelte.', 'Ich ging los, nachdem das Telefon geklingelt hatte.'],
    answer: 0,
    whyDe: '„was about to“ heißt wollte gerade; „when“ setzt das Klingeln mitten hinein.',
  }),
  I('h_3_2', 'hoeren', 3, 'listen', 'Was bedeutet der Satz?', {
    audioEn: 'It’s worth booking a table in advance.',
    options: ['Es lohnt sich, vorher einen Tisch zu reservieren.', 'Es ist besser, später einen Tisch zu reservieren.'],
    answer: 0,
    whyDe: '„worth“ heißt lohnend, „in advance“ im Voraus.',
  }),

  // ---- Grammatik --------------------------------------------------------------------------
  I('g_0_1', 'grammatik', 0, 'gap', 'Welches Wort passt?', {
    textEn: 'I ___ tired.', options: ['am', 'is'], answer: 0,
    whyDe: 'Zu „I“ gehört immer „am“.',
  }),
  I('g_0_2', 'grammatik', 0, 'gap', 'Welches Wort passt?', {
    textEn: 'She ___ two grandchildren.', options: ['has', 'have'], answer: 0,
    whyDe: 'Bei he/she/it steht „has“.',
  }),
  I('g_0_3', 'grammatik', 0, 'gap', 'Welches Wort passt?', {
    textEn: '___ you have a fever?', options: ['Do', 'Are'], answer: 0,
    whyDe: 'Fragen mit have bildet man mit do: „Do you have …?“',
  }),
  I('g_1_1', 'grammatik', 1, 'gap', 'Welches Wort passt?', {
    textEn: 'He ___ in a hospital.', options: ['works', 'work'], answer: 0,
    whyDe: 'Bei he/she/it bekommt das Verb ein -s.',
  }),
  I('g_1_2', 'grammatik', 1, 'gap', 'Welches Wort passt?', {
    textEn: 'I ___ her yesterday.', options: ['saw', 'seen'], answer: 0,
    whyDe: '„yesterday“ verlangt die einfache Vergangenheit: saw.',
  }),
  I('g_1_3', 'grammatik', 1, 'gap', 'Welches Wort passt?', {
    textEn: 'There ___ some apples on the table.', options: ['are', 'is'], answer: 0,
    whyDe: '„apples“ ist Plural, deshalb are.',
  }),
  I('g_2_1', 'grammatik', 2, 'gap', 'Welches Wort passt?', {
    textEn: 'I ___ here for three years.', options: ['have lived', 'live'], answer: 0,
    whyDe: '„for three years“ bis heute: have + Partizip.',
  }),
  I('g_2_2', 'grammatik', 2, 'gap', 'Welches Wort passt?', {
    textEn: 'She’s the woman ___ helped me.', options: ['who', 'which'], answer: 0,
    whyDe: 'Für Personen steht who, für Dinge which.',
  }),
  I('g_2_3', 'grammatik', 2, 'gap', 'Welches Wort passt?', {
    textEn: 'If I ___ more time, I would cook.', options: ['had', 'have'], answer: 0,
    whyDe: 'Unwirkliche Bedingung: if + had, dann would.',
  }),
  I('g_3_1', 'grammatik', 3, 'gap', 'Welches Wort passt?', {
    textEn: 'He asked me where I ___ from.', options: ['came', 'come'], answer: 0,
    whyDe: 'In der indirekten Rede verschiebt sich die Zeit zurück.',
  }),
  I('g_3_2', 'grammatik', 3, 'gap', 'Welches Wort passt?', {
    textEn: 'By the time we arrived, the film ___.', options: ['had started', 'has started'], answer: 0,
    whyDe: 'Vorzeitigkeit in der Vergangenheit: had + Partizip.',
  }),

  // ---- Bausteine --------------------------------------------------------------------------
  I('b_0_1', 'bausteine', 0, 'chunk', 'Was bedeutet dieser Baustein?', {
    textEn: 'I’m fine, thanks.', options: ['Mir geht es gut, danke.', 'Ich bin fertig, danke.'], answer: 0,
    whyDe: '„fine“ heißt hier gut, nicht fein.',
  }),
  I('b_0_2', 'bausteine', 0, 'chunk', 'Was bedeutet dieser Baustein?', {
    textEn: 'See you tomorrow.', options: ['Bis morgen.', 'Bis später.'], answer: 0,
    whyDe: '„tomorrow“ ist morgen, „later“ ist später.',
  }),
  I('b_0_3', 'bausteine', 0, 'chunk', 'Was bedeutet dieser Baustein?', {
    textEn: 'How much is it?', options: ['Wie viel kostet es?', 'Wie viele sind es?'], answer: 0,
    whyDe: '„How much“ fragt nach dem Preis.',
  }),
  I('b_1_1', 'bausteine', 1, 'chunk', 'Was bedeutet dieser Baustein?', {
    textEn: 'Could I have the bill, please?', options: ['Könnte ich bitte die Rechnung haben?', 'Könnte ich bitte die Speisekarte haben?'], answer: 0,
    whyDe: '„bill“ ist die Rechnung, „menu“ die Speisekarte.',
  }),
  I('b_1_2', 'bausteine', 1, 'chunk', 'Was bedeutet dieser Baustein?', {
    textEn: 'I’d like to make an appointment.', options: ['Ich möchte einen Termin machen.', 'Ich möchte etwas bestellen.'], answer: 0,
    whyDe: '„appointment“ ist der Termin.',
  }),
  I('b_1_3', 'bausteine', 1, 'chunk', 'Was bedeutet dieser Baustein?', {
    textEn: 'It doesn’t matter.', options: ['Das macht nichts.', 'Das ist wichtig.'], answer: 0,
    whyDe: '„it doesn’t matter“ heißt es ist egal.',
  }),
  I('b_2_1', 'bausteine', 2, 'chunk', 'Was bedeutet dieser Baustein?', {
    textEn: 'I’m afraid I can’t make it.', options: ['Leider kann ich nicht kommen.', 'Ich fürchte, ich habe es nicht geschafft.'], answer: 0,
    whyDe: '„can’t make it“ heißt nicht kommen können.',
  }),
  I('b_2_2', 'bausteine', 2, 'chunk', 'Was bedeutet dieser Baustein?', {
    textEn: 'Would you mind repeating that?', options: ['Würden Sie das bitte wiederholen?', 'Hätten Sie etwas dagegen, wenn ich das wiederhole?'], answer: 0,
    whyDe: '„Would you mind …ing“ ist die höfliche Bitte.',
  }),
  I('b_2_3', 'bausteine', 2, 'chunk', 'Was bedeutet dieser Baustein?', {
    textEn: 'That sounds like a good idea.', options: ['Das klingt nach einer guten Idee.', 'Das hört sich gefährlich an.'], answer: 0,
    whyDe: '„sounds like“ heißt klingt nach.',
  }),
  I('b_3_1', 'bausteine', 3, 'chunk', 'Was bedeutet dieser Baustein?', {
    textEn: 'I’ve been meaning to call you.', options: ['Ich wollte dich schon länger anrufen.', 'Ich habe dich gerade angerufen.'], answer: 0,
    whyDe: '„have been meaning to“ heißt es schon länger vorhaben.',
  }),
  I('b_3_2', 'bausteine', 3, 'chunk', 'Was bedeutet dieser Baustein?', {
    textEn: 'Let’s take it one step at a time.', options: ['Gehen wir es Schritt für Schritt an.', 'Lass uns einen Schritt zurückgehen.'], answer: 0,
    whyDe: '„one step at a time“ heißt Schritt für Schritt.',
  }),

  // ---- Sprechen ---------------------------------------------------------------------------
  I('s_0_1', 'sprechen', 0, 'speak', 'Sprich den Satz einmal laut.', { model: 'Good morning.' }),
  I('s_0_2', 'sprechen', 0, 'speak', 'Sprich den Satz einmal laut.', { model: 'Thank you very much.' }),
  I('s_0_3', 'sprechen', 0, 'speak', 'Sprich den Satz einmal laut.', { model: 'I’m sorry.' }),
  I('s_1_1', 'sprechen', 1, 'speak', 'Sprich die höfliche Bitte.', { model: 'Could you say that again, please?' }),
  I('s_1_2', 'sprechen', 1, 'speak', 'Sag, was dir weh tut.', { model: 'I have a bad headache.' }),
  I('s_1_3', 'sprechen', 1, 'speak', 'Frag nach dem Bus.', { model: 'Where is the bus stop?' }),
  I('s_2_1', 'sprechen', 2, 'speak', 'Reserviere einen Tisch.', { model: 'I’d like to book a table for two, please.' }),
  I('s_2_2', 'sprechen', 2, 'speak', 'Erzähl, wie lange du hier wohnst.', { model: 'I’ve lived in this street for ten years.' }),
  I('s_2_3', 'sprechen', 2, 'speak', 'Bitte höflich um einen Vormittagstermin.', { model: 'If it’s possible, I’d prefer a morning appointment.' }),
  I('s_3_1', 'sprechen', 3, 'speak', 'Sag, dass du nachfragen möchtest.', { model: 'I’m not sure I understood – could you explain it in another way?' }),
  I('s_3_2', 'sprechen', 3, 'speak', 'Erzähl, was dir am besten gefallen hat.', { model: 'What I liked most was how quiet the town was.' }),

  // ---- Verknüpfen -------------------------------------------------------------------------
  I('v_0_1', 'verknuepfen', 0, 'connector', 'Welches Wort verbindet die Sätze?', {
    textEn: 'I’m tired ___ I’m happy.', options: ['but', 'because'], answer: 0,
    whyDe: 'Gegensatz: but (aber).',
  }),
  I('v_0_2', 'verknuepfen', 0, 'connector', 'Welches Wort verbindet die Sätze?', {
    textEn: 'I drink tea ___ I get up.', options: ['when', 'that'], answer: 0,
    whyDe: 'Gleichzeitigkeit: when (wenn).',
  }),
  I('v_0_3', 'verknuepfen', 0, 'connector', 'Welches Wort verbindet die Sätze?', {
    textEn: 'She is here ___ she wants to help.', options: ['because', 'but'], answer: 0,
    whyDe: 'Grund: because (weil).',
  }),
  I('v_1_1', 'verknuepfen', 1, 'connector', 'Welches Wort verbindet die Sätze?', {
    textEn: 'I’ll call you ___ I arrive.', options: ['when', 'that'], answer: 0,
    whyDe: 'Zeitpunkt: when.',
  }),
  I('v_1_2', 'verknuepfen', 1, 'connector', 'Welches Wort verbindet die Sätze?', {
    textEn: 'It was raining, ___ we stayed inside.', options: ['so', 'because'], answer: 0,
    whyDe: 'Folge: so (deshalb).',
  }),
  I('v_1_3', 'verknuepfen', 1, 'connector', 'Welches Wort verbindet die Sätze?', {
    textEn: 'I don’t know ___ he is coming.', options: ['if', 'that'], answer: 0,
    whyDe: 'Unsicherheit: if (ob).',
  }),
  I('v_2_1', 'verknuepfen', 2, 'connector', 'Welches Wort verbindet die Sätze?', {
    textEn: '___ I was cooking, the phone rang.', options: ['While', 'During'], answer: 0,
    whyDe: 'Vor einem Satz steht while, vor einem Nomen during.',
  }),
  I('v_2_2', 'verknuepfen', 2, 'connector', 'Welches Wort verbindet die Sätze?', {
    textEn: 'I stayed at home ___ I was ill.', options: ['because', 'although'], answer: 0,
    whyDe: 'Grund: because; although wäre ein Gegensatz.',
  }),
  I('v_2_3', 'verknuepfen', 2, 'connector', 'Welches Wort verbindet die Sätze?', {
    textEn: 'She speaks slowly ___ I can understand.', options: ['so that', 'in order'], answer: 0,
    whyDe: 'Ziel mit Subjekt: so that.',
  }),
  I('v_3_1', 'verknuepfen', 3, 'connector', 'Welches Wort verbindet die Sätze?', {
    textEn: '___ I had known, I would have come.', options: ['If', 'When'], answer: 0,
    whyDe: 'Unwirkliche Vergangenheit: if + had known.',
  }),
  I('v_3_2', 'verknuepfen', 3, 'connector', 'Welches Wort verbindet die Sätze?', {
    textEn: 'He kept talking, ___ nobody was listening.', options: ['although', 'because'], answer: 0,
    whyDe: 'Gegensatz: although (obwohl).',
  }),
];

/**
 * The right answer must not always sit in the first place: otherwise a learner could pass by
 * tapping the top card. The shuffle is deterministic per id, so content and tests stay stable.
 */
function shuffleOptions(item) {
  if (!item.options?.length) return item;
  let seed = 7;
  for (const char of item.id) seed = (seed * 31 + char.charCodeAt(0)) % 100000;
  const order = item.options.map((_, index) => index);
  for (let i = order.length - 1; i > 0; i -= 1) {
    seed = (seed * 1103515245 + 12345) % 2147483648;
    const j = seed % (i + 1);
    [order[i], order[j]] = [order[j], order[i]];
  }
  return {
    ...item,
    options: order.map((index) => item.options[index]),
    answer: order.indexOf(item.answer),
  };
}

export const checkup = raw.map(shuffleOptions);
