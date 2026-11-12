// Lehrgang „Zahlen & Zeiten“: Uhrzeiten, Preise, Datum und Telefonnummern.
// Dieselbe Form wie new-lessons.js, damit expand-content.js ihn ohne Sonderweg einbaut.
// Jede Lektion trägt genau zwei Bausteine – so bleibt die Inhaltsprüfung gültig.
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

const lessons = [
  // --- Bürgeramt: Öffnungszeiten und Termine ---
  L("amt_l4", "amt", "Öffnungszeiten", "Ich kann Öffnungszeiten verstehen und erfragen.", 4, "prepositions_space", "zahlen", "A1",
    ["The office opens at eight o’clock.", "Das Amt öffnet um acht Uhr.", "at eight o’clock", "Uhrzeiten stehen mit „at“."],
    ["It closes at half past four.", "Es schließt um halb fünf.", "half past four", "„half past“ heißt halb nach – also halb fünf."],
    ["Am Eingang steht ein Schild mit den Zeiten.", [["peters", "The office opens at eight o’clock.", "Das Amt öffnet um acht Uhr."], ["you", "And when does it close?", "Und wann schließt es?"], ["peters", "It closes at half past four.", "Es schließt um halb fünf."]]],
    ["Wann schließt das Amt?", ["Um halb fünf.", "Um fünf Uhr."], "„half past four“ ist 4:30 – auf Deutsch halb fünf."],
    "Uhrzeiten stehen mit „at“: at eight o’clock, at half past four. „half past“ ist die halbe Stunde danach, „quarter to“ die Viertelstunde davor.",
    "Uhrzeit · at half past",
    ["Sag: Das Amt öffnet um acht Uhr.", "The office opens at eight o’clock.", ["opens"]],
    ["Welcher Satz ist richtig?", "The office opens at eight o’clock.", "The office opens on eight o’clock.", "Vor der Uhrzeit steht „at“, nicht „on“."],
    ["opens_at", "It opens at ___", "Es öffnet um ___"]),

  L("amt_l5", "amt", "Der Termin", "Ich kann einen Termin mit Datum und Uhrzeit nennen.", 5, "word_order_question", "zahlen", "A1–A2",
    ["My appointment is at ten past nine.", "Mein Termin ist um zehn nach neun.", "ten past nine", "„past“ heißt nach der vollen Stunde."],
    ["That’s Tuesday, the third of May.", "Das ist Dienstag, der dritte Mai.", "the third of May", "Datum: Tag zuerst, dann der Monat."],
    ["Frau Peters liest den Termin vom Zettel ab.", [["you", "My appointment is at ten past nine.", "Mein Termin ist um zehn nach neun."], ["peters", "That’s Tuesday, the third of May.", "Das ist Dienstag, der dritte Mai."], ["you", "Thank you very much.", "Vielen Dank."]]],
    ["Wann ist der Termin?", ["Um zehn nach neun.", "Um neun Uhr."], "„ten past nine“ ist zehn Minuten nach neun."],
    "Datum und Uhrzeit stehen anders als im Deutschen: erst der Tag, dann der Monat – und vor der Uhrzeit steht „at“.",
    "Termin nennen · at ten past nine",
    ["Sag: Mein Termin ist um zehn nach neun.", "My appointment is at ten past nine.", ["appointment"]],
    ["Welcher Satz ist richtig?", "My appointment is at ten past nine.", "My appointment is on ten past nine.", "Vor der Uhrzeit steht „at“."],
    ["appointment_at", "My appointment is at ___", "Mein Termin ist um ___"]),

  // --- Telefon: Nummern verstehen ---
  L("telefon_l4", "telefon", "Nummern am Telefon", "Ich kann eine Nummer erfragen und wiederholen lassen.", 4, "modals_politeness", "zahlen", "A1",
    ["My number is oh seven seven, four one.", "Meine Nummer ist null sieben sieben, vier eins.", "oh seven seven", "Die Null heißt am Telefon „oh“."],
    ["Could you repeat that, please?", "Könnten Sie das bitte wiederholen?", "repeat that", "„Could you“ ist die höfliche Bitte."],
    ["Die Praxis nennt die Nummer zu schnell.", [["weber", "My number is oh seven seven, four one.", "Meine Nummer ist null sieben sieben, vier eins."], ["you", "Sorry, could you repeat that, please?", "Entschuldigung, könnten Sie das bitte wiederholen?"], ["weber", "Of course: oh seven seven, four one.", "Natürlich: null sieben sieben, vier eins."]]],
    ["Was soll wiederholt werden?", ["Die Telefonnummer.", "Der Termin."], "„repeat that“ heißt „das wiederholen“."],
    "Ziffern werden einzeln gesprochen: „oh“ für die Null, „double“ für zwei gleiche Ziffern. Wer nichts versteht, fragt einfach nach.",
    "Nummer erfragen · could you repeat",
    ["Sag: Könnten Sie das bitte wiederholen?", "Could you repeat that, please?", ["repeat"]],
    ["Welcher Satz ist richtig?", "Could you repeat that, please?", "Could you to repeat that, please?", "Nach „could you“ steht das Verb ohne „to“."],
    ["could_repeat", "Could you repeat ___?", "Könnten Sie ___ wiederholen?"]),

  // --- Supermarkt: Preise ---
  L("supermarkt_l4", "supermarkt", "Preise verstehen", "Ich kann Preise hören und nachfragen.", 4, "articles_quantity", "zahlen", "A1",
    ["That’s two pounds fifty, please.", "Das macht zwei Pfund fünfzig, bitte.", "two pounds fifty", "„pounds“ im Plural, „fifty“ ohne pence."],
    ["It’s on special offer today.", "Es ist heute im Angebot.", "on special offer", "Angebote heißen „special offer“."],
    ["An der Kasse wird es voll.", [["krueger", "That’s two pounds fifty, please.", "Das macht zwei Pfund fünfzig, bitte."], ["you", "Here you are. What’s on special offer?", "Bitte. Was ist im Angebot?"], ["krueger", "The milk is on special offer today.", "Die Milch ist heute im Angebot."]]],
    ["Wie viel kostet der Einkauf?", ["Zwei Pfund fünfzig.", "Fünf Pfund zwanzig."], "„two pounds fifty“ sind 2,50 £."],
    "Preise: erst Pfund, dann Pence – „two pounds fifty“. Bei mehr als einem Pfund steht der Plural: pounds.",
    "Preis nennen · two pounds fifty",
    ["Sag: Das macht zwei Pfund fünfzig.", "That’s two pounds fifty.", ["pounds"]],
    ["Welcher Satz ist richtig?", "That’s two pounds fifty.", "That’s two pound fifty.", "Mehr als eins: pounds im Plural."],
    ["costs_price", "That’s ___ , please", "Das macht ___ , bitte"]),

  // --- Restaurant: Rechnung und Zeiten ---
  L("restaurant_l4", "restaurant", "Die Rechnung", "Ich kann nach der Rechnung fragen und Beträge verstehen.", 4, "articles_quantity", "zahlen", "A1–A2",
    ["The bill comes to twenty-four pounds.", "Die Rechnung beträgt vierundzwanzig Pfund.", "comes to", "„comes to“ heißt „beträgt“."],
    ["We’re open until half past ten.", "Wir haben bis halb elf geöffnet.", "until half past ten", "„until“ heißt bis."],
    ["Am Ende des Abends kommt die Rechnung.", [["you", "Could we have the bill, please?", "Könnten wir bitte die Rechnung haben?"], ["lena", "The bill comes to twenty-four pounds.", "Die Rechnung beträgt vierundzwanzig Pfund."], ["you", "Thank you. Are you open until half past ten?", "Danke. Haben Sie bis halb elf geöffnet?"], ["lena", "Yes, until half past ten.", "Ja, bis halb elf."]]],
    ["Bis wann ist das Restaurant geöffnet?", ["Bis halb elf.", "Bis zehn Uhr."], "„half past ten“ ist 22:30 – halb elf."],
    "Beträge: „comes to“ für die Summe. Zeiten mit „until“, wenn etwas bis dahin dauert.",
    "Rechnung · comes to",
    ["Sag: Die Rechnung beträgt vierundzwanzig Pfund.", "The bill comes to twenty-four pounds.", ["bill"]],
    ["Welcher Satz ist richtig?", "The bill comes to twenty-four pounds.", "The bill comes to twenty-four pound.", "Mehr als eins: pounds."],
    ["bill_comes_to", "The bill comes to ___", "Die Rechnung beträgt ___"]),

  // --- Fahrkartenautomat: Abfahrt und Preis ---
  L("fahrkartenautomat_l4", "fahrkartenautomat", "Abfahrt und Preis", "Ich kann Abfahrtszeiten und Fahrkartenpreise verstehen.", 4, "prepositions_space", "zahlen", "A1",
    ["The next train leaves at quarter past seven.", "Der nächste Zug fährt um Viertel nach sieben.", "quarter past seven", "„quarter past“ heißt Viertel nach."],
    ["A day ticket costs six pounds fifty.", "Eine Tageskarte kostet sechs Pfund fünfzig.", "a day ticket", "Tageskarte = day ticket."],
    ["Der Bahnsteig ist voll, die Anzeige springt.", [["krueger", "The next train leaves at quarter past seven.", "Der nächste Zug fährt um Viertel nach sieben."], ["you", "And what does a day ticket cost?", "Und was kostet eine Tageskarte?"], ["krueger", "A day ticket costs six pounds fifty.", "Eine Tageskarte kostet sechs Pfund fünfzig."]]],
    ["Wann fährt der nächste Zug?", ["Um Viertel nach sieben.", "Um Viertel vor sieben."], "„quarter past seven“ ist 7:15 – Viertel nach sieben."],
    "„quarter past“ ist Viertel nach der Stunde, „quarter to“ Viertel vor der nächsten. Beides steht mit „at“.",
    "Abfahrt · quarter past",
    ["Sag: Der nächste Zug fährt um Viertel nach sieben.", "The next train leaves at quarter past seven.", ["train"]],
    ["Welcher Satz ist richtig?", "The train leaves at quarter past seven.", "The train leaves in quarter past seven.", "Vor der Uhrzeit steht „at“."],
    ["leaves_at", "The train leaves at ___", "Der Zug fährt um ___"]),

  // --- Hotel: Zeiten am Empfang ---
  L("hotel_l4", "hotel", "Zeiten am Empfang", "Ich kann Check-out und Frühstückszeiten erfragen.", 4, "prepositions_space", "zahlen", "A1",
    ["Check-out is at eleven o’clock.", "Der Check-out ist um elf Uhr.", "at eleven o’clock", "„o’clock“ nur bei vollen Stunden."],
    ["Breakfast is served from seven until ten.", "Frühstück gibt es von sieben bis zehn.", "from seven until ten", "„from … until …“ heißt von … bis …."],
    ["Du fragst an der Rezeption nach den Zeiten.", [["you", "When is check-out?", "Wann ist der Check-out?"], ["lena", "Check-out is at eleven o’clock.", "Der Check-out ist um elf Uhr."], ["you", "And breakfast?", "Und das Frühstück?"], ["lena", "Breakfast is served from seven until ten.", "Frühstück gibt es von sieben bis zehn."]]],
    ["Wann gibt es Frühstück?", ["Von sieben bis zehn.", "Von zehn bis elf."], "„from seven until ten“ – von sieben bis zehn."],
    "Für Zeiträume steht „from … until …“. „o’clock“ gehört nur zur vollen Stunde, nicht zu „half past“.",
    "Zeitraum · from until",
    ["Sag: Der Check-out ist um elf Uhr.", "Check-out is at eleven o’clock.", ["check-out"]],
    ["Welcher Satz ist richtig?", "Check-out is at eleven o’clock.", "Check-out is on eleven o’clock.", "Vor der Uhrzeit steht „at“."],
    ["checkout_at", "Check-out is at ___", "Der Check-out ist um ___"]),

  // --- Bus: Abfahrt und Entfernung ---
  L("bus_l4", "bus", "Wann fährt der Bus?", "Ich kann Abfahrtszeiten und Entfernungen verstehen.", 4, "word_order_question", "zahlen", "A1",
    ["The bus leaves at twenty past eight.", "Der Bus fährt um zwanzig nach acht.", "twenty past eight", "„past“ heißt nach der vollen Stunde."],
    ["It’s a ten-minute walk from here.", "Von hier sind es zehn Minuten zu Fuß.", "a ten-minute walk", "Zwischen Zahl und Nomen steht ein Bindestrich."],
    ["An der Haltestelle hängt ein Fahrplan.", [["hana", "The bus leaves at twenty past eight.", "Der Bus fährt um zwanzig nach acht."], ["you", "How far is the stop?", "Wie weit ist die Haltestelle?"], ["hana", "It’s a ten-minute walk from here.", "Von hier sind es zehn Minuten zu Fuß."]]],
    ["Wie weit ist die Haltestelle?", ["Zehn Minuten zu Fuß.", "Zwanzig Minuten zu Fuß."], "„a ten-minute walk“ – zehn Minuten zu Fuß."],
    "Entfernungen zu Fuß: „a ten-minute walk“. Die Zahl bekommt einen Bindestrich, das Nomen bleibt im Singular.",
    "Entfernung · ten-minute walk",
    ["Sag: Der Bus fährt um zwanzig nach acht.", "The bus leaves at twenty past eight.", ["leaves"]],
    ["Welcher Satz ist richtig?", "It’s a ten-minute walk.", "It’s a ten-minutes walk.", "Mit Bindestrich bleibt das Nomen im Singular."],
    ["walk_minutes", "It’s a ___-minute walk", "Es sind ___ Minuten zu Fuß"]),

  // --- Bank: Beträge ---
  L("bank_l4", "bank", "Geld abheben", "Ich kann einen Betrag nennen und verstehen.", 4, "modals_politeness", "zahlen", "A2",
    ["I’d like to withdraw a hundred pounds.", "Ich möchte hundert Pfund abheben.", "withdraw", "„withdraw“ heißt abheben."],
    ["The machine is out of order.", "Der Automat ist außer Betrieb.", "out of order", "„out of order“ heißt außer Betrieb."],
    ["Der Geldautomat ist kaputt, du gehst zum Schalter.", [["peters", "How much would you like?", "Wie viel möchten Sie?"], ["you", "I’d like to withdraw a hundred pounds.", "Ich möchte hundert Pfund abheben."], ["peters", "The machine is out of order, I’m afraid.", "Der Automat ist leider außer Betrieb."]]],
    ["Wie viel möchte die Person abheben?", ["Hundert Pfund.", "Fünfzig Pfund."], "„a hundred pounds“ – hundert Pfund."],
    "„would like“ braucht „to“ vor dem Verb: I’d like to withdraw. Beträge stehen ohne „of“: a hundred pounds.",
    "Betrag nennen · I’d like to",
    ["Sag: Ich möchte hundert Pfund abheben.", "I’d like to withdraw a hundred pounds.", ["withdraw"]],
    ["Welcher Satz ist richtig?", "I’d like to withdraw a hundred pounds.", "I’d like withdraw a hundred pounds.", "Nach „would like“ steht „to“."],
    ["would_like_to", "I’d like to ___", "Ich möchte ___"]),

  // --- Post: Sendungen und Zeiten ---
  L("post_l4", "post", "Sendungen und Zeiten", "Ich kann Lieferzeiten und Öffnungszeiten verstehen.", 4, "prepositions_space", "zahlen", "A1",
    ["It should arrive in two days.", "Es sollte in zwei Tagen ankommen.", "in two days", "„in two days“ heißt in zwei Tagen."],
    ["The post office closes at half past five.", "Die Post schließt um halb sechs.", "half past five", "„half past five“ ist 5:30 – halb sechs."],
    ["Du gibst einen Brief ab und fragst nach der Dauer.", [["you", "How long will it take?", "Wie lange dauert es?"], ["peters", "It should arrive in two days.", "Es sollte in zwei Tagen ankommen."], ["you", "Thank you. And when do you close?", "Danke. Und wann schließen Sie?"], ["peters", "The post office closes at half past five.", "Die Post schließt um halb sechs."]]],
    ["Wann schließt die Post?", ["Um halb sechs.", "Um sechs Uhr."], "„half past five“ ist 5:30 – halb sechs."],
    "Dauer mit „in“: in two days. Uhrzeit mit „at“: at half past five. Beides steht ohne Artikel.",
    "Dauer · in two days",
    ["Sag: Es sollte in zwei Tagen ankommen.", "It should arrive in two days.", ["arrive"]],
    ["Welcher Satz ist richtig?", "It should arrive in two days.", "It should arrive after two days.", "Für die Dauer steht „in“."],
    ["arrive_in", "It should arrive in ___", "Es sollte in ___ ankommen"]),

  // --- Zahnarzt: Termin verschieben ---
  L("zahnarzt_l4", "zahnarzt", "Termin verschieben", "Ich kann einen Termin auf eine andere Zeit legen.", 4, "prepositions_space", "zahlen", "A1–A2",
    ["Could we move it to quarter to four?", "Könnten wir es auf Viertel vor vier verschieben?", "move it to", "„move to“ + Zeitpunkt."],
    ["That’s the fifteenth of June.", "Das ist der fünfzehnte Juni.", "the fifteenth of June", "Datum: Ordnungszahl + of + Monat."],
    ["Die Praxis ruft an, weil sich der Termin verschiebt.", [["weber", "Could we move it to quarter to four?", "Könnten wir es auf Viertel vor vier verschieben?"], ["you", "Which day is that?", "Welcher Tag ist das?"], ["weber", "That’s the fifteenth of June.", "Das ist der fünfzehnte Juni."]]],
    ["Auf welche Uhrzeit soll verschoben werden?", ["Viertel vor vier.", "Viertel nach vier."], "„quarter to four“ ist 3:45 – Viertel vor vier."],
    "„quarter to four“ heißt Viertel vor vier. Termine verschiebt man mit „move it to“ und dem neuen Zeitpunkt.",
    "Termin verschieben · move to",
    ["Sag: Könnten wir es auf Viertel vor vier verschieben?", "Could we move it to quarter to four?", ["move"]],
    ["Welcher Satz ist richtig?", "Could we move it to quarter to four?", "Could we move it on quarter to four?", "„move“ + to + Zeitpunkt."],
    ["move_to", "Could we move it to ___?", "Könnten wir es auf ___ verschieben?"]),

  // --- Markt: Mengen und Preise ---
  L("markt_l4", "markt", "Mengen und Preise", "Ich kann Mengen bestellen und den Preis verstehen.", 4, "articles_quantity", "zahlen", "A1",
    ["Two pounds of tomatoes, please.", "Zwei Pfund Tomaten, bitte.", "pounds of", "Menge + of + Nomen."],
    ["That’s one pound twenty.", "Das macht ein Pfund zwanzig.", "one pound twenty", "Ein Pfund bleibt im Singular."],
    ["Am Stand riecht es nach Tomaten.", [["you", "Two pounds of tomatoes, please.", "Zwei Pfund Tomaten, bitte."], ["hana", "Here you are. That’s one pound twenty.", "Bitte schön. Das macht ein Pfund zwanzig."], ["you", "Here you are.", "Bitte."]]],
    ["Was kostet die Ware?", ["Ein Pfund zwanzig.", "Zwei Pfund zwanzig."], "„one pound twenty“ sind 1,20 £."],
    "Mengen brauchen „of“: two pounds of tomatoes. Bei genau einem Pfund steht der Singular: one pound twenty.",
    "Menge · pounds of",
    ["Sag: Zwei Pfund Tomaten, bitte.", "Two pounds of tomatoes, please.", ["pounds"]],
    ["Welcher Satz ist richtig?", "Two pounds of tomatoes, please.", "Two pounds tomatoes, please.", "Zwischen Menge und Nomen steht „of“."],
    ["amount_of", "___ of ___ , please", "___ ___ , bitte"]),
];

module.exports = { lessons };
