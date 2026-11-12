import { S, L, q, chunk } from './lib.mjs';

export const situations1 = [
  S('arzt', 'gesundheit', 'Beim Arzt', 'Sagen, was weh tut', 'Du fühlst dich nicht gut und gehst zur Hausärztin.', 'stethoscope', [
    L('Was fehlt dir?', 'Ich kann sagen, was mir weh tut.', 'articles_quantity', 1,
      [
        ['doctor', 'Good morning. What’s the problem?', 'Guten Morgen. Was ist das Problem?'],
        ['you', 'I have a bad headache.', 'Ich habe starke Kopfschmerzen.'],
        ['doctor', 'How long have you had it?', 'Seit wann haben Sie das?'],
        ['you', 'For two days.', 'Seit zwei Tagen.'],
      ],
      [
        q('Was fragt die Ärztin zuerst?', ['Nach dem Problem', 'Nach dem Wetter'], 0, '„What’s the problem?“ heißt „Was ist das Problem?“'),
        q('Was bedeutet „for two days“?', ['Seit zwei Tagen', 'In zwei Tagen'], 0, '„for“ + Zeitraum beschreibt eine Dauer.'),
      ],
      [
        chunk('I have a bad headache.', 'Ich habe starke Kopfschmerzen.', 'i_have', 'I have ___', 'Ich habe ___', 'a bad headache', '„headache“: das a in der Mitte klingt wie ein kurzes ä.'),
        chunk('It started two days ago.', 'Es hat vor zwei Tagen angefangen.', 'started_ago', 'It started ___ ago', 'Es hat vor ___ angefangen', 'two days', '„ago“ steht nach dem Zeitraum.'),
      ],
      { promptDe: 'Die Ärztin fragt: „What’s the problem?“', model: 'I have a bad headache.', expect: ['headache'] },
      q('Welcher Satz ist richtig?', ['I have a headache.', 'I have headache.'], 0, '„headache“ ist zählbar – deshalb „a headache“.'),
      'Achte auf „a“ vor dem Symptom: a headache, a cough, a fever.'),

    L('Seit wann?', 'Ich kann sagen, wie lange etwas dauert.', 'past_simple', 2,
      [
        ['doctor', 'How long have you had the headache?', 'Seit wann haben Sie die Kopfschmerzen?'],
        ['you', 'For about two days.', 'Etwa seit zwei Tagen.'],
        ['doctor', 'Do you have a fever?', 'Haben Sie Fieber?'],
        ['you', 'No, I don’t. But I’m very tired.', 'Nein. Aber ich bin sehr müde.'],
      ],
      [
        q('Was will die Ärztin wissen?', ['Wie lange die Beschwerden dauern', 'Ob du Sport machst'], 0, '„How long“ fragt nach der Dauer.'),
        q('Was heißt „I’m very tired“?', ['Ich bin sehr müde', 'Ich habe Hunger'], 0, 'tired = müde.'),
      ],
      [
        chunk('I’ve had it for two days.', 'Ich habe es seit zwei Tagen.', 'for_since', 'I’ve had it for ___', 'Ich habe es seit ___', 'two days', 'for + Zeitraum, since + Zeitpunkt.'),
        chunk('Do you have a fever?', 'Haben Sie Fieber?', 'do_you_have', 'Do you have ___?', 'Haben Sie ___?', 'a fever', 'Fragen mit do/does – nicht „Have you a fever?“'),
      ],
      { promptDe: 'Die Ärztin fragt: „How long have you had it?“', model: 'For two days.', expect: ['two', 'days'] },
      q('Welche Frage ist richtig?', ['Do you have a fever?', 'Have you a fever?'], 0, 'Im modernen Englisch bildet man die Frage mit do.'),
      'Deutsch: „Ich habe seit zwei Tagen …“ – Englisch: „I have had it for two days.“'),

    L('Was soll ich tun?', 'Ich kann verstehen, was ich tun soll.', 'modals_politeness', 1,
      [
        ['doctor', 'You should rest and drink water.', 'Sie sollten sich ausruhen und Wasser trinken.'],
        ['you', 'Should I take anything?', 'Soll ich etwas nehmen?'],
        ['doctor', 'Yes, take one tablet twice a day.', 'Ja, nehmen Sie eine Tablette zweimal täglich.'],
        ['you', 'Thank you very much.', 'Vielen Dank.'],
      ],
      [
        q('Was empfiehlt die Ärztin?', ['Ausruhen und Wasser trinken', 'Sport machen'], 0, '„rest“ = ausruhen.'),
        q('Wie oft soll die Tablette genommen werden?', ['Zweimal täglich', 'Einmal pro Woche'], 0, '„twice a day“ = zweimal täglich.'),
      ],
      [
        chunk('You should rest.', 'Sie sollten sich ausruhen.', 'you_should', 'You should ___', 'Sie sollten ___', 'rest', 'Nach „should“ steht die Grundform, kein „to“.'),
        chunk('Take one tablet twice a day.', 'Nehmen Sie eine Tablette zweimal täglich.', 'take_twice', 'Take ___ twice a day', 'Nehmen Sie ___ zweimal täglich', 'one tablet', 'once = einmal, twice = zweimal, three times = dreimal.'),
      ],
      { promptDe: 'Die Ärztin sagt: „You should rest.“ Frage nach: „Soll ich etwas nehmen?“', model: 'Should I take anything?', expect: ['should', 'take'] },
      q('Welche Form ist richtig?', ['You should rest.', 'You should to rest.'], 0, 'Nach should steht die Grundform.'),
      'should = sollten – ein Rat, keine Pflicht. must = müssen (stärker).'),
  ]),

  S('apotheke', 'gesundheit', 'In der Apotheke', 'Etwas gegen Beschwerden holen', 'Du brauchst etwas gegen Husten.', 'pharmacy', [
    L('Ich brauche etwas', 'Ich kann sagen, was ich brauche.', 'articles_quantity', 1,
      [
        ['you', 'Good morning. I need something for a cough.', 'Guten Morgen. Ich brauche etwas gegen Husten.'],
        ['pharmacist', 'Do you have a prescription?', 'Haben Sie ein Rezept?'],
        ['you', 'No, I don’t.', 'Nein.'],
        ['pharmacist', 'This syrup is good. Take it three times a day.', 'Dieser Saft ist gut. Nehmen Sie ihn dreimal täglich.'],
      ],
      [
        q('Was braucht die Person?', ['Etwas gegen Husten', 'Etwas gegen Fieber'], 0, '„a cough“ = Husten.'),
        q('Braucht sie ein Rezept?', ['Nein', 'Ja, zwei'], 0, '„No, I don’t.“ = Nein.'),
      ],
      [
        chunk('I need something for a cough.', 'Ich brauche etwas gegen Husten.', 'i_need', 'I need something for ___', 'Ich brauche etwas gegen ___', 'a cough', '„for“ heißt hier „gegen“ – nicht „against“.'),
        chunk('Do you have a prescription?', 'Haben Sie ein Rezept?', 'do_you_have', 'Do you have ___?', 'Haben Sie ___?', 'a prescription', 'prescription = Rezept (vom Arzt).'),
      ],
      { promptDe: 'Der Apotheker fragt: „Can I help you?“', model: 'I need something for a cough.', expect: ['need', 'cough'] },
      q('Welche Präposition passt?', ['something for a cough', 'something against a cough'], 0, 'Englisch sagt hier „for“, nicht wörtlich „against“.'),
      '„something for a cough“ – feste Wendung, wie „etwas gegen Husten“.'),
    L('Wie oft?', 'Ich kann Anweisungen verstehen.', 'articles_quantity', 2,
      [
        ['pharmacist', 'Take one spoonful in the morning and one in the evening.', 'Nehmen Sie morgens einen Löffel und abends einen.'],
        ['you', 'Before or after food?', 'Vor oder nach dem Essen?'],
        ['pharmacist', 'After food.', 'Nach dem Essen.'],
        ['you', 'Thank you. How much is it?', 'Danke. Wie viel kostet es?'],
      ],
      [
        q('Wann soll die Person den Saft nehmen?', ['Morgens und abends', 'Nur mittags'], 0, '„in the morning … in the evening“ = morgens … abends.'),
        q('Vor oder nach dem Essen?', ['Nach dem Essen', 'Vor dem Essen'], 0, '„after food“ = nach dem Essen.'),
      ],
      [
        chunk('Before or after food?', 'Vor oder nach dem Essen?', 'before_after', 'Before or after ___?', 'Vor oder nach ___?', 'food', 'before = vor, after = nach.'),
        chunk('How much is it?', 'Wie viel kostet es?', 'how_much', 'How much is ___?', 'Wie viel kostet ___?', 'it', '„How much“ fragt nach dem Preis.'),
      ],
      { promptDe: 'Der Apotheker fragt: „Before or after food?“', model: 'After food, please.', expect: ['after', 'food'] },
      q('Was heißt „twice a day“?', ['zweimal täglich', 'zwei Tage lang'], 0, 'twice = zweimal.'),
      'Mengen: once, twice, three times – danach „a day“.'),
    L('Gibt es das auch ohne Rezept?', 'Ich kann nach Alternativen fragen.', 'articles_quantity', 2,
      [
        ['you', 'Is there something without a prescription?', 'Gibt es etwas ohne Rezept?'],
        ['pharmacist', 'Yes, these drops are fine.', 'Ja, diese Tropfen sind gut.'],
        ['you', 'Are there any side effects?', 'Gibt es Nebenwirkungen?'],
        ['pharmacist', 'They can make you sleepy.', 'Sie können müde machen.'],
      ],
      [
        q('Was sucht die Person?', ['Etwas ohne Rezept', 'Etwas mit Rezept'], 0, '„without a prescription“ = ohne Rezept.'),
        q('Welche Nebenwirkung gibt es?', ['Müdigkeit', 'Kopfschmerzen'], 0, '„sleepy“ = müde.'),
      ],
      [
        chunk('Is there something without a prescription?', 'Gibt es etwas ohne Rezept?', 'is_there', 'Is there ___?', 'Gibt es ___?', 'something without a prescription', '„Is there“ = gibt es (Singular); „Are there“ = Plural.'),
        chunk('They can make you sleepy.', 'Sie können müde machen.', 'can_make', 'It can make you ___', 'Es kann dich ___ machen', 'sleepy', 'make + Eigenschaft: make you sleepy.'),
      ],
      { promptDe: 'Frage in der Apotheke: „Gibt es etwas ohne Rezept?“', model: 'Is there something without a prescription?', expect: ['without', 'prescription'] },
      q('Welche Frage ist richtig?', ['Is there something without a prescription?', 'Is something without a prescription there?'], 0, 'Frage: Is there + Ding + Zusatz.'),
      'there is / there are = es gibt.'),
  ]),

  S('nachbarn', 'leute', 'Nachbarn & Small Talk', 'Ins Gespräch kommen', 'Du triffst deine Nachbarin im Hausflur.', 'people', [
    L('Guten Tag', 'Ich kann jemanden begrüßen und vorstellen.', 'be_have', 1,
      [
        ['neighbour', 'Good morning! How are you?', 'Guten Morgen! Wie geht es Ihnen?'],
        ['you', 'I’m fine, thank you. And you?', 'Mir geht es gut, danke. Und Ihnen?'],
        ['neighbour', 'Not bad. Are you new here?', 'Nicht schlecht. Sind Sie neu hier?'],
        ['you', 'Yes, I moved in last week.', 'Ja, ich bin letzte Woche eingezogen.'],
      ],
      [
        q('Was fragt die Nachbarin?', ['Wie es dir geht', 'Wo du arbeitest'], 0, '„How are you?“ fragt nach dem Befinden.'),
        q('Seit wann wohnt die Person hier?', ['Seit letzter Woche', 'Seit einem Jahr'], 0, '„last week“ = letzte Woche.'),
      ],
      [
        chunk('How are you?', 'Wie geht es Ihnen?', 'how_are_you', 'How are you?', 'Wie geht es Ihnen?', '', 'Antwort: I’m fine, thank you. And you?'),
        chunk('I moved in last week.', 'Ich bin letzte Woche eingezogen.', 'i_moved', 'I moved in ___', 'Ich bin ___ eingezogen', 'last week', '„last week“ steht ohne in/on.'),
      ],
      { promptDe: 'Die Nachbarin fragt: „How are you?“', model: 'I’m fine, thank you. And you?', expect: ['fine'] },
      q('Welche Antwort passt?', ['I’m fine, thank you.', 'I have fine.'], 0, '„How are you?“ fragt nach dem Befinden.'),
      'Small Talk beginnt oft mit „How are you?“ – die Antwort ist eine Formel, keine Krankenakte.'),
    L('Das Wetter', 'Ich kann über das Wetter sprechen.', 'comparison', 2,
      [
        ['neighbour', 'Lovely weather today, isn’t it?', 'Schönes Wetter heute, nicht wahr?'],
        ['you', 'Yes, it’s much warmer than yesterday.', 'Ja, es ist viel wärmer als gestern.'],
        ['neighbour', 'Are you going for a walk?', 'Gehen Sie spazieren?'],
        ['you', 'Yes, I’m going to the park.', 'Ja, ich gehe in den Park.'],
      ],
      [
        q('Wie ist das Wetter?', ['Schön und wärmer als gestern', 'Kalt und regnerisch'], 0, '„much warmer than yesterday“ = viel wärmer als gestern.'),
        q('Wohin geht die Person?', ['In den Park', 'Zum Arzt'], 0, '„to the park“ = in den Park.'),
      ],
      [
        chunk('It’s much warmer than yesterday.', 'Es ist viel wärmer als gestern.', 'comparative', 'It’s ___ than yesterday', 'Es ist ___ als gestern', 'much warmer', 'Vergleich: Adjektiv + -er + than.'),
        chunk('I’m going to the park.', 'Ich gehe in den Park.', 'going_to_place', 'I’m going to ___', 'Ich gehe ___', 'the park', 'to = Richtung.'),
      ],
      { promptDe: 'Die Nachbarin sagt: „Lovely weather today.“ Stimme zu.', model: 'Yes, it’s lovely, isn’t it?', expect: ['yes', 'lovely'] },
      q('Welches Wort gehört zum Vergleich?', ['warmer than', 'warmer as'], 0, 'Englisch: comparative + than.'),
      'Deutsch „als“ = englisch „than“ – nicht „as“.'),
    L('Kleine Gefälligkeit', 'Ich kann um etwas bitten.', 'modals_politeness', 2,
      [
        ['you', 'Could you help me for a moment?', 'Könnten Sie mir kurz helfen?'],
        ['neighbour', 'Of course. What do you need?', 'Natürlich. Was brauchen Sie?'],
        ['you', 'Could you carry this bag upstairs?', 'Könnten Sie diese Tasche nach oben tragen?'],
        ['neighbour', 'No problem.', 'Kein Problem.'],
      ],
      [
        q('Um was bittet die Person?', ['Um Hilfe beim Tragen', 'Um ein Rezept'], 0, '„carry this bag“ = diese Tasche tragen.'),
        q('Wie reagiert die Nachbarin?', ['Hilfsbereit', 'Ablehnend'], 0, '„No problem.“ = Kein Problem.'),
      ],
      [
        chunk('Could you help me for a moment?', 'Könnten Sie mir kurz helfen?', 'could_you', 'Could you ___?', 'Könnten Sie ___?', 'help me for a moment', 'could ist höflicher als can.'),
        chunk('No problem.', 'Kein Problem.', 'no_problem', 'No problem.', 'Kein Problem.', '', 'Kurze, freundliche Antwort.'),
      ],
      { promptDe: 'Bitte die Nachbarin: „Could you help me for a moment?“', model: 'Could you help me for a moment?', expect: ['help'] },
      q('Welche Bitte ist höflicher?', ['Could you help me?', 'You help me!'], 0, 'could + Grundform ist die höfliche Bitte.'),
      'Höflichkeit entsteht durch could/would + please – nicht durch Lautstärke.'),
  ]),

  S('familie', 'leute', 'Über die Familie', 'Von den Enkeln erzählen', 'Du zeigst Fotos von deiner Familie.', 'family', [
    L('Meine Familie', 'Ich kann sagen, wen ich habe.', 'be_have', 1,
      [
        ['friend', 'Is that your family?', 'Ist das Ihre Familie?'],
        ['you', 'Yes, I have two grandchildren.', 'Ja, ich habe zwei Enkelkinder.'],
        ['friend', 'How old are they?', 'Wie alt sind sie?'],
        ['you', 'The boy is six and the girl is nine.', 'Der Junge ist sechs und das Mädchen ist neun.'],
      ],
      [
        q('Wen hat die Person?', ['Zwei Enkelkinder', 'Zwei Kinder'], 0, '„grandchildren“ = Enkelkinder.'),
        q('Wie alt ist das Mädchen?', ['Neun', 'Sechs'], 0, '„the girl is nine“ = das Mädchen ist neun.'),
      ],
      [
        chunk('I have two grandchildren.', 'Ich habe zwei Enkelkinder.', 'i_have', 'I have ___', 'Ich habe ___', 'two grandchildren', 'grandchild → grandchildren (Plural).'),
        chunk('How old are they?', 'Wie alt sind sie?', 'how_old', 'How old is ___?', 'Wie alt ist ___?', 'she', 'he/she/it → is, they → are.'),
      ],
      { promptDe: 'Frage: „Is that your family?“ Antworte mit deinen Enkeln.', model: 'Yes, I have two grandchildren.', expect: ['grandchildren'] },
      q('Wie lautet der Plural?', ['grandchildren', 'grandchilds'], 0, 'child → children.'),
      'have = haben. Für Besitz und Familie: I have two grandchildren.'),
    L('Was macht sie?', 'Ich kann über Berufe sprechen.', 'present_progressive', 2,
      [
        ['friend', 'What does your daughter do?', 'Was macht Ihre Tochter?'],
        ['you', 'She works in a hospital.', 'Sie arbeitet in einem Krankenhaus.'],
        ['friend', 'Is she a nurse?', 'Ist sie Krankenschwester?'],
        ['you', 'Yes, she is. She likes her job.', 'Ja. Sie mag ihre Arbeit.'],
      ],
      [
        q('Wo arbeitet die Tochter?', ['Im Krankenhaus', 'In der Schule'], 0, '„in a hospital“ = in einem Krankenhaus.'),
        q('Mag sie ihre Arbeit?', ['Ja', 'Nein'], 0, '„She likes her job.“ = Sie mag ihre Arbeit.'),
      ],
      [
        chunk('She works in a hospital.', 'Sie arbeitet in einem Krankenhaus.', 'she_works', 'She works in ___', 'Sie arbeitet in ___', 'a hospital', 'he/she/it + Verb mit -s.'),
        chunk('She likes her job.', 'Sie mag ihre Arbeit.', 'she_likes', 'She likes ___', 'Sie mag ___', 'her job', 'like = mögen (nicht „leiden“).'),
      ],
      { promptDe: 'Frage: „What does your daughter do?“', model: 'She works in a hospital.', expect: ['works', 'hospital'] },
      q('Welche Form ist richtig?', ['She works in a hospital.', 'She work in a hospital.'], 0, 'Bei he/she/it bekommt das Verb ein -s.'),
      'he/she/it + Verb + s – die häufigste Endung im Englischen.'),
    L('Fotos zeigen', 'Ich kann jemanden vorstellen.', 'be_have', 1,
      [
        ['you', 'This is my granddaughter Mia.', 'Das ist meine Enkelin Mia.'],
        ['friend', 'She looks like you.', 'Sie sieht aus wie Sie.'],
        ['you', 'Yes, everyone says that.', 'Ja, das sagen alle.'],
        ['friend', 'She’s lovely.', 'Sie ist bezaubernd.'],
      ],
      [
        q('Wen stellt die Person vor?', ['Ihre Enkelin', 'Ihre Nachbarin'], 0, '„granddaughter“ = Enkelin.'),
        q('Was sagt die Freundin?', ['Sie sieht aus wie du', 'Sie ist Ärztin'], 0, '„She looks like you.“ = Sie sieht aus wie Sie.'),
      ],
      [
        chunk('This is my granddaughter.', 'Das ist meine Enkelin.', 'this_is', 'This is ___', 'Das ist ___', 'my granddaughter', 'this is = das ist (Person vorstellen).'),
        chunk('She looks like you.', 'Sie sieht aus wie Sie.', 'looks_like', 'She looks like ___', 'Sie sieht aus wie ___', 'you', 'look like = aussehen wie.'),
      ],
      { promptDe: 'Stelle deine Enkelin vor.', model: 'This is my granddaughter Mia.', expect: ['this', 'granddaughter'] },
      q('Was heißt „She looks like you“?', ['Sie sieht aus wie du.', 'Sie mag dich.'], 0, 'look like = aussehen wie.'),
      'look like = aussehen wie; like = mögen. Zwei verschiedene Wörter.'),
  ]),

  S('cafe', 'einkaufen', 'Bäckerei & Café', 'Höflich bestellen', 'Du bestellst im Café.', 'cafe', [
    L('Bestellen', 'Ich kann höflich bestellen.', 'modals_politeness', 2,
      [
        ['you', 'Good morning. Could I have a coffee, please?', 'Guten Morgen. Könnte ich einen Kaffee haben, bitte?'],
        ['server', 'Of course. Anything else?', 'Natürlich. Sonst noch etwas?'],
        ['you', 'A croissant, please.', 'Ein Croissant, bitte.'],
        ['server', 'That’s four euros fifty.', 'Das macht vier Euro fünfzig.'],
      ],
      [
        q('Was bestellt die Person?', ['Kaffee und Croissant', 'Tee und Kuchen'], 0, '„a coffee“ und „a croissant“.'),
        q('Wie viel kostet es?', ['4,50 Euro', '5,40 Euro'], 0, '„four euros fifty“ = 4,50 Euro.'),
      ],
      [
        chunk('Could I have a coffee, please?', 'Könnte ich einen Kaffee haben, bitte?', 'could_i_have', 'Could I have ___ , please?', 'Könnte ich ___ haben, bitte?', 'a coffee', 'could I have = höfliche Bestellung.'),
        chunk('Anything else?', 'Sonst noch etwas?', 'anything_else', 'Anything else?', 'Sonst noch etwas?', '', 'Die Bedienung fragt nach weiteren Wünschen.'),
      ],
      { promptDe: 'Die Bedienung fragt: „What can I get you?“', model: 'Could I have a coffee, please?', expect: ['coffee'] },
      q('Welche Bestellung ist höflich?', ['Could I have a coffee, please?', 'Give me a coffee.'], 0, 'could + please ist höflich.'),
      'Die Formel „Could I have …, please?“ funktioniert überall: Café, Apotheke, Hotel.'),
    L('Mitnehmen', 'Ich kann verstehen, was gefragt wird.', 'modals_politeness', 2,
      [
        ['server', 'For here or to go?', 'Zum Hieressen oder zum Mitnehmen?'],
        ['you', 'To go, please.', 'Zum Mitnehmen, bitte.'],
        ['server', 'Would you like a bag?', 'Möchten Sie eine Tüte?'],
        ['you', 'No, thank you.', 'Nein, danke.'],
      ],
      [
        q('Was bedeutet „to go“?', ['Zum Mitnehmen', 'Zum Hieressen'], 0, '„to go“ = zum Mitnehmen.'),
        q('Möchte die Person eine Tüte?', ['Nein', 'Ja'], 0, '„No, thank you.“ = Nein, danke.'),
      ],
      [
        chunk('To go, please.', 'Zum Mitnehmen, bitte.', 'to_go', '___ , please.', '___ , bitte.', 'To go', 'to go = zum Mitnehmen.'),
        chunk('Would you like a bag?', 'Möchten Sie eine Tüte?', 'would_you_like', 'Would you like ___?', 'Möchten Sie ___?', 'a bag', 'would like = möchten.'),
      ],
      { promptDe: 'Die Bedienung fragt: „For here or to go?“', model: 'To go, please.', expect: ['go'] },
      q('Wie sagt man „zum Mitnehmen“?', ['to go', 'to take'], 0, '„To go“ ist die feste Wendung.'),
      'would you like = möchten. Höflicher als „do you want“.'),
    L('Bezahlen', 'Ich kann nach der Rechnung fragen.', 'modals_politeness', 2,
      [
        ['you', 'Could I have the bill, please?', 'Könnte ich die Rechnung haben, bitte?'],
        ['server', 'Sure. Cash or card?', 'Klar. Bar oder Karte?'],
        ['you', 'Card, please.', 'Mit Karte, bitte.'],
        ['server', 'Here you are.', 'Bitte schön.'],
      ],
      [
        q('Was möchte die Person?', ['Die Rechnung', 'Noch einen Kaffee'], 0, '„the bill“ = die Rechnung.'),
        q('Wie bezahlt sie?', ['Mit Karte', 'Bar'], 0, '„Card, please.“ = mit Karte.'),
      ],
      [
        chunk('Could I have the bill, please?', 'Könnte ich die Rechnung haben, bitte?', 'could_i_have', 'Could I have ___ , please?', 'Könnte ich ___ haben, bitte?', 'the bill', 'bill = Rechnung (US: check).'),
        chunk('Cash or card?', 'Bar oder Karte?', 'cash_or_card', 'Cash or card?', 'Bar oder Karte?', '', 'Die übliche Frage an der Kasse.'),
      ],
      { promptDe: 'Frage nach der Rechnung.', model: 'Could I have the bill, please?', expect: ['bill'] },
      q('Was heißt „bill“ hier?', ['Rechnung', 'Geldschein'], 0, 'Im Café: bill = Rechnung.'),
      'the bill = die Rechnung. „Here you are“ = bitte schön.'),
  ]),

  S('supermarkt', 'einkaufen', 'Im Supermarkt', 'Finden, fragen, bezahlen', 'Du kaufst für die Woche ein.', 'basket', [
    L('Wo finde ich …?', 'Ich kann nach einem Produkt fragen.', 'prepositions_space', 2,
      [
        ['you', 'Excuse me, where can I find the milk?', 'Entschuldigung, wo finde ich die Milch?'],
        ['staff', 'It’s in aisle three, on the left.', 'Das ist in Gang drei, links.'],
        ['you', 'Thank you. And where is the bread?', 'Danke. Und wo ist das Brot?'],
        ['staff', 'Next to the bakery.', 'Neben der Bäckerei.'],
      ],
      [
        q('Was sucht die Person zuerst?', ['Milch', 'Brot'], 0, '„the milk“ = die Milch.'),
        q('Wo ist die Milch?', ['Gang drei, links', 'Neben der Kasse'], 0, '„in aisle three, on the left“.'),
      ],
      [
        chunk('Where can I find the milk?', 'Wo finde ich die Milch?', 'where_can_i_find', 'Where can I find ___?', 'Wo finde ich ___?', 'the milk', 'where + can + I + find.'),
        chunk('It’s on the left.', 'Es ist links.', 'on_the_left', 'It’s on the ___', 'Es ist ___', 'left', 'on the left / on the right.'),
      ],
      { promptDe: 'Frage nach dem Brot.', model: 'Where can I find the bread?', expect: ['bread'] },
      q('Welche Frage ist richtig?', ['Where can I find the milk?', 'Where I can find the milk?'], 0, 'In Fragen: where + can + ich.'),
      'Präpositionen als Raumlogik: in (drinnen), on (auf), at (an einem Punkt).'),
    L('Mengen & Preise', 'Ich kann Mengen und Preise verstehen.', 'articles_quantity', 2,
      [
        ['you', 'How much are these apples?', 'Wie viel kosten diese Äpfel?'],
        ['staff', 'Two euros fifty a kilo.', 'Zwei Euro fünfzig pro Kilo.'],
        ['you', 'I’d like one kilo, please.', 'Ich hätte gern ein Kilo, bitte.'],
        ['staff', 'Anything else?', 'Sonst noch etwas?'],
      ],
      [
        q('Was kosten die Äpfel pro Kilo?', ['2,50 Euro', '5,20 Euro'], 0, '„two euros fifty“ = 2,50 Euro.'),
        q('Wie viel kauft die Person?', ['Ein Kilo', 'Zwei Kilo'], 0, '„one kilo“ = ein Kilo.'),
      ],
      [
        chunk('How much are these apples?', 'Wie viel kosten diese Äpfel?', 'how_much_are', 'How much are ___?', 'Wie viel kosten ___?', 'these apples', 'how much + are bei Plural.'),
        chunk('I’d like one kilo, please.', 'Ich hätte gern ein Kilo, bitte.', 'id_like', 'I’d like ___ , please.', 'Ich hätte gern ___ , bitte.', 'one kilo', 'I’d like = I would like (höflich).'),
      ],
      { promptDe: 'Frage nach dem Preis der Äpfel.', model: 'How much are these apples?', expect: ['how', 'much'] },
      q('Was heißt „a kilo“?', ['ein Kilo', 'ein Liter'], 0, 'kilo = Kilogramm.'),
      'much = unzählbar (money, water), many = zählbar (apples, people).'),
    L('An der Kasse', 'Ich kann an der Kasse reagieren.', 'negation_short_answers', 2,
      [
        ['cashier', 'Do you have a bag?', 'Haben Sie eine Tüte?'],
        ['you', 'No, I don’t. Can I have one?', 'Nein. Kann ich eine haben?'],
        ['cashier', 'That’s one euro.', 'Das macht einen Euro.'],
        ['you', 'Here you are.', 'Bitte schön.'],
      ],
      [
        q('Was fragt die Kassiererin?', ['Ob du eine Tüte hast', 'Ob du bar zahlst'], 0, '„a bag“ = eine Tüte.'),
        q('Was kostet die Tüte?', ['Ein Euro', 'Zwei Euro'], 0, '„one euro“ = ein Euro.'),
      ],
      [
        chunk('Can I have one?', 'Kann ich eine haben?', 'can_i_have', 'Can I have ___?', 'Kann ich ___ haben?', 'one', 'one ersetzt „a bag“.'),
        chunk('Here you are.', 'Bitte schön.', 'here_you_are', 'Here you are.', 'Bitte schön.', '', 'Beim Geben: Here you are.'),
      ],
      { promptDe: 'Die Kassiererin fragt: „Do you have a bag?“', model: 'No, I don’t. Can I have one?', expect: ['can', 'have'] },
      q('Was ist die richtige Kurzantwort?', ['No, I don’t.', 'No, I have not.'], 0, 'Kurzantwort mit do: No, I don’t.'),
      'Kurzantworten: Yes, I do. / No, I don’t. – nicht „Yes, I have.“'),
  ]),
];
