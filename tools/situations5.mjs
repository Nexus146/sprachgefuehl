import { S, L, q, chunk } from './lib.mjs';

export const situations5 = [
  S('amt', 'behoerden', 'Im Bürgeramt', 'Termin, Formular, Nachfrage', 'Du meldest dich im neuen Viertel an.', 'home', [
    L('Einen Termin machen', 'Ich kann einen Termin erfragen.', 'word_order_question', 1,
      [
        ['clerk', 'Good morning. Do you have an appointment?', 'Guten Morgen. Haben Sie einen Termin?'],
        ['you', 'No, I don’t. Can I make one?', 'Nein. Kann ich einen machen?'],
        ['clerk', 'The next one is on Thursday at nine.', 'Der nächste ist am Donnerstag um neun.'],
        ['you', 'Thursday at nine is fine.', 'Donnerstag um neun passt.'],
      ],
      [
        q('Was fragt die Frau am Schalter zuerst?', ['Nach dem Termin', 'Nach dem Wetter'], 0, '„Do you have an appointment?“ fragt nach dem Termin.'),
        q('Wann ist der nächste Termin?', ['Donnerstag um neun', 'Dienstag um zehn'], 0, '„on Thursday at nine“ = Donnerstag um neun.'),
      ],
      [
        chunk('Do you have an appointment?', 'Haben Sie einen Termin?', 'do_you_have', 'Do you have ___?', 'Haben Sie ___?', 'an appointment', 'appointment = Termin.'),
        chunk('The next one is on Thursday.', 'Der nächste ist am Donnerstag.', 'on_day', 'The next one is on ___', 'Der nächste ist am ___', 'Thursday', 'on + Wochentag.'),
      ],
      { promptDe: 'Die Frau am Schalter fragt: „Do you have an appointment?“', model: 'No, I don’t. Can I make one?', expect: ['appointment', 'make'] },
      q('Welche Frage ist richtig?', ['Do you have an appointment?', 'Have you an appointment?'], 0, 'Fragen mit have bildet man mit do.'),
      'Termine: „on Thursday“ – on + Wochentag.'),

    L('Das Formular', 'Ich kann nach einem Formular fragen.', 'articles_quantity', 1,
      [
        ['you', 'I need a form for the new address.', 'Ich brauche ein Formular für die neue Adresse.'],
        ['clerk', 'Here you are. Please fill in this part.', 'Bitte. Füllen Sie diesen Teil aus.'],
        ['you', 'Do I need my passport?', 'Brauche ich meinen Pass?'],
        ['clerk', 'Yes, and a copy of your lease.', 'Ja, und eine Kopie Ihres Mietvertrags.'],
      ],
      [
        q('Was brauchst du?', ['Ein Formular', 'Eine Fahrkarte'], 0, '„a form“ = ein Formular.'),
        q('Was sollst du mitbringen?', ['Pass und Mietvertrag', 'Nur ein Foto'], 0, '„your passport“ und „your lease“.'),
      ],
      [
        chunk('I need a form.', 'Ich brauche ein Formular.', 'i_need', 'I need ___', 'Ich brauche ___', 'a form', 'a form = ein Formular.'),
        chunk('Do I need my passport?', 'Brauche ich meinen Pass?', 'do_i_need', 'Do I need ___?', 'Brauche ich ___?', 'my passport', 'Frage mit do + I.'),
      ],
      { promptDe: 'Du stehst am Schalter und brauchst ein Formular.', model: 'I need a form, please.', expect: ['form'] },
      q('Was passt?', ['a form', 'form'], 0, 'Zählbares Singular braucht a.'),
      'a form, the form – neu und bekannt.'),

    L('Wann ist es fertig?', 'Ich kann nach dem Stand fragen.', 'future', 2,
      [
        ['you', 'When will it be ready?', 'Wann ist es fertig?'],
        ['clerk', 'It will take about two weeks.', 'Es dauert etwa zwei Wochen.'],
        ['you', 'Can I call and ask?', 'Kann ich anrufen und fragen?'],
        ['clerk', 'Yes, or you’ll get a letter.', 'Ja, oder Sie bekommen einen Brief.'],
      ],
      [
        q('Wie lange dauert es?', ['Etwa zwei Wochen', 'Zwei Tage'], 0, '„about two weeks“ = etwa zwei Wochen.'),
        q('Wie erfährst du es?', ['Per Brief oder Anruf', 'Gar nicht'], 0, '„or you’ll get a letter“ – oder ein Brief.'),
      ],
      [
        chunk('When will it be ready?', 'Wann ist es fertig?', 'when_ready', 'When will it be ___?', 'Wann ist es ___?', 'ready', 'will für die Zukunft.'),
        chunk('It will take two weeks.', 'Es dauert zwei Wochen.', 'will_take', 'It will take ___', 'Es dauert ___', 'two weeks', 'take time = dauern.'),
      ],
      { promptDe: 'Frag, wann das Formular fertig ist.', model: 'When will it be ready?', expect: ['when', 'ready'] },
      q('Welche Zeit ist richtig?', ['It will take two weeks.', 'It will takes two weeks.'], 0, 'Nach will steht die Grundform.'),
      'will + Grundform für Vorhersagen.'),
  ]),

  S('versicherung', 'behoerden', 'Bei der Krankenkasse', 'Am Telefon klären', 'Deine Karte ist abgelaufen.', 'health', [
    L('Meine Karte ist abgelaufen', 'Ich kann ein Problem am Telefon erklären.', 'present_progressive', 2,
      [
        ['you', 'Hello, my card has expired.', 'Hallo, meine Karte ist abgelaufen.'],
        ['clerk', 'I’m sending you a new one.', 'Ich schicke Ihnen eine neue.'],
        ['you', 'How long does it take?', 'Wie lange dauert das?'],
        ['clerk', 'About ten days.', 'Etwa zehn Tage.'],
      ],
      [
        q('Was ist das Problem?', ['Die Karte ist abgelaufen', 'Die Karte ist verloren'], 0, '„has expired“ = ist abgelaufen.'),
        q('Was macht die Frau?', ['Sie schickt eine neue Karte', 'Sie schickt einen Brief'], 0, '„I’m sending you a new one“ – eine neue Karte.'),
      ],
      [
        chunk('My card has expired.', 'Meine Karte ist abgelaufen.', 'has_expired', 'My ___ has expired', 'Mein ___ ist abgelaufen', 'card', 'expired = abgelaufen.'),
        chunk('How long does it take?', 'Wie lange dauert das?', 'how_long', 'How long ___?', 'Wie lange ___?', 'does it take', 'how long = wie lange.'),
      ],
      { promptDe: 'Sag am Telefon: Meine Karte ist abgelaufen.', model: 'My card has expired.', expect: ['card', 'expired'] },
      q('Welche Frage ist richtig?', ['How long does it take?', 'How long it takes?'], 0, 'Fragen mit does.'),
      'have + Partizip: has expired.'),

    L('Welche Unterlagen?', 'Ich kann nach Unterlagen fragen.', 'articles_quantity', 1,
      [
        ['clerk', 'We need your insurance number.', 'Wir brauchen Ihre Versicherungsnummer.'],
        ['you', 'Which documents do I need?', 'Welche Unterlagen brauche ich?'],
        ['clerk', 'Your card and a copy of your ID.', 'Ihre Karte und eine Kopie Ihres Ausweises.'],
        ['you', 'Please send me the address again.', 'Bitte schicken Sie mir die Adresse noch einmal.'],
      ],
      [
        q('Was braucht die Kasse?', ['Die Versicherungsnummer', 'Die Bankverbindung'], 0, '„insurance number“ = Versicherungsnummer.'),
        q('Was sollst du schicken?', ['Eine Kopie des Ausweises', 'Das Original'], 0, '„a copy of your ID“ – eine Kopie.'),
      ],
      [
        chunk('Which documents do I need?', 'Welche Unterlagen brauche ich?', 'which_documents', 'Which documents do I need?', 'Welche Unterlagen brauche ich?', 'documents', 'documents = Unterlagen.'),
        chunk('Please send a copy.', 'Bitte schicken Sie eine Kopie.', 'please_send', 'Please send ___', 'Bitte schicken Sie ___', 'a copy', 'copy = Kopie.'),
      ],
      { promptDe: 'Frag, welche Unterlagen du brauchst.', model: 'Which documents do I need?', expect: ['which', 'documents'] },
      q('Welche Frage ist richtig?', ['Which documents do I need?', 'Which documents I need?'], 0, 'Frage: which + Nomen + do I need.'),
      'which fragt nach einer Auswahl.'),

    L('Ich verstehe das nicht', 'Ich kann höflich nachfragen.', 'negation_short_answers', 3,
      [
        ['clerk', 'You need to fill in the second page as well.', 'Sie müssen auch die zweite Seite ausfüllen.'],
        ['you', 'Sorry, I don’t understand. Could you repeat that?', 'Entschuldigung, ich verstehe nicht. Könnten Sie das wiederholen?'],
        ['clerk', 'Of course. Fill in page two.', 'Natürlich. Füllen Sie Seite zwei aus.'],
        ['you', 'Thank you. Is that all?', 'Danke. Ist das alles?'],
      ],
      [
        q('Was sollst du tun?', ['Seite zwei ausfüllen', 'Seite zwei wegwerfen'], 0, '„fill in page two“ = Seite zwei ausfüllen.'),
        q('Worum bittest du?', ['Um eine Wiederholung', 'Um einen Rabatt'], 0, '„Could you repeat that?“ = Könnten Sie das wiederholen?'),
      ],
      [
        chunk('I don’t understand.', 'Ich verstehe nicht.', 'dont_understand', 'I don’t ___', 'Ich ___ nicht', 'understand', 'don’t + Grundform.'),
        chunk('Could you repeat that?', 'Könnten Sie das wiederholen?', 'could_repeat', 'Could you ___?', 'Könnten Sie ___?', 'repeat that', 'repeat = wiederholen.'),
      ],
      { promptDe: 'Sag höflich, dass du es nicht verstehst.', model: 'Sorry, I don’t understand. Could you repeat that?', expect: ['understand', 'repeat'] },
      q('Welche Verneinung ist richtig?', ['I don’t understand.', 'I not understand.'], 0, 'Verneinung mit don’t.'),
      'don’t + Grundform.'),
  ]),

  S('einladung', 'feste', 'Zum Geburtstag einladen', 'Einladen und absagen', 'Dein Geburtstag steht bevor.', 'people', [
    L('Einladen', 'Ich kann jemanden einladen.', 'modals_politeness', 1,
      [
        ['you', 'Would you like to come to my birthday?', 'Möchtest du zu meinem Geburtstag kommen?'],
        ['hana', 'I’d love to. When is it?', 'Sehr gern. Wann ist es?'],
        ['you', 'On Saturday at four.', 'Am Samstag um vier.'],
        ['hana', 'Shall I bring something?', 'Soll ich etwas mitbringen?'],
      ],
      [
        q('Wozu lädt sie ein?', ['Zum Geburtstag', 'Zum Kaffeetrinken'], 0, '„to my birthday“ = zu meinem Geburtstag.'),
        q('Wann ist es?', ['Samstag um vier', 'Sonntag um sechs'], 0, '„on Saturday at four“.'),
      ],
      [
        chunk('Would you like to come?', 'Möchtest du kommen?', 'would_like_to', 'Would you like to ___?', 'Möchtest du ___?', 'come', 'Would you like = höfliche Einladung.'),
        chunk('Shall I bring something?', 'Soll ich etwas mitbringen?', 'shall_i', 'Shall I ___?', 'Soll ich ___?', 'bring something', 'shall I = Angebot.'),
      ],
      { promptDe: 'Lade Hana zu deinem Geburtstag ein.', model: 'Would you like to come to my birthday?', expect: ['would', 'birthday'] },
      q('Welche Einladung ist höflich?', ['Would you like to come?', 'Do you come?'], 0, 'Would you like … ist die höfliche Form.'),
      'would like to + Grundform.'),

    L('Absagen', 'Ich kann freundlich absagen.', 'modals_politeness', 2,
      [
        ['hana', 'Can you come on Sunday instead?', 'Kannst du stattdessen am Sonntag kommen?'],
        ['you', 'I’m afraid I can’t. I have an appointment.', 'Leider kann ich nicht. Ich habe einen Termin.'],
        ['hana', 'No problem. Another time.', 'Kein Problem. Ein anderes Mal.'],
        ['you', 'Thanks for understanding.', 'Danke für Ihr Verständnis.'],
      ],
      [
        q('Warum kann sie nicht?', ['Sie hat einen Termin', 'Sie ist krank'], 0, '„I have an appointment“ = ich habe einen Termin.'),
        q('Wie reagiert Hana?', ['Verständnisvoll', 'Ärgerlich'], 0, '„No problem“ – kein Problem.'),
      ],
      [
        chunk('I’m afraid I can’t.', 'Leider kann ich nicht.', 'afraid_cant', 'I’m afraid I ___', 'Leider ___ ich nicht', 'can’t', 'I’m afraid = leider.'),
        chunk('Another time, then.', 'Dann ein anderes Mal.', 'another_time', 'Another ___', 'Ein anderes ___', 'time', 'another = ein anderes.'),
      ],
      { promptDe: 'Sag freundlich ab.', model: 'I’m afraid I can’t. I have an appointment.', expect: ['afraid', 'appointment'] },
      q('Welche Absage ist richtig?', ['I’m afraid I can’t.', 'I afraid I can’t.'], 0, 'I am afraid – das am gehört dazu.'),
      'Höfliche Absage: I’m afraid …'),

    L('Was soll ich mitbringen?', 'Ich kann über Beiträge sprechen.', 'articles_quantity', 1,
      [
        ['you', 'What should I bring?', 'Was soll ich mitbringen?'],
        ['hana', 'Just yourself. Maybe some bread.', 'Nur dich. Vielleicht etwas Brot.'],
        ['you', 'I’ll bring a salad.', 'Ich bringe einen Salat mit.'],
        ['hana', 'That’s perfect.', 'Das ist perfekt.'],
      ],
      [
        q('Was soll sie mitbringen?', ['Vielleicht Brot', 'Auf jeden Fall Wein'], 0, '„Maybe some bread“ – vielleicht Brot.'),
        q('Was bringt sie?', ['Einen Salat', 'Einen Kuchen'], 0, '„I’ll bring a salad“.'),
      ],
      [
        chunk('What should I bring?', 'Was soll ich mitbringen?', 'what_should', 'What should I ___?', 'Was soll ich ___?', 'bring', 'should + Grundform.'),
        chunk('I’ll bring a salad.', 'Ich bringe einen Salat mit.', 'ill_bring', 'I’ll bring ___', 'Ich bringe ___ mit', 'a salad', 'will = kurzfristige Entscheidung.'),
      ],
      { promptDe: 'Frag, was du mitbringen sollst.', model: 'What should I bring?', expect: ['what', 'bring'] },
      q('Welche Zeit passt zur spontanen Entscheidung?', ['I’ll bring a salad.', 'I bring a salad yesterday.'], 0, 'will für eine spontane Entscheidung.'),
      'will vs. going to.'),
  ]),

  S('nachbarschaftsfest', 'feste', 'Beim Nachbarschaftsfest', 'Helfen und mitreden', 'Das Fest im Hof ist geplant.', 'leaf', [
    L('Beim Planen helfen', 'Ich kann Hilfe anbieten.', 'modals_politeness', 3,
      [
        ['hana', 'We’re planning a party in the yard.', 'Wir planen ein Fest im Hof.'],
        ['you', 'Can I help with something?', 'Kann ich bei etwas helfen?'],
        ['hana', 'Could you bring some chairs?', 'Könntest du ein paar Stühle bringen?'],
        ['you', 'Of course. How many?', 'Natürlich. Wie viele?'],
      ],
      [
        q('Was planen sie?', ['Ein Fest im Hof', 'Einen Umzug'], 0, '„a party in the yard“ = ein Fest im Hof.'),
        q('Was soll sie bringen?', ['Stühle', 'Tische'], 0, '„some chairs“ = ein paar Stühle.'),
      ],
      [
        chunk('Can I help with something?', 'Kann ich bei etwas helfen?', 'can_i_help', 'Can I help with ___?', 'Kann ich bei ___ helfen?', 'something', 'help with = helfen bei.'),
        chunk('How many do you need?', 'Wie viele brauchst du?', 'how_many', 'How many ___?', 'Wie viele ___?', 'do you need', 'many + zählbarer Plural.'),
      ],
      { promptDe: 'Biete deine Hilfe an.', model: 'Can I help with something?', expect: ['help', 'something'] },
      q('Was passt zu „chairs“?', ['How many chairs?', 'How much chairs?'], 0, 'many + zählbarer Plural.'),
      'much für Unzählbares, many für Zählbares.'),

    L('Mitreden', 'Ich kann meine Meinung sagen.', 'connectors_word_order', 2,
      [
        ['krueger', 'I think we should start at six.', 'Ich finde, wir sollten um sechs anfangen.'],
        ['you', 'I agree, but it gets cold in the evening.', 'Ich stimme zu, aber abends wird es kalt.'],
        ['krueger', 'Then we could start at five.', 'Dann könnten wir um fünf anfangen.'],
        ['you', 'That’s a good idea.', 'Das ist eine gute Idee.'],
      ],
      [
        q('Wann anfangen?', ['Um sechs', 'Um fünf'], 0, '„start at six“ – um sechs.'),
        q('Was gibt sie zu bedenken?', ['Es wird kalt', 'Es regnet'], 0, '„it gets cold in the evening“.'),
      ],
      [
        chunk('I agree, but …', 'Ich stimme zu, aber …', 'i_agree_but', 'I agree, but ___', 'Ich stimme zu, aber ___', 'it gets cold', 'but zeigt den Einwand.'),
        chunk('That’s a good idea.', 'Das ist eine gute Idee.', 'good_idea', 'That’s a ___', 'Das ist eine ___', 'good idea', 'idea = Idee.'),
      ],
      { promptDe: 'Sag deine Meinung mit einem Einwand.', model: 'I agree, but it gets cold in the evening.', expect: ['agree', 'cold'] },
      q('Welcher Satz ist richtig?', ['I agree, but it gets cold.', 'I agree, and but it gets cold.'], 0, 'Ein Konnektor reicht.'),
      'but, because, so – je eine Aufgabe.'),

    L('Über das Fest erzählen', 'Ich kann erzählen, wie es war.', 'past_simple', 4,
      [
        ['hana', 'How was the party?', 'Wie war das Fest?'],
        ['you', 'It was lovely. We talked until ten.', 'Es war schön. Wir haben bis zehn geredet.'],
        ['hana', 'Did many people come?', 'Sind viele Leute gekommen?'],
        ['you', 'Yes, almost everyone from the house.', 'Ja, fast alle aus dem Haus.'],
      ],
      [
        q('Wie war das Fest?', ['Schön', 'Langweilig'], 0, '„lovely“ = schön.'),
        q('Bis wann haben sie geredet?', ['Bis zehn', 'Bis zwölf'], 0, '„until ten“ = bis zehn.'),
      ],
      [
        chunk('It was lovely.', 'Es war schön.', 'it_was', 'It was ___', 'Es war ___', 'lovely', 'was = Vergangenheit von be.'),
        chunk('We talked until ten.', 'Wir haben bis zehn geredet.', 'talked_until', 'We ___ until ten', 'Wir haben bis zehn ___', 'talked', '-ed bildet die regelmäßige Vergangenheit.'),
      ],
      { promptDe: 'Erzähl, wie das Fest war.', model: 'It was lovely. We talked until ten.', expect: ['was', 'talked'] },
      q('Welche Frage ist richtig?', ['Did many people come?', 'Did many people came?'], 0, 'Nach did steht die Grundform.'),
      'Vergangenheit: did + Grundform in Fragen.'),
  ]),

  S('ausweis', 'behoerden', 'Neuer Ausweis', 'Antrag, Preis, Warten', 'Dein Pass läuft bald ab.', 'home', [
    L('Der Antrag', 'Ich kann einen Antrag stellen.', 'word_order_question', 1,
      [
        ['you', 'I would like to apply for a new passport.', 'Ich möchte einen neuen Pass beantragen.'],
        ['clerk', 'Do you have a photo and your old passport?', 'Haben Sie ein Foto und Ihren alten Pass?'],
        ['you', 'Yes, here you are.', 'Ja, bitte schön.'],
        ['clerk', 'Thank you. Please fill in this form.', 'Danke. Bitte füllen Sie dieses Formular aus.'],
      ],
      [
        q('Was möchtest du?', ['Einen neuen Pass', 'Ein neues Auto'], 0, '„apply for a new passport“ = einen neuen Pass beantragen.'),
        q('Was sollst du ausfüllen?', ['Ein Formular', 'Einen Scheck'], 0, '„fill in this form“ = dieses Formular ausfüllen.'),
      ],
      [
        chunk('I would like to apply for a passport.', 'Ich möchte einen Pass beantragen.', 'would_like_apply', 'I would like to apply for ___', 'Ich möchte ___ beantragen', 'a passport', 'would like to + Grundform = höflich wollen.'),
        chunk('Do you have a photo?', 'Haben Sie ein Foto?', 'do_you_have', 'Do you have ___?', 'Haben Sie ___?', 'a photo', 'Frage mit do.'),
      ],
      { promptDe: 'Sag am Schalter, dass du einen neuen Pass brauchst.', model: 'I would like to apply for a new passport.', expect: ['apply', 'passport'] },
      q('Welche Form ist höflich?', ['I would like to apply for a passport.', 'I want apply a passport.'], 0, 'would like to + Grundform.'),
      'would like to + Grundform.'),

    L('Was kostet das?', 'Ich kann nach dem Preis fragen.', 'articles_quantity', 1,
      [
        ['you', 'How much is it?', 'Wie viel kostet es?'],
        ['clerk', 'It’s sixty euros.', 'Es kostet sechzig Euro.'],
        ['you', 'Can I pay by card?', 'Kann ich mit Karte bezahlen?'],
        ['clerk', 'Yes, at the counter over there.', 'Ja, dort am Schalter.'],
      ],
      [
        q('Wie viel kostet es?', ['Sechzig Euro', 'Sechzehn Euro'], 0, '„sixty euros“ = sechzig Euro.'),
        q('Wie kannst du bezahlen?', ['Mit Karte', 'Nur bar'], 0, '„pay by card“ = mit Karte bezahlen.'),
      ],
      [
        chunk('How much is it?', 'Wie viel kostet es?', 'how_much_is', 'How much is ___?', 'Wie viel kostet ___?', 'it', 'how much fragt nach dem Preis.'),
        chunk('Can I pay by card?', 'Kann ich mit Karte bezahlen?', 'can_i_pay', 'Can I pay by ___?', 'Kann ich mit ___ bezahlen?', 'card', 'by card = mit Karte.'),
      ],
      { promptDe: 'Frag am Schalter nach dem Preis.', model: 'How much is it?', expect: ['how', 'much'] },
      q('Was passt zum Preis?', ['How much is it?', 'How many is it?'], 0, 'much für Unzählbares wie Geld.'),
      'much für Preise, many für zählbare Dinge.'),

    L('Wann ist es fertig?', 'Ich kann nach dem Termin fragen.', 'future', 2,
      [
        ['you', 'When will the passport be ready?', 'Wann ist der Pass fertig?'],
        ['clerk', 'It will take about three weeks.', 'Es dauert etwa drei Wochen.'],
        ['you', 'Will you send it to my address?', 'Schicken Sie ihn an meine Adresse?'],
        ['clerk', 'Yes, or you can pick it up here.', 'Ja, oder Sie holen ihn hier ab.'],
      ],
      [
        q('Wie lange dauert es?', ['Etwa drei Wochen', 'Drei Tage'], 0, '„about three weeks“ = etwa drei Wochen.'),
        q('Wie bekommst du den Pass?', ['Per Post oder hier', 'Nur per E-Mail'], 0, '„send it“ oder „pick it up here“.'),
      ],
      [
        chunk('When will it be ready?', 'Wann ist es fertig?', 'when_ready', 'When will it be ___?', 'Wann ist es ___?', 'ready', 'will für die Zukunft.'),
        chunk('It will take three weeks.', 'Es dauert drei Wochen.', 'will_take', 'It will take ___', 'Es dauert ___', 'three weeks', 'take time = dauern.'),
      ],
      { promptDe: 'Frag, wann der Pass fertig ist.', model: 'When will the passport be ready?', expect: ['when', 'ready'] },
      q('Welche Zeit ist richtig?', ['It will take three weeks.', 'It will takes three weeks.'], 0, 'Nach will steht die Grundform.'),
      'will + Grundform für die Zukunft.'),
  ]),

  S('abendessen', 'feste', 'Zu Gast zum Abendessen', 'Loben, helfen, verabschieden', 'Hana hat dich zum Essen eingeladen.', 'people', [
    L('Es riecht gut', 'Ich kann etwas loben.', 'present_progressive', 1,
      [
        ['hana', 'Come in! Dinner is almost ready.', 'Komm herein! Das Essen ist fast fertig.'],
        ['you', 'It smells wonderful.', 'Es riecht wunderbar.'],
        ['hana', 'I’m making a soup from my mother’s recipe.', 'Ich mache eine Suppe nach dem Rezept meiner Mutter.'],
        ['you', 'I’m sure it tastes as good as it smells.', 'Sie schmeckt sicher so gut, wie sie riecht.'],
      ],
      [
        q('Was kocht Hana?', ['Eine Suppe', 'Einen Kuchen'], 0, '„a soup“ = eine Suppe.'),
        q('Wie riecht es?', ['Wunderbar', 'Seltsam'], 0, '„It smells wonderful“ = es riecht wunderbar.'),
      ],
      [
        chunk('It smells wonderful.', 'Es riecht wunderbar.', 'it_smells', 'It smells ___', 'Es riecht ___', 'wonderful', 'smell = riechen.'),
        chunk('I’m making a soup.', 'Ich mache eine Suppe.', 'im_making', 'I’m making ___', 'Ich mache ___', 'a soup', 'Verlaufsform für „gerade jetzt“.'),
      ],
      { promptDe: 'Sag Hana, dass es gut riecht.', model: 'It smells wonderful.', expect: ['smells', 'wonderful'] },
      q('Was passt zu „gerade jetzt“?', ['I’m making a soup.', 'I make a soup every day.'], 0, 'Verlaufsform = gerade jetzt.'),
      'am/is/are + -ing: was gerade passiert.'),

    L('Darf ich helfen?', 'Ich kann Hilfe anbieten.', 'modals_politeness', 2,
      [
        ['you', 'Can I help you with anything?', 'Kann ich dir irgendwie helfen?'],
        ['hana', 'Could you set the table?', 'Könntest du den Tisch decken?'],
        ['you', 'Of course. Where are the plates?', 'Natürlich. Wo sind die Teller?'],
        ['hana', 'In the cupboard on the left.', 'Im Schrank links.'],
      ],
      [
        q('Was sollst du tun?', ['Den Tisch decken', 'Das Essen kochen'], 0, '„set the table“ = den Tisch decken.'),
        q('Wo sind die Teller?', ['Im Schrank links', 'Im Kühlschrank'], 0, '„in the cupboard on the left“.'),
      ],
      [
        chunk('Can I help you with anything?', 'Kann ich dir irgendwie helfen?', 'can_i_help', 'Can I help with ___?', 'Kann ich bei ___ helfen?', 'anything', 'help with = helfen bei.'),
        chunk('Could you set the table?', 'Könntest du den Tisch decken?', 'could_you_set', 'Could you ___?', 'Könntest du ___?', 'set the table', 'could ist höflicher als can.'),
      ],
      { promptDe: 'Biete Hana deine Hilfe an.', model: 'Can I help you with anything?', expect: ['help', 'anything'] },
      q('Welche Bitte ist höflicher?', ['Could you set the table?', 'Set the table!'], 0, 'could macht die Bitte höflich.'),
      'could = höfliche Bitte, can = neutral.'),

    L('Ein schöner Abend', 'Ich kann mich bedanken.', 'past_simple', 3,
      [
        ['you', 'Thank you for the lovely evening.', 'Danke für den schönen Abend.'],
        ['hana', 'Did you like the soup?', 'Hat dir die Suppe geschmeckt?'],
        ['you', 'I loved it. Could I have the recipe?', 'Sie war köstlich. Könnte ich das Rezept haben?'],
        ['hana', 'Of course. I’ll write it down for you.', 'Natürlich. Ich schreibe es dir auf.'],
      ],
      [
        q('Wie war der Abend?', ['Schön', 'Langweilig'], 0, '„the lovely evening“ = der schöne Abend.'),
        q('Was möchte sie haben?', ['Das Rezept', 'Die Suppe'], 0, '„have the recipe“ = das Rezept haben.'),
      ],
      [
        chunk('Thank you for the lovely evening.', 'Danke für den schönen Abend.', 'thank_you_for', 'Thank you for ___', 'Danke für ___', 'the lovely evening', 'thank you for + Nomen.'),
        chunk('I loved it.', 'Es hat mir sehr gefallen.', 'i_loved_it', 'I ___ it', 'Es hat mir ___', 'loved', 'Vergangenheit von love.'),
      ],
      { promptDe: 'Bedanke dich für den Abend.', model: 'Thank you for the lovely evening.', expect: ['thank', 'evening'] },
      q('Welche Vergangenheit ist richtig?', ['I loved it.', 'I love it yesterday.'], 0, 'Vergangenheit mit -ed.'),
      'Past Simple: -ed bei regelmäßigen Verben.'),
  ]),
];
