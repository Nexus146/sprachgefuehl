import { S, L, q, chunk } from './lib.mjs';

/* Staffel 2 – Winter und Frühling. Dieselben Figuren, dieselben Bausteine, neue Gelegenheiten. */

export const situations4 = [
  S('post', 'alltag', 'Auf der Post', 'Paket, Brief, Versand', 'Ein Paket muss zurück – Frau Peters kennt den Weg.', 'help', [
    L('Ein Paket zurück', 'Ich kann höflich sagen, was ich brauche.', 'conditionals_would_like', 3,
      [
        ['you', 'I’d like to send this parcel back.', 'Ich möchte dieses Paket zurückschicken.'],
        ['peters', 'Do you have the receipt?', 'Haben Sie den Beleg?'],
        ['you', 'Yes, it’s in the box.', 'Ja, er ist in der Schachtel.'],
        ['peters', 'Then please fill in this label.', 'Dann füllen Sie bitte dieses Etikett aus.'],
      ],
      [
        q('Was möchte die Person?', ['Ein Paket zurückschicken', 'Ein Paket abholen'], 0, '„send back“ = zurückschicken.'),
        q('Wo ist der Beleg?', ['In der Schachtel', 'In der Tasche'], 0, '„in the box“ = in der Schachtel.'),
      ],
      [
        chunk('I’d like to send this parcel back.', 'Ich möchte dieses Paket zurückschicken.', 'send_parcel_back', 'I’d like to send ___ back', 'Ich möchte ___ zurückschicken', 'this parcel', 'parcel = Paket.'),
        chunk('Do you have the receipt?', 'Haben Sie den Beleg?', 'receipt', 'Do you have ___?', 'Haben Sie ___?', 'the receipt', 'receipt = Beleg, Quittung.'),
      ],
      { promptDe: 'Sag höflich, dass du das Paket zurückschicken möchtest.', model: 'I’d like to send this parcel back.', expect: ['parcel'] },
      q('Welcher Satz ist höflich?', ['I’d like to send this parcel back.', 'I send this parcel back.'], 0, 'would like = möchte.'),
      'would like to + Grundform ist die höfliche Form; bei Behörden und Post besonders wichtig.',
      ['apotheke_l1_c2', 'supermarkt_l1_c1']),

    L('Was kostet der Versand?', 'Ich kann nach Preisen und Zahlungsarten fragen.', 'word_order_question', 2,
      [
        ['you', 'How much does it cost?', 'Wie viel kostet es?'],
        ['peters', 'Four euros fifty.', 'Vier Euro fünfzig.'],
        ['you', 'Can I pay by card?', 'Kann ich mit Karte zahlen?'],
        ['peters', 'Yes, at the counter.', 'Ja, am Schalter.'],
      ],
      [
        q('Was kostet der Versand?', ['Vier Euro fünfzig', 'Fünf Euro'], 0, '„four euros fifty“ = vier Euro fünfzig.'),
        q('Wie kann sie zahlen?', ['Mit Karte', 'Nur bar'], 0, '„pay by card“ = mit Karte zahlen.'),
      ],
      [
        chunk('How much does it cost?', 'Wie viel kostet es?', 'how_much_cost', 'How much does it ___?', 'Wie viel ___ es?', 'cost', 'does + Grundform in der Frage.'),
        chunk('Can I pay by card?', 'Kann ich mit Karte zahlen?', 'pay_by_card', 'Can I pay by ___?', 'Kann ich mit ___ zahlen?', 'card', 'by card = mit Karte.'),
      ],
      { promptDe: 'Frag, ob du mit Karte zahlen kannst.', model: 'Can I pay by card?', expect: ['pay', 'card'] },
      q('Welche Frage ist richtig?', ['How much does it cost?', 'How much it costs?'], 0, 'does + Grundform.'),
      'Preise: How much does it cost? – bei mehreren Dingen: How much do they cost?',
      ['supermarkt_l2_c1', 'cafe_l3_c2']),

    L('Der Brief ist da', 'Ich kann über Vergangenes berichten.', 'past_simple', 2,
      [
        ['peters', 'The letter arrived yesterday.', 'Der Brief ist gestern angekommen.'],
        ['you', 'That was quick.', 'Das ging schnell.'],
        ['peters', 'Yes, the post is good here.', 'Ja, die Post hier ist gut.'],
        ['you', 'Good to know. Thank you.', 'Gut zu wissen. Danke.'],
      ],
      [
        q('Wann kam der Brief?', ['Gestern', 'Heute'], 0, '„yesterday“ = gestern.'),
        q('Wie ist die Post hier?', ['Gut', 'Langsam'], 0, '„the post is good here“ = die Post ist gut.'),
      ],
      [
        chunk('The letter arrived yesterday.', 'Der Brief ist gestern angekommen.', 'letter_arrived', 'The letter arrived ___', 'Der Brief ist ___ angekommen', 'yesterday', 'arrived = kam an (Vergangenheit).'),
        chunk('That was quick.', 'Das ging schnell.', 'that_was_quick', 'That was ___', 'Das war ___', 'quick', 'was = Vergangenheit von is.'),
      ],
      { promptDe: 'Sag, dass der Brief gestern angekommen ist.', model: 'The letter arrived yesterday.', expect: ['letter', 'yesterday'] },
      q('Welche Form ist Vergangenheit?', ['arrived', 'arrives'], 0, 'Regelmäßige Verben: -ed.'),
      'Zeitangaben wie yesterday, last week, two days ago ziehen die Vergangenheit nach sich.',
      ['bus_l2_c2', 'nachbarn_l1_c1']),
  ]),

  S('frueher', 'leute', 'Über früher erzählen', 'Wie das Viertel war', 'Herr Krüger erzählt, wie das Viertel früher war.', 'people', [
    L('Wie war es früher?', 'Ich kann über die Vergangenheit sprechen.', 'past_simple', 3,
      [
        ['krueger', 'There was a bakery on this corner.', 'An dieser Ecke war eine Bäckerei.'],
        ['you', 'Really? When was that?', 'Wirklich? Wann war das?'],
        ['krueger', 'About twenty years ago.', 'Etwa vor zwanzig Jahren.'],
        ['you', 'I didn’t know that.', 'Das wusste ich nicht.'],
      ],
      [
        q('Was war an der Ecke?', ['Eine Bäckerei', 'Eine Bank'], 0, '„bakery“ = Bäckerei.'),
        q('Wann war das?', ['Vor etwa zwanzig Jahren', 'Letztes Jahr'], 0, '„twenty years ago“ = vor zwanzig Jahren.'),
      ],
      [
        chunk('There was a bakery on this corner.', 'An dieser Ecke war eine Bäckerei.', 'there_was_bakery', 'There was ___ on this corner', 'An dieser Ecke war ___', 'a bakery', 'there was = es gab (Einzahl).'),
        chunk('I didn’t know that.', 'Das wusste ich nicht.', 'didnt_know', 'I didn’t ___', 'Ich ___ nicht', 'know', 'didn’t + Grundform.'),
      ],
      { promptDe: 'Sag, dass du das nicht wusstest.', model: 'I didn’t know that.', expect: ['know'] },
      q('Welche Vergangenheit passt zu „a bakery“?', ['was', 'were'], 0, 'Einzahl → was.'),
      'there was / there were: Einzahl was, Mehrzahl were.',
      ['weg_l2_c1', 'nachbarn_l1_c2']),

    L('Früher und heute', 'Ich kann Dinge vergleichen.', 'comparison', 2,
      [
        ['krueger', 'The street is busier than before.', 'Die Straße ist belebter als früher.'],
        ['you', 'But the park is nicer now.', 'Aber der Park ist jetzt schöner.'],
        ['krueger', 'That’s true.', 'Das stimmt.'],
        ['you', 'I like it here.', 'Ich mag es hier.'],
      ],
      [
        q('Wie ist die Straße jetzt?', ['Belebter', 'Ruhiger'], 0, '„busier than before“ = belebter als früher.'),
        q('Wie findet die Person den Park?', ['Schöner', 'Kleiner'], 0, '„nicer now“ = jetzt schöner.'),
      ],
      [
        chunk('The street is busier than before.', 'Die Straße ist belebter als früher.', 'busier_than', 'The street is ___ than before', 'Die Straße ist ___ als früher', 'busier', 'busy → busier (y wird zu i).'),
        chunk('But the park is nicer now.', 'Aber der Park ist jetzt schöner.', 'nicer_now', 'The park is ___ now', 'Der Park ist jetzt ___', 'nicer', 'nice → nicer.'),
      ],
      { promptDe: 'Sag, dass die Straße belebter ist als früher.', model: 'The street is busier than before.', expect: ['busier', 'than'] },
      q('Wie lautet der Komparativ von busy?', ['busier', 'more busy'], 0, 'Kurze Adjektive auf -y: y wird zu i + er.'),
      'Steigerung: busier, nicer, better – und immer than für den Vergleich.',
      ['nachbarn_l2_c1', 'supermarkt_l2_c1']),

    L('Bleiben oder gehen?', 'Ich kann Wünsche und Bedingungen ausdrücken.', 'conditionals_would_like', 4,
      [
        ['krueger', 'If I moved away, I would miss this street.', 'Wenn ich wegzöge, würde ich diese Straße vermissen.'],
        ['you', 'I would miss it too.', 'Ich würde sie auch vermissen.'],
        ['krueger', 'Then we stay.', 'Dann bleiben wir.'],
        ['you', 'Good plan.', 'Guter Plan.'],
      ],
      [
        q('Was würde Herr Krüger vermissen?', ['Die Straße', 'Die Bäckerei'], 0, '„miss this street“ = diese Straße vermissen.'),
        q('Was beschließen sie?', ['Zu bleiben', 'Umzuziehen'], 0, '„Then we stay“ = dann bleiben wir.'),
      ],
      [
        chunk('If I moved away, I would miss this street.', 'Wenn ich wegzöge, würde ich diese Straße vermissen.', 'if_moved_miss', 'If I moved away, I would ___', 'Wenn ich wegzöge, würde ich ___', 'miss this street', 'If + Vergangenheit, would + Grundform.'),
        chunk('I would miss it too.', 'Ich würde sie auch vermissen.', 'would_miss', 'I would ___ too', 'Ich würde auch ___', 'miss it', 'would + Grundform für Wünsche.'),
      ],
      { promptDe: 'Sag, dass du die Straße auch vermissen würdest.', model: 'I would miss it too.', expect: ['miss'] },
      q('Welcher Satz ist eine Bedingung?', ['If I moved away, I would miss this street.', 'I move away and miss the street.'], 0, 'if + would beschreibt eine gedachte Bedingung.'),
      'Höfliche Wehmut: If + simple past, would + Grundform. Kein „would“ im if-Teil.',
      ['nachbarn_l3_c1', 'hilfe_l3_c1']),
  ]),

  S('fotos', 'leute', 'Fotos zeigen', 'Erinnerungen teilen', 'Lena und Hana schauen mit dir alte Fotos an.', 'family', [
    L('Das ist meine Familie', 'Ich kann Menschen vorstellen und beschreiben.', 'be_have', 1,
      [
        ['lena', 'This is my grandmother in 1978.', 'Das ist meine Großmutter 1978.'],
        ['you', 'She looks like you.', 'Sie sieht dir ähnlich.'],
        ['lena', 'Everyone says that.', 'Das sagen alle.'],
        ['you', 'And who is the man next to her?', 'Und wer ist der Mann neben ihr?'],
      ],
      [
        q('Wen zeigt Lena?', ['Ihre Großmutter', 'Ihre Mutter'], 0, '„grandmother“ = Großmutter.'),
        q('Wem sieht sie ähnlich?', ['Lena', 'Hana'], 0, '„She looks like you“ = sie sieht dir ähnlich.'),
      ],
      [
        chunk('She looks like you.', 'Sie sieht dir ähnlich.', 'looks_like', 'She looks like ___', 'Sie sieht ___ ähnlich', 'you', 'look like = ähnlich sehen, nicht „look as“.'),
        chunk('Who is the man next to her?', 'Wer ist der Mann neben ihr?', 'who_is_man', 'Who is the ___ next to her?', 'Wer ist ___ neben ihr?', 'man', 'who is = wer ist.'),
      ],
      { promptDe: 'Sag, dass sie dir ähnlich sieht.', model: 'She looks like you.', expect: ['looks', 'like'] },
      q('Which is correct?', ['look like', 'look as'], 0, 'look like = ähnlich sehen.'),
      'look like für Ähnlichkeit, look for für Suchen, look after für Aufpassen.',
      ['familie_l3_c1', 'familie_l3_c2']),

    L('Wo war das?', 'Ich kann nach Orten und Zeitpunkten fragen.', 'past_simple', 2,
      [
        ['hana', 'Where was this photo taken?', 'Wo wurde dieses Foto aufgenommen?'],
        ['you', 'In the old house, I think.', 'Im alten Haus, glaube ich.'],
        ['hana', 'Was that before the war?', 'War das vor dem Krieg?'],
        ['you', 'Yes, long before.', 'Ja, lange davor.'],
      ],
      [
        q('Wo wurde das Foto aufgenommen?', ['Im alten Haus', 'Im Garten'], 0, '„in the old house“ = im alten Haus.'),
        q('Wann war das?', ['Lange vor dem Krieg', 'Danach'], 0, '„long before“ = lange davor.'),
      ],
      [
        chunk('Where was this photo taken?', 'Wo wurde dieses Foto aufgenommen?', 'photo_taken', 'Where was this photo ___?', 'Wo wurde dieses Foto ___?', 'taken', 'Passiv: was + taken.'),
        chunk('It was in the old house.', 'Es war im alten Haus.', 'old_house', 'It was in ___', 'Es war in ___', 'the old house', 'in + Gebäude.'),
      ],
      { promptDe: 'Frag, wo das Foto aufgenommen wurde.', model: 'Where was this photo taken?', expect: ['photo', 'taken'] },
      q('Welche Frage ist richtig?', ['Where was this photo taken?', 'Where this photo was taken?'], 0, 'Fragewort zuerst, dann was + Subjekt.'),
      'Passiv in der Vergangenheit: was/were + Partizip – was taken, were made.',
      ['weg_l1_c1', 'familie_l2_c1']),

    L('Erinnerungen', 'Ich kann beschreiben, was gerade passiert.', 'present_progressive', 5,
      [
        ['you', 'I’m looking at the old photos.', 'Ich schaue mir die alten Fotos an.'],
        ['lena', 'Are you crying?', 'Weinst du?'],
        ['you', 'A little. They’re beautiful.', 'Ein wenig. Sie sind wunderschön.'],
        ['lena', 'Let’s make new ones.', 'Lass uns neue machen.'],
      ],
      [
        q('Was macht die Person gerade?', ['Fotos anschauen', 'Fotos machen'], 0, '„I’m looking at“ = ich schaue an.'),
        q('Wie findet sie die Fotos?', ['Wunderschön', 'Langweilig'], 0, '„beautiful“ = wunderschön.'),
      ],
      [
        chunk('I’m looking at the old photos.', 'Ich schaue mir die alten Fotos an.', 'looking_at_photos', 'I’m looking at ___', 'Ich schaue mir ___ an', 'the old photos', 'am/is/are + -ing = gerade jetzt.'),
        chunk('They’re beautiful.', 'Sie sind wunderschön.', 'theyre_beautiful', 'They’re ___', 'Sie sind ___', 'beautiful', 'they’re = they are.'),
      ],
      { promptDe: 'Sag, dass du gerade die alten Fotos anschaust.', model: 'I’m looking at the old photos.', expect: ['looking', 'photos'] },
      q('Welcher Satz beschreibt „gerade jetzt“?', ['I’m looking', 'I look'], 0, 'am + -ing = gerade jetzt.'),
      'present progressive für den Moment; simple present für Gewohnheiten.',
      ['familie_l1_c1', 'nachbarn_l1_c1']),
  ]),

  S('zahnarzt', 'gesundheit', 'Beim Zahnarzt', 'Termin, Schmerzen, Nachsorge', 'Dr. Weber überweist dich weiter, und du machst den Termin selbst.', 'stethoscope', [
    L('Der Termin', 'Ich kann einen Termin machen und den Grund nennen.', 'modals_politeness', 1,
      [
        ['you', 'I’d like to make an appointment.', 'Ich möchte einen Termin machen.'],
        ['weber', 'Is it urgent?', 'Ist es dringend?'],
        ['you', 'I have a toothache.', 'Ich habe Zahnschmerzen.'],
        ['weber', 'Can you come tomorrow at nine?', 'Können Sie morgen um neun kommen?'],
      ],
      [
        q('Warum braucht die Person einen Termin?', ['Zahnschmerzen', 'Rückenschmerzen'], 0, '„toothache“ = Zahnschmerzen.'),
        q('Wann kann sie kommen?', ['Morgen um neun', 'Heute um fünf'], 0, '„tomorrow at nine“ = morgen um neun.'),
      ],
      [
        chunk('I’d like to make an appointment.', 'Ich möchte einen Termin machen.', 'make_appointment', 'I’d like to make ___', 'Ich möchte ___ machen', 'an appointment', 'appointment = Termin.'),
        chunk('I have a toothache.', 'Ich habe Zahnschmerzen.', 'toothache', 'I have ___', 'Ich habe ___', 'a toothache', 'toothache ist zählbar: a toothache.'),
      ],
      { promptDe: 'Sag, dass du Zahnschmerzen hast.', model: 'I have a toothache.', expect: ['toothache'] },
      q('Welcher Satz ist richtig?', ['I have a toothache.', 'I have toothache.'], 0, 'zählbar → a toothache.'),
      'Schmerzen: a headache, a toothache, a stomach ache – immer mit a.',
      ['telefon_l1_c1', 'arzt_l1_c1']),

    L('Es tut weh', 'Ich kann beschreiben, wann und wo es weh tut.', 'present_progressive', 2,
      [
        ['weber', 'Where does it hurt?', 'Wo tut es weh?'],
        ['you', 'It hurts when I’m eating.', 'Es tut weh, wenn ich esse.'],
        ['weber', 'Open your mouth, please.', 'Öffnen Sie bitte den Mund.'],
        ['you', 'Is it bad?', 'Ist es schlimm?'],
      ],
      [
        q('Wann tut es weh?', ['Beim Essen', 'Beim Gehen'], 0, '„when I’m eating“ = wenn ich esse.'),
        q('Was soll die Person tun?', ['Den Mund öffnen', 'Tief einatmen'], 0, '„open your mouth“ = den Mund öffnen.'),
      ],
      [
        chunk('It hurts when I’m eating.', 'Es tut weh, wenn ich esse.', 'hurts_eating', 'It hurts when I’m ___', 'Es tut weh, wenn ich ___', 'eating', 'when + present progressive für „während“.'),
        chunk('Open your mouth, please.', 'Öffnen Sie bitte den Mund.', 'open_mouth', 'Open your ___', 'Öffnen Sie ___', 'mouth', 'mouth = Mund.'),
      ],
      { promptDe: 'Sag, dass es beim Essen weh tut.', model: 'It hurts when I’m eating.', expect: ['hurts', 'eating'] },
      q('Welcher Satz ist richtig?', ['when I’m eating', 'when I eating'], 0, 'am/is/are + -ing.'),
      'Beim Arzt beschreibt man den Moment: It hurts when I’m eating / when I press here.',
      ['arzt_l1_c1', 'arzt_l2_c2']),

    L('Nach der Behandlung', 'Ich kann Anweisungen verstehen und versprechen, aufzupassen.', 'negation_short_answers', 3,
      [
        ['weber', 'You shouldn’t eat anything hard today.', 'Sie sollten heute nichts Hartes essen.'],
        ['you', 'Should I take a painkiller?', 'Soll ich ein Schmerzmittel nehmen?'],
        ['weber', 'Yes, if it hurts.', 'Ja, wenn es weh tut.'],
        ['you', 'Thank you. I’ll be careful.', 'Danke. Ich werde aufpassen.'],
      ],
      [
        q('Was soll die Person nicht essen?', ['Etwas Hartes', 'Etwas Warmes'], 0, '„anything hard“ = etwas Hartes.'),
        q('Was soll sie nehmen, wenn es weh tut?', ['Ein Schmerzmittel', 'Kaffee'], 0, '„painkiller“ = Schmerzmittel.'),
      ],
      [
        chunk('You shouldn’t eat anything hard today.', 'Sie sollten heute nichts Hartes essen.', 'shouldnt_eat_hard', 'You shouldn’t eat anything ___ today', 'Sie sollten heute nichts ___ essen', 'hard', 'shouldn’t = sollten nicht.'),
        chunk('I’ll be careful.', 'Ich werde aufpassen.', 'ill_be_careful', 'I’ll be ___', 'Ich werde ___', 'careful', 'careful = vorsichtig.'),
      ],
      { promptDe: 'Sag, dass du aufpassen wirst.', model: 'I’ll be careful.', expect: ['careful'] },
      q('Welche Verneinung ist richtig?', ['You shouldn’t eat anything hard.', 'You should don’t eat anything hard.'], 0, 'shouldn’t = should not.'),
      'Ratschläge: should / shouldn’t + Grundform. will für das eigene Versprechen.',
      ['arzt_l3_c1', 'arzt_l3_c2']),
  ]),

  S('krankenhaus', 'gesundheit', 'Im Krankenhaus', 'Überweisung und Untersuchung', 'Eine Untersuchung steht an – Ruth hat dir vorher Mut gemacht.', 'health', [
    L('Die Überweisung', 'Ich kann nach dem Weg und nach Nummern fragen.', 'modals_politeness', 2,
      [
        ['weber', 'I’ll write you a referral.', 'Ich schreibe Ihnen eine Überweisung.'],
        ['you', 'Do I need an appointment?', 'Brauche ich einen Termin?'],
        ['weber', 'Yes, call the hospital first.', 'Ja, rufen Sie zuerst im Krankenhaus an.'],
        ['you', 'Could you give me the number?', 'Könnten Sie mir die Nummer geben?'],
      ],
      [
        q('Was bekommt die Person?', ['Eine Überweisung', 'Ein Rezept'], 0, '„referral“ = Überweisung.'),
        q('Was soll sie zuerst tun?', ['Im Krankenhaus anrufen', 'Hinfahren'], 0, '„call the hospital first“ = zuerst anrufen.'),
      ],
      [
        chunk('I’ll write you a referral.', 'Ich schreibe Ihnen eine Überweisung.', 'write_referral', 'I’ll write you ___', 'Ich schreibe Ihnen ___', 'a referral', 'referral = Überweisung.'),
        chunk('Could you give me the number?', 'Könnten Sie mir die Nummer geben?', 'give_number', 'Could you give me ___?', 'Könnten Sie mir ___ geben?', 'the number', 'give me = mir geben.'),
      ],
      { promptDe: 'Bitte höflich um die Telefonnummer.', model: 'Could you give me the number?', expect: ['number'] },
      q('Welche Bitte ist höflich?', ['Could you give me the number?', 'Give me the number.'], 0, 'could + please ist höflich.'),
      'In Praxen und Ämtern: could you …? statt einer direkten Aufforderung.',
      ['telefon_l1_c2', 'arzt_l2_c2']),

    L('Im Wartezimmer', 'Ich kann Wege und Orte im Gebäude verstehen.', 'prepositions_space', 3,
      [
        ['nurse', 'Please sit in the waiting room.', 'Bitte setzen Sie sich ins Wartezimmer.'],
        ['you', 'Is it on this floor?', 'Ist es auf dieser Etage?'],
        ['nurse', 'Yes, at the end of the corridor.', 'Ja, am Ende des Flurs.'],
        ['you', 'Thank you.', 'Danke.'],
      ],
      [
        q('Wohin soll die Person?', ['Ins Wartezimmer', 'In die Kantine'], 0, '„waiting room“ = Wartezimmer.'),
        q('Wo ist es?', ['Am Ende des Flurs', 'Im Erdgeschoss'], 0, '„at the end of the corridor“ = am Ende des Flurs.'),
      ],
      [
        chunk('Please sit in the waiting room.', 'Bitte setzen Sie sich ins Wartezimmer.', 'waiting_room', 'Please sit in ___', 'Bitte setzen Sie sich in ___', 'the waiting room', 'in + Raum.'),
        chunk('At the end of the corridor.', 'Am Ende des Flurs.', 'end_corridor', 'At the end of ___', 'Am Ende ___', 'the corridor', 'at the end of = am Ende von.'),
      ],
      { promptDe: 'Sag, dass es am Ende des Flurs ist.', model: 'It’s at the end of the corridor.', expect: ['end', 'corridor'] },
      q('Welche Präposition passt?', ['at the end', 'in the end'], 0, 'at the end of = am Ende von.'),
      'in the end heißt „schließlich“ – at the end of the corridor ist der Ort.',
      ['weg_l1_c2', 'weg_l2_c1']),

    L('Wieder zu Hause', 'Ich kann von einer Untersuchung erzählen.', 'past_simple', 4,
      [
        ['ruth', 'How was the examination?', 'Wie war die Untersuchung?'],
        ['you', 'It was fine. Everyone was kind.', 'Sie war in Ordnung. Alle waren freundlich.'],
        ['ruth', 'I’m glad. Do you need anything?', 'Das freut mich. Brauchen Sie etwas?'],
        ['you', 'No, thank you. You helped a lot.', 'Nein, danke. Sie haben sehr geholfen.'],
      ],
      [
        q('Wie war die Untersuchung?', ['In Ordnung', 'Schlimm'], 0, '„It was fine“ = sie war in Ordnung.'),
        q('Wie waren die Menschen?', ['Freundlich', 'Gestresst'], 0, '„kind“ = freundlich.'),
      ],
      [
        chunk('Everyone was kind.', 'Alle waren freundlich.', 'everyone_kind', 'Everyone was ___', 'Alle waren ___', 'kind', 'kind = freundlich.'),
        chunk('You helped a lot.', 'Sie haben sehr geholfen.', 'helped_a_lot', 'You helped ___', 'Sie haben ___ geholfen', 'a lot', 'a lot = sehr, viel.'),
      ],
      { promptDe: 'Sag Ruth, dass sie sehr geholfen hat.', model: 'You helped a lot.', expect: ['helped'] },
      q('Wie lautet die Vergangenheit von help?', ['helped', 'helps'], 0, 'Regelmäßig: -ed.'),
      'Dank in der Vergangenheit: You helped a lot. – konkret und ehrlich.',
      ['apotheke_l3_c1', 'hilfe_l3_c1']),
  ]),

  S('reklamieren', 'hakten', 'Etwas reklamieren', 'Freundlich sagen, was nicht stimmt', 'Die Bestellung stimmt nicht, und du sagst es freundlich.', 'repair', [
    L('Das ist nicht meine Bestellung', 'Ich kann ruhig sagen, dass etwas nicht stimmt.', 'articles_quantity', 1,
      [
        ['you', 'Excuse me, this isn’t what I ordered.', 'Entschuldigung, das ist nicht, was ich bestellt habe.'],
        ['sam', 'Sorry, what did you order?', 'Entschuldigung, was haben Sie bestellt?'],
        ['you', 'A tea, not a coffee.', 'Einen Tee, keinen Kaffee.'],
        ['sam', 'Of course. I’ll change it.', 'Natürlich. Ich tausche es um.'],
      ],
      [
        q('Was stimmt nicht?', ['Die Bestellung', 'Die Rechnung'], 0, '„this isn’t what I ordered“ = das ist nicht meine Bestellung.'),
        q('Was hatte die Person bestellt?', ['Einen Tee', 'Einen Kaffee'], 0, '„A tea, not a coffee“ = Tee, nicht Kaffee.'),
      ],
      [
        chunk('This isn’t what I ordered.', 'Das ist nicht, was ich bestellt habe.', 'not_what_ordered', 'This isn’t what I ___', 'Das ist nicht, was ich ___', 'ordered', 'isn’t = is not.'),
        chunk('I’ll change it.', 'Ich tausche es um.', 'change_it', 'I’ll ___ it', 'Ich ___ es', 'change', 'change = ändern, tauschen.'),
      ],
      { promptDe: 'Sag ruhig, dass das nicht deine Bestellung ist.', model: 'This isn’t what I ordered.', expect: ['ordered'] },
      q('Welcher Satz ist richtig?', ['This isn’t what I ordered.', 'This is not’t what I ordered.'], 0, 'isn’t = is not.'),
      'Reklamation beginnt mit „Excuse me“ und beschreibt die Sache, nicht die Person.',
      ['cafe_l1_c1', 'cafe_l3_c1']),

    L('Freundlich bleiben', 'Ich kann Kritik höflich formulieren.', 'modals_politeness', 4,
      [
        ['sam', 'Is everything all right?', 'Ist alles in Ordnung?'],
        ['you', 'The soup is cold, I’m afraid.', 'Die Suppe ist kalt, fürchte ich.'],
        ['sam', 'I’m very sorry. I’ll bring a new one.', 'Das tut mir sehr leid. Ich bringe eine neue.'],
        ['you', 'Thank you. That’s kind of you.', 'Danke. Das ist nett von Ihnen.'],
      ],
      [
        q('Was ist das Problem?', ['Die Suppe ist kalt', 'Der Tisch ist zu klein'], 0, '„cold“ = kalt.'),
        q('Was macht Sam?', ['Bringt eine neue Suppe', 'Bringt die Rechnung'], 0, '„I’ll bring a new one“ = ich bringe eine neue.'),
      ],
      [
        chunk('The soup is cold, I’m afraid.', 'Die Suppe ist kalt, fürchte ich.', 'im_afraid', 'The soup is cold, ___', 'Die Suppe ist kalt, ___', 'I’m afraid', 'I’m afraid = leider, fürchte ich.'),
        chunk('That’s kind of you.', 'Das ist nett von Ihnen.', 'kind_of_you', 'That’s kind of ___', 'Das ist nett von ___', 'you', 'kind of you = nett von Ihnen.'),
      ],
      { promptDe: 'Sag höflich, dass die Suppe kalt ist.', model: 'The soup is cold, I’m afraid.', expect: ['cold'] },
      q('Welcher Satz klingt freundlich?', ['I’m afraid the soup is cold.', 'The soup is bad!'], 0, 'I’m afraid mildert die Kritik.'),
      'Kritik mit I’m afraid und einem Dank am Ende hält die Beziehung intakt.',
      ['cafe_l1_c2', 'missverstaendnis_l3_c1']),

    L('Die Lösung', 'Ich kann eine Lösung vorschlagen und freundlich abschließen.', 'conditionals_would_like', 5,
      [
        ['you', 'Could I have a refund, please?', 'Könnte ich das Geld zurückbekommen?'],
        ['sam', 'Of course. Would you like something else?', 'Natürlich. Möchten Sie etwas anderes?'],
        ['you', 'No, thank you. I’ll come back tomorrow.', 'Nein, danke. Ich komme morgen wieder.'],
        ['sam', 'See you then.', 'Bis dann.'],
      ],
      [
        q('Was möchte die Person?', ['Das Geld zurück', 'Einen neuen Tisch'], 0, '„refund“ = Rückerstattung.'),
        q('Kommt sie wieder?', ['Ja, morgen', 'Nein, nie'], 0, '„I’ll come back tomorrow“ = ich komme morgen wieder.'),
      ],
      [
        chunk('Could I have a refund, please?', 'Könnte ich das Geld zurückbekommen?', 'refund', 'Could I have ___?', 'Könnte ich ___ haben?', 'a refund', 'refund = Rückerstattung.'),
        chunk('I’ll come back tomorrow.', 'Ich komme morgen wieder.', 'come_back_tomorrow', 'I’ll come back ___', 'Ich komme ___ wieder', 'tomorrow', 'come back = wiederkommen.'),
      ],
      { promptDe: 'Bitte höflich um eine Rückerstattung.', model: 'Could I have a refund, please?', expect: ['refund'] },
      q('Welche Bitte ist höflich?', ['Could I have a refund, please?', 'Give me my money.'], 0, 'could + please ist höflich.'),
      'Eine Lösung anbieten statt Schuld zu verteilen: Could I have …? und ein freundlicher Abschluss.',
      ['cafe_l1_c1', 'nachbarn_l3_c1']),
  ]),
];
