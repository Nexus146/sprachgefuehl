import { S, L, q, chunk } from './lib.mjs';

export const situations2 = [
  S('weg', 'unterwegs', 'Nach dem Weg fragen', 'Höflich nach dem Weg fragen', 'Du suchst den Bahnhof.', 'map', [
    L('Entschuldigung', 'Ich kann höflich nach dem Weg fragen.', 'prepositions_space', 1,
      [
        ['you', 'Excuse me, where is the station?', 'Entschuldigung, wo ist der Bahnhof?'],
        ['stranger', 'Go straight and turn left at the lights.', 'Gehen Sie geradeaus und biegen Sie an der Ampel links ab.'],
        ['you', 'Is it far?', 'Ist es weit?'],
        ['stranger', 'No, about five minutes.', 'Nein, etwa fünf Minuten.'],
      ],
      [
        q('Was sucht die Person?', ['Den Bahnhof', 'Das Hotel'], 0, '„the station“ = der Bahnhof.'),
        q('Wie weit ist es?', ['Etwa fünf Minuten', 'Etwa fünf Kilometer'], 0, '„about five minutes“ = etwa fünf Minuten.'),
      ],
      [
        chunk('Where is the station?', 'Wo ist der Bahnhof?', 'where_is', 'Where is ___?', 'Wo ist ___?', 'the station', 'where + is + Ort.'),
        chunk('Go straight and turn left.', 'Gehen Sie geradeaus und biegen Sie links ab.', 'go_turn', 'Go straight and turn ___', 'Gehen Sie geradeaus und biegen Sie ___ ab', 'left', 'turn left / turn right.'),
      ],
      { promptDe: 'Frage nach dem Bahnhof.', model: 'Excuse me, where is the station?', expect: ['station'] },
      q('Welche Frage ist richtig?', ['Where is the station?', 'Where the station is?'], 0, 'Frage: where + is + Ort.'),
      '„Excuse me“ öffnet jedes Gespräch mit Fremden – es entschuldigt die Störung, nicht den Fehler.'),
    L('Wegbeschreibung', 'Ich kann Wegbeschreibungen verstehen.', 'prepositions_space', 2,
      [
        ['stranger', 'It’s next to the bank.', 'Es ist neben der Bank.'],
        ['you', 'Is there a bus?', 'Gibt es einen Bus?'],
        ['stranger', 'Yes, the number five stops there.', 'Ja, die Linie fünf hält dort.'],
        ['you', 'Thank you for your help.', 'Danke für Ihre Hilfe.'],
      ],
      [
        q('Wo ist der Bahnhof?', ['Neben der Bank', 'Hinter der Kirche'], 0, '„next to the bank“ = neben der Bank.'),
        q('Welcher Bus hält dort?', ['Linie fünf', 'Linie neun'], 0, '„number five“ = Linie fünf.'),
      ],
      [
        chunk('It’s next to the bank.', 'Es ist neben der Bank.', 'next_to', 'It’s next to ___', 'Es ist neben ___', 'the bank', 'next to = direkt neben.'),
        chunk('Is there a bus?', 'Gibt es einen Bus?', 'is_there', 'Is there ___?', 'Gibt es ___?', 'a bus', 'Is there = gibt es (Singular).'),
      ],
      { promptDe: 'Frage: „Gibt es einen Bus?“', model: 'Is there a bus?', expect: ['bus'] },
      q('Was heißt „next to“?', ['neben', 'gegenüber'], 0, 'next to = direkt neben.'),
      'Wege bestehen aus drei Bausteinen: Richtung (go straight), Abbiegung (turn left), Orientierung (next to the bank).'),
    L('Nochmal fragen', 'Ich kann nachfragen, wenn ich nicht verstehe.', 'function_cards', 2,
      [
        ['stranger', 'Take the second turning on the right.', 'Nehmen Sie die zweite Abzweigung rechts.'],
        ['you', 'Sorry, could you say that again?', 'Entschuldigung, könnten Sie das noch einmal sagen?'],
        ['stranger', 'Take the second street on the right.', 'Nehmen Sie die zweite Straße rechts.'],
        ['you', 'Now I understand. Thank you.', 'Jetzt verstehe ich. Danke.'],
      ],
      [
        q('Was macht die Person, als sie nicht versteht?', ['Sie fragt nach', 'Sie geht weg'], 0, '„Could you say that again?“ = Könnten Sie das wiederholen?'),
        q('Wo soll sie abbiegen?', ['Zweite Straße rechts', 'Erste Straße links'], 0, '„the second street on the right“.'),
      ],
      [
        chunk('Could you say that again?', 'Könnten Sie das noch einmal sagen?', 'could_you_say', 'Could you say that again?', 'Könnten Sie das noch einmal sagen?', '', 'Die wichtigste Rettungsphrase.'),
        chunk('I don’t understand.', 'Ich verstehe nicht.', 'dont_understand', 'I don’t understand.', 'Ich verstehe nicht.', '', 'Ehrlich und höflich.'),
      ],
      { promptDe: 'Du hast nicht verstanden. Frage nach.', model: 'Sorry, could you say that again?', expect: ['again'] },
      q('Welche Phrase hilft, wenn du nichts verstehst?', ['Could you say that again?', 'What is your name?'], 0, 'Nachfragen ist eine Strategie, kein Fehler.'),
      'Nachfragen ist keine Schwäche – Muttersprachler fragen genauso.'),
  ]),

  S('bus', 'unterwegs', 'Bus & Bahn', 'Ticket, Gleis, Umsteigen', 'Du fährst mit dem Bus zum Bahnhof.', 'bus', [
    L('Welcher Bus?', 'Ich kann nach dem richtigen Bus fragen.', 'word_order_question', 2,
      [
        ['you', 'Does this bus go to the centre?', 'Fährt dieser Bus ins Zentrum?'],
        ['driver', 'Yes, it does.', 'Ja.'],
        ['you', 'How much is a ticket?', 'Was kostet ein Ticket?'],
        ['driver', 'Two euros.', 'Zwei Euro.'],
      ],
      [
        q('Wohin fährt der Bus?', ['Ins Zentrum', 'Zum Bahnhof'], 0, '„the centre“ = das Zentrum.'),
        q('Was kostet ein Ticket?', ['Zwei Euro', 'Drei Euro'], 0, '„two euros“.'),
      ],
      [
        chunk('Does this bus go to the centre?', 'Fährt dieser Bus ins Zentrum?', 'does_this_go', 'Does this bus go to ___?', 'Fährt dieser Bus ___?', 'the centre', 'Frage mit does + Grundform.'),
        chunk('How much is a ticket?', 'Was kostet ein Ticket?', 'how_much', 'How much is ___?', 'Was kostet ___?', 'a ticket', 'how much = wie viel.'),
      ],
      { promptDe: 'Frage, ob der Bus ins Zentrum fährt.', model: 'Does this bus go to the centre?', expect: ['bus', 'centre'] },
      q('Welche Frage ist richtig?', ['Does this bus go to the centre?', 'Goes this bus to the centre?'], 0, 'Frage mit does + Grundform.'),
      'do/does/did sind die Frage-Hilfsverben. Sie tragen die Zeit, das Hauptverb bleibt in der Grundform.'),
    L('Ticket', 'Ich kann ein Ticket kaufen.', 'articles_quantity', 2,
      [
        ['you', 'A return ticket to the station, please.', 'Eine Hin- und Rückfahrkarte zum Bahnhof, bitte.'],
        ['clerk', 'That’s five euros.', 'Das macht fünf Euro.'],
        ['you', 'When does the next train leave?', 'Wann fährt der nächste Zug?'],
        ['clerk', 'In ten minutes, from platform two.', 'In zehn Minuten, von Gleis zwei.'],
      ],
      [
        q('Was kauft die Person?', ['Hin- und Rückfahrt', 'Nur Hinfahrt'], 0, '„return ticket“ = Hin- und Rückfahrkarte.'),
        q('Wann fährt der Zug?', ['In zehn Minuten', 'In einer Stunde'], 0, '„in ten minutes“ = in zehn Minuten.'),
      ],
      [
        chunk('A return ticket, please.', 'Eine Hin- und Rückfahrkarte, bitte.', 'a_ticket', 'A ___ ticket, please.', 'Eine ___ Fahrkarte, bitte.', 'return', 'return = hin und zurück; single = einfach.'),
        chunk('When does the next train leave?', 'Wann fährt der nächste Zug?', 'when_does', 'When does ___ leave?', 'Wann fährt ___ ab?', 'the next train', 'when + does + Subjekt + Grundform.'),
      ],
      { promptDe: 'Kaufe eine Rückfahrkarte.', model: 'A return ticket, please.', expect: ['return', 'ticket'] },
      q('Was heißt „return ticket“?', ['Hin- und Rückfahrt', 'Nur Hinfahrt'], 0, 'return = Rückfahrkarte.'),
      'single = einfach, return = hin und zurück.'),
    L('Umsteigen', 'Ich kann nach dem Umsteigen fragen.', 'modals_politeness', 2,
      [
        ['you', 'Do I have to change?', 'Muss ich umsteigen?'],
        ['clerk', 'Yes, change at Central Station.', 'Ja, steigen Sie am Hauptbahnhof um.'],
        ['you', 'Which platform?', 'Welches Gleis?'],
        ['clerk', 'Platform four.', 'Gleis vier.'],
      ],
      [
        q('Muss die Person umsteigen?', ['Ja', 'Nein'], 0, '„Yes, change at Central Station.“'),
        q('Auf welchem Gleis?', ['Vier', 'Zwei'], 0, '„Platform four“ = Gleis vier.'),
      ],
      [
        chunk('Do I have to change?', 'Muss ich umsteigen?', 'have_to_change', 'Do I have to ___?', 'Muss ich ___?', 'change', 'have to = müssen.'),
        chunk('Which platform?', 'Welches Gleis?', 'which_platform', 'Which platform?', 'Welches Gleis?', '', 'which = welches.'),
      ],
      { promptDe: 'Frage, ob du umsteigen musst.', model: 'Do I have to change?', expect: ['change'] },
      q('Was heißt „I have to change“?', ['Ich muss umsteigen', 'Ich möchte umsteigen'], 0, 'have to = müssen.'),
      'must = müssen (Pflicht), have to = müssen (Umstand), should = sollten (Rat).'),
  ]),

  S('telefon', 'alltag', 'Telefon & Termin', 'Am Telefon einen Termin machen', 'Du rufst in der Arztpraxis an.', 'phone', [
    L('Ich möchte einen Termin', 'Ich kann einen Termin vereinbaren.', 'modals_politeness', 2,
      [
        ['reception', 'Good morning, Doctor Meyer’s practice.', 'Guten Morgen, Praxis Doktor Meyer.'],
        ['you', 'Hello. I’d like to make an appointment, please.', 'Hallo. Ich möchte einen Termin machen, bitte.'],
        ['reception', 'When would you like to come?', 'Wann möchten Sie kommen?'],
        ['you', 'As soon as possible, please.', 'So bald wie möglich, bitte.'],
      ],
      [
        q('Warum ruft die Person an?', ['Für einen Termin', 'Für ein Rezept'], 0, '„make an appointment“ = einen Termin machen.'),
        q('Wann möchte sie kommen?', ['So bald wie möglich', 'Nächsten Monat'], 0, '„as soon as possible“ = so bald wie möglich.'),
      ],
      [
        chunk('I’d like to make an appointment.', 'Ich möchte einen Termin machen.', 'id_like_to', 'I’d like to ___', 'Ich möchte ___', 'make an appointment', 'I’d like to + Grundform.'),
        chunk('As soon as possible, please.', 'So bald wie möglich, bitte.', 'asap', 'As soon as possible.', 'So bald wie möglich.', '', 'Feste Wendung am Telefon.'),
      ],
      { promptDe: 'Die Praxis fragt: „When would you like to come?“', model: 'As soon as possible, please.', expect: ['soon'] },
      q('Was heißt „appointment“?', ['Termin', 'Apotheke'], 0, 'appointment = Termin.'),
      'Am Telefon: erst grüßen, dann kurz sagen, warum du anrufst.'),
    L('Wann passt es?', 'Ich kann Uhrzeiten und Tage verstehen.', 'prepositions_space', 2,
      [
        ['reception', 'Can you come on Tuesday at ten?', 'Können Sie Dienstag um zehn kommen?'],
        ['you', 'I’m sorry, I can’t. I have another appointment.', 'Tut mir leid, ich kann nicht. Ich habe einen anderen Termin.'],
        ['reception', 'How about Thursday at three?', 'Wie wäre es Donnerstag um drei?'],
        ['you', 'That works for me. Thank you.', 'Das passt mir. Danke.'],
      ],
      [
        q('Warum kann die Person Dienstag nicht?', ['Sie hat einen anderen Termin', 'Sie ist krank'], 0, '„another appointment“ = ein anderer Termin.'),
        q('Wann ist der neue Termin?', ['Donnerstag um drei', 'Dienstag um zehn'], 0, '„Thursday at three“.'),
      ],
      [
        chunk('Can you come on Tuesday at ten?', 'Können Sie Dienstag um zehn kommen?', 'on_day_at_time', 'Can you come on ___ at ___?', 'Können Sie ___ um ___ kommen?', 'Tuesday at ten', 'on + Tag, at + Uhrzeit.'),
        chunk('That works for me.', 'Das passt mir.', 'that_works', 'That works for me.', 'Das passt mir.', '', 'Natürliche Zustimmung.'),
      ],
      { promptDe: 'Die Praxis fragt: „Can you come on Tuesday at ten?“ Du kannst nicht.', model: 'I’m sorry, I can’t. I have another appointment.', expect: ['can’t'] },
      q('Welche Präposition passt?', ['on Tuesday', 'in Tuesday'], 0, 'on + Wochentag.'),
      'Zeit-Präpositionen: at + Uhrzeit, on + Tag, in + Monat/Jahr.'),
    L('Absagen', 'Ich kann einen Termin absagen.', 'modals_politeness', 2,
      [
        ['you', 'I’m sorry, I have to cancel my appointment.', 'Tut mir leid, ich muss meinen Termin absagen.'],
        ['reception', 'That’s fine. Would you like a new one?', 'Das ist in Ordnung. Möchten Sie einen neuen?'],
        ['you', 'Yes, please. Next week, if possible.', 'Ja, bitte. Nächste Woche, wenn möglich.'],
        ['reception', 'I’ll call you back.', 'Ich rufe Sie zurück.'],
      ],
      [
        q('Was möchte die Person?', ['Absagen', 'Bezahlen'], 0, '„cancel“ = absagen.'),
        q('Was schlägt sie vor?', ['Nächste Woche', 'Nächsten Monat'], 0, '„next week“ = nächste Woche.'),
      ],
      [
        chunk('I have to cancel my appointment.', 'Ich muss meinen Termin absagen.', 'have_to', 'I have to ___', 'Ich muss ___', 'cancel my appointment', 'cancel = absagen.'),
        chunk('Next week, if possible.', 'Nächste Woche, wenn möglich.', 'if_possible', '___ , if possible.', '___ , wenn möglich.', 'Next week', 'if possible = wenn möglich.'),
      ],
      { promptDe: 'Sage den Termin ab.', model: 'I’m sorry, I have to cancel my appointment.', expect: ['cancel'] },
      q('Was heißt „cancel“?', ['absagen', 'verschieben'], 0, 'cancel = absagen.'),
      'Absagen: kurz entschuldigen, Grund nennen (oder nicht), neuen Termin anbieten.'),
  ]),

  S('hilfe', 'alltag', 'Um Hilfe bitten', 'Hilfe holen und danken', 'Du brauchst Hilfe im Alltag.', 'help', [
    L('Im Hausflur', 'Ich kann um Hilfe bitten.', 'modals_politeness', 2,
      [
        ['you', 'Excuse me, could you help me with this box?', 'Entschuldigung, könnten Sie mir mit dieser Kiste helfen?'],
        ['neighbour', 'Of course. It looks heavy.', 'Natürlich. Sie sieht schwer aus.'],
        ['you', 'Thank you so much. That’s very kind.', 'Vielen Dank. Das ist sehr freundlich.'],
        ['neighbour', 'You’re welcome.', 'Gern geschehen.'],
      ],
      [
        q('Worum bittet die Person?', ['Um Hilfe mit einer Kiste', 'Um Geld'], 0, '„help me with this box“.'),
        q('Wie reagiert der Nachbar?', ['Hilfsbereit', 'Genervt'], 0, '„Of course.“ = Natürlich.'),
      ],
      [
        chunk('Could you help me with this box?', 'Könnten Sie mir mit dieser Kiste helfen?', 'help_with', 'Could you help me with ___?', 'Könnten Sie mir mit ___ helfen?', 'this box', 'help someone with something.'),
        chunk('That’s very kind.', 'Das ist sehr freundlich.', 'very_kind', 'That’s very kind.', 'Das ist sehr freundlich.', '', 'Dank mit Wärme.'),
      ],
      { promptDe: 'Bitte um Hilfe mit einer Kiste.', model: 'Could you help me with this box?', expect: ['help'] },
      q('Welche Präposition passt?', ['help me with the box', 'help me for the box'], 0, 'help someone with something.'),
      'Hilfe bitten: „Could you …?“ + konkrete Sache. Konkret ist höflicher als „Kannst du helfen?“'),
    L('Etwas ausleihen', 'Ich kann fragen, ob ich etwas bekomme.', 'modals_politeness', 2,
      [
        ['you', 'Do you have a ladder I could borrow?', 'Haben Sie eine Leiter, die ich mir leihen könnte?'],
        ['neighbour', 'Yes, I’ll get it for you.', 'Ja, ich hole sie Ihnen.'],
        ['you', 'I’ll bring it back tomorrow.', 'Ich bringe sie morgen zurück.'],
        ['neighbour', 'No rush.', 'Keine Eile.'],
      ],
      [
        q('Was möchte die Person leihen?', ['Eine Leiter', 'Einen Hammer'], 0, '„a ladder“ = eine Leiter.'),
        q('Wann bringt sie es zurück?', ['Morgen', 'Nächste Woche'], 0, '„tomorrow“ = morgen.'),
      ],
      [
        chunk('Do you have a ladder I could borrow?', 'Haben Sie eine Leiter, die ich mir leihen könnte?', 'do_you_have', 'Do you have ___?', 'Haben Sie ___?', 'a ladder', 'do you have = haben Sie.'),
        chunk('I’ll bring it back tomorrow.', 'Ich bringe es morgen zurück.', 'bring_back', 'I’ll bring it back ___', 'Ich bringe es ___ zurück', 'tomorrow', 'bring back = zurückbringen.'),
      ],
      { promptDe: 'Frage nach einer Leiter.', model: 'Do you have a ladder I could borrow?', expect: ['ladder'] },
      q('Was heißt „borrow“?', ['leihen (nehmen)', 'kaufen'], 0, 'borrow = sich leihen; lend = verleihen.'),
      'borrow = ich leihe mir; lend = ich verleihe.'),
    L('Danke sagen', 'Ich kann mich bedanken und Hilfe anbieten.', 'function_cards', 2,
      [
        ['you', 'Thank you for your help. I really appreciate it.', 'Danke für Ihre Hilfe. Ich weiß das sehr zu schätzen.'],
        ['neighbour', 'Don’t mention it. If you need anything, just knock.', 'Gern. Wenn Sie etwas brauchen, klopfen Sie einfach.'],
        ['you', 'I will. Have a nice day!', 'Mache ich. Einen schönen Tag!'],
        ['neighbour', 'You too!', 'Ihnen auch!'],
      ],
      [
        q('Wofür bedankt sich die Person?', ['Für Hilfe', 'Für ein Geschenk'], 0, '„Thank you for your help.“'),
        q('Was bietet der Nachbar an?', ['Weitere Hilfe', 'Ein Auto'], 0, '„If you need anything, just knock.“'),
      ],
      [
        chunk('Thank you for your help.', 'Danke für Ihre Hilfe.', 'thanks_for', 'Thank you for ___', 'Danke für ___', 'your help', 'thank you for = danke für.'),
        chunk('Have a nice day!', 'Einen schönen Tag!', 'nice_day', 'Have a nice day!', 'Einen schönen Tag!', '', 'Standardverabschiedung.'),
      ],
      { promptDe: 'Bedanke dich für die Hilfe.', model: 'Thank you for your help.', expect: ['thank'] },
      q('Welche Präposition passt?', ['Thank you for your help.', 'Thank you about your help.'], 0, 'thank you for = danke für.'),
      'Danken + konkreter Grund wirkt wärmer als ein knappes „Thanks“.'),
  ]),

  S('langsam', 'hakten', 'Langsamer, bitte', 'Sagen, dass du nicht verstehst', 'Du verstehst zu schnell gesprochenes Englisch nicht.', 'slow', [
    L('Ich verstehe nicht', 'Ich kann sagen, dass ich nicht verstehe.', 'function_cards', 2,
      [
        ['clerk', 'Sorry, we’re closing in five minutes.', 'Entschuldigung, wir schließen in fünf Minuten.'],
        ['you', 'Sorry, I don’t understand. Could you speak more slowly?', 'Entschuldigung, ich verstehe nicht. Könnten Sie langsamer sprechen?'],
        ['clerk', 'Of course. We close in five minutes.', 'Natürlich. Wir schließen in fünf Minuten.'],
        ['you', 'Thank you. Now I understand.', 'Danke. Jetzt verstehe ich.'],
      ],
      [
        q('Was sagt die Person, als sie nicht versteht?', ['Sie bittet um langsameres Sprechen', 'Sie geht'], 0, '„Could you speak more slowly?“'),
        q('Wann schließt der Laden?', ['In fünf Minuten', 'In einer Stunde'], 0, '„in five minutes“.'),
      ],
      [
        chunk('Could you speak more slowly?', 'Könnten Sie langsamer sprechen?', 'speak_slowly', 'Could you speak more ___?', 'Könnten Sie ___ sprechen?', 'slowly', 'more slowly = langsamer.'),
        chunk('Sorry, I don’t understand.', 'Entschuldigung, ich verstehe nicht.', 'dont_understand', 'Sorry, I don’t understand.', 'Entschuldigung, ich verstehe nicht.', '', 'Ehrlich und höflich.'),
      ],
      { promptDe: 'Bitte um langsameres Sprechen.', model: 'Could you speak more slowly, please?', expect: ['slowly'] },
      q('Was ist die höfliche Bitte?', ['Could you speak more slowly?', 'Speak slowly!'], 0, 'could + please macht die Bitte höflich.'),
      'Drei Rettungsphrasen: „Sorry?“, „Could you say that again?“, „Could you speak more slowly?“'),
    L('Buchstabieren & wiederholen', 'Ich kann um Wiederholung und Buchstabieren bitten.', 'function_cards', 2,
      [
        ['you', 'Could you repeat the name, please?', 'Könnten Sie den Namen wiederholen, bitte?'],
        ['clerk', 'Yes, it’s Miller. M-I-L-L-E-R.', 'Ja, Miller. M-I-L-L-E-R.'],
        ['you', 'Thank you. And how do you spell that?', 'Danke. Und wie schreibt man das?'],
        ['clerk', 'M-I-L-L-E-R.', 'M-I-L-L-E-R.'],
      ],
      [
        q('Worum bittet die Person?', ['Um Wiederholung', 'Um ein Formular'], 0, '„Could you repeat …?“'),
        q('Wie buchstabiert die Person den Namen?', ['M-I-L-L-E-R', 'M-U-E-L-L-E-R'], 0, 'Buchstabe für Buchstabe.'),
      ],
      [
        chunk('Could you repeat that, please?', 'Könnten Sie das wiederholen, bitte?', 'could_repeat', 'Could you repeat ___ , please?', 'Könnten Sie ___ wiederholen, bitte?', 'that', 'repeat = wiederholen.'),
        chunk('How do you spell that?', 'Wie schreibt man das?', 'how_spell', 'How do you spell ___?', 'Wie schreibt man ___?', 'that', 'spell = buchstabieren.'),
      ],
      { promptDe: 'Bitte um Wiederholung.', model: 'Could you repeat that, please?', expect: ['repeat'] },
      q('Was heißt „spell“?', ['buchstabieren', 'aussprechen'], 0, 'spell = buchstabieren; pronounce = aussprechen.'),
      'Namen und Straßen buchstabiert man – das ist normal, nicht peinlich.'),
    L('Zeit gewinnen', 'Ich kann um Zeit bitten und nachfragen.', 'function_cards', 2,
      [
        ['doctor', 'Do you have any allergies?', 'Haben Sie Allergien?'],
        ['you', 'One moment, please. How do you say … in English?', 'Einen Moment, bitte. Wie sagt man … auf Englisch?'],
        ['doctor', 'Take your time.', 'Lassen Sie sich Zeit.'],
        ['you', 'I’m not sure. I’ll write it down.', 'Ich bin nicht sicher. Ich schreibe es auf.'],
      ],
      [
        q('Was macht die Person, wenn sie ein Wort nicht kennt?', ['Sie fragt nach', 'Sie schweigt'], 0, '„How do you say … in English?“'),
        q('Was macht sie dann?', ['Sie schreibt es auf', 'Sie geht'], 0, '„I’ll write it down.“'),
      ],
      [
        chunk('How do you say … in English?', 'Wie sagt man … auf Englisch?', 'how_say', 'How do you say ___ in English?', 'Wie sagt man ___ auf Englisch?', '…', 'Die Lücke füllst du mit dem deutschen Wort.'),
        chunk('One moment, please.', 'Einen Moment, bitte.', 'one_moment', 'One moment, please.', 'Einen Moment, bitte.', '', 'Zeit gewinnen, ohne abzubrechen.'),
      ],
      { promptDe: 'Du kennst ein Wort nicht. Frage nach.', model: 'How do you say … in English?', expect: ['say', 'english'] },
      q('Welche Strategie hilft, wenn ein Wort fehlt?', ['How do you say … in English?', 'I don’t know.'], 0, 'Nachfragen hält das Gespräch am Leben.'),
      'Strategien sind kein Notbehelf – sie sind Teil echter Sprachfähigkeit.'),
  ]),

  S('missverstaendnis', 'hakten', 'Missverständnisse klären', 'Sich verständlich machen', 'Ein Gespräch ist schiefgelaufen.', 'repair', [
    L('Das habe ich nicht gemeint', 'Ich kann ein Missverständnis klären.', 'function_cards', 2,
      [
        ['friend', 'So you don’t want to come?', 'Du willst also nicht kommen?'],
        ['you', 'No, that’s not what I meant. I’d love to come.', 'Nein, das habe ich nicht gemeint. Ich komme sehr gern.'],
        ['friend', 'Great! I misunderstood.', 'Super! Ich habe es falsch verstanden.'],
        ['you', 'No problem. Let’s meet at six.', 'Kein Problem. Treffen wir uns um sechs.'],
      ],
      [
        q('Was wollte die Person sagen?', ['Sie kommt gern', 'Sie kommt nicht'], 0, '„I’d love to come.“'),
        q('Wann treffen sie sich?', ['Um sechs', 'Um sieben'], 0, '„at six“ = um sechs.'),
      ],
      [
        chunk('That’s not what I meant.', 'Das habe ich nicht gemeint.', 'not_what_i_meant', 'That’s not what I meant.', 'Das habe ich nicht gemeint.', '', 'mean = meinen/bedeuten.'),
        chunk('I’d love to come.', 'Ich komme sehr gern.', 'id_love_to', 'I’d love to ___', 'Ich ___ sehr gern', 'come', 'I’d love to = ich würde sehr gern.'),
      ],
      { promptDe: 'Kläre: Du willst doch kommen.', model: 'No, that’s not what I meant. I’d love to come.', expect: ['meant'] },
      q('Was heißt „I’d love to“?', ['Ich würde sehr gern', 'Ich muss leider'], 0, 'I’d love to = ich würde sehr gern.'),
      'Missverständnisse klärt man mit „That’s not what I meant“ – nicht mit Schweigen.'),
    L('Sag es anders', 'Ich kann mich mit anderen Worten ausdrücken.', 'function_cards', 2,
      [
        ['friend', 'Which one do you mean?', 'Welchen meinst du?'],
        ['you', 'The small one, next to the window.', 'Den kleinen, neben dem Fenster.'],
        ['friend', 'Ah, the blue one?', 'Ach, den blauen?'],
        ['you', 'Yes, exactly. That’s the one.', 'Ja, genau. Der ist es.'],
      ],
      [
        q('Welchen Gegenstand meint die Person?', ['Den kleinen neben dem Fenster', 'Den großen an der Tür'], 0, '„the small one, next to the window“.'),
        q('Welche Farbe hat er?', ['Blau', 'Rot'], 0, '„the blue one“.'),
      ],
      [
        chunk('The small one, next to the window.', 'Der kleine, neben dem Fenster.', 'the_one', 'The ___ one', 'Der/die/das ___', 'small', 'one ersetzt das Nomen.'),
        chunk('Yes, exactly.', 'Ja, genau.', 'exactly', 'Yes, exactly.', 'Ja, genau.', '', 'Zustimmung mit Präzision.'),
      ],
      { promptDe: 'Beschreibe den kleinen Gegenstand neben dem Fenster.', model: 'The small one, next to the window.', expect: ['small', 'window'] },
      q('Was macht „one“ in „the small one“?', ['Es ersetzt das Nomen.', 'Es ist eine Zahl.'], 0, 'one = Stellvertreter für das Nomen.'),
      'Umschreiben ist eine Kernfähigkeit: Eigenschaft + Ort + Farbe statt eines fehlenden Wortes.'),
    L('Freundlich widersprechen', 'Ich kann höflich widersprechen.', 'function_cards', 2,
      [
        ['friend', 'I think it’s too expensive.', 'Ich finde, es ist zu teuer.'],
        ['you', 'I see your point, but it’s very good quality.', 'Ich verstehe, aber es ist sehr gute Qualität.'],
        ['friend', 'Maybe you’re right.', 'Vielleicht hast du recht.'],
        ['you', 'Let’s think about it.', 'Denken wir darüber nach.'],
      ],
      [
        q('Was sagt die Person zum Preis?', ['Sie versteht den Einwand, findet es aber gut', 'Sie findet es billig'], 0, '„I see your point, but …“.'),
        q('Was schlagen sie am Ende vor?', ['Darüber nachdenken', 'Sofort kaufen'], 0, '„Let’s think about it.“'),
      ],
      [
        chunk('I see your point, but …', 'Ich verstehe, aber …', 'see_your_point', 'I see your point, but ___', 'Ich verstehe, aber ___', 'it’s very good quality', 'Erst anerkennen, dann widersprechen.'),
        chunk('Maybe you’re right.', 'Vielleicht hast du recht.', 'maybe_right', 'Maybe you’re right.', 'Vielleicht hast du recht.', '', 'Weiches Nachgeben.'),
      ],
      { promptDe: 'Widersprich freundlich: Es ist gute Qualität.', model: 'I see your point, but it’s very good quality.', expect: ['point', 'quality'] },
      q('Welche Form ist höflich?', ['I see your point, but …', 'You are wrong.'], 0, 'Erst anerkennen, dann widersprechen.'),
      'Widerspruch ohne Gesichtsverlust: anerkennen, dann die eigene Sicht.'),
  ]),
];
