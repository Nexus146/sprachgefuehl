import { S, L, q, chunk } from './lib.mjs';

/* Staffel 2 – Frühling und Sommer. Jede Lektion nimmt mindestens zwei Bausteine
   aus Staffel 1 wieder auf und führt höchstens zwei neue ein. */

export const situations3 = [
  S('restaurant', 'einkaufen', 'Im Restaurant', 'Zu zweit essen gehen', 'Lena kommt zu Besuch, Sam hat das Restaurant empfohlen.', 'cafe', [
    L('Ein Tisch für zwei', 'Ich kann einen Tisch reservieren und höflich fragen.', 'modals_politeness', 1,
      [
        ['you', 'Good evening. A table for two, please.', 'Guten Abend. Einen Tisch für zwei, bitte.'],
        ['waiter', 'Of course. Would you like to sit by the window?', 'Natürlich. Möchten Sie am Fenster sitzen?'],
        ['you', 'Yes, please. Could we see the menu?', 'Ja, gern. Könnten wir die Karte sehen?'],
        ['waiter', 'Here you are.', 'Bitte schön.'],
      ],
      [
        q('Was möchte die Person?', ['Einen Tisch für zwei', 'Die Rechnung'], 0, '„A table for two“ = ein Tisch für zwei Personen.'),
        q('Wo soll sie sitzen?', ['Am Fenster', 'Draußen'], 0, '„by the window“ = am Fenster.'),
      ],
      [
        chunk('A table for two, please.', 'Einen Tisch für zwei, bitte.', 'table_for_two', 'A table for ___', 'Einen Tisch für ___', 'two', 'for two = für zwei Personen.'),
        chunk('Could we see the menu?', 'Könnten wir die Karte sehen?', 'could_we_menu', 'Could we ___?', 'Könnten wir ___?', 'see the menu', 'Nach could steht die Grundform, kein to.'),
      ],
      { promptDe: 'Der Kellner fragt: „How many?“ – Sag: einen Tisch für zwei.', model: 'A table for two, please.', expect: ['table', 'two'] },
      q('Welche Bitte ist höflich?', ['Could we see the menu, please?', 'Give me the menu.'], 0, 'could + please macht aus einer Forderung eine Bitte.'),
      'could macht die Bitte höflich; please steht am Ende.',
      ['cafe_l1_c1', 'cafe_l3_c2']),

    L('Was empfehlen Sie?', 'Ich kann nach einer Empfehlung fragen und bestellen.', 'word_order_question', 2,
      [
        ['waiter', 'Are you ready to order?', 'Möchten Sie bestellen?'],
        ['you', 'What do you recommend?', 'Was empfehlen Sie?'],
        ['waiter', 'The soup is very good today.', 'Die Suppe ist heute sehr gut.'],
        ['you', 'Then I’ll have the soup, please.', 'Dann nehme ich die Suppe, bitte.'],
      ],
      [
        q('Was fragt die Person?', ['Nach einer Empfehlung', 'Nach der Toilette'], 0, '„recommend“ = empfehlen.'),
        q('Was bestellt sie?', ['Die Suppe', 'Den Kaffee'], 0, '„I’ll have the soup“ = ich nehme die Suppe.'),
      ],
      [
        chunk('What do you recommend?', 'Was empfehlen Sie?', 'what_recommend', 'What do you ___?', 'Was ___ Sie?', 'recommend', 'Frage mit do, Verb in der Grundform.'),
        chunk('I’ll have the soup, please.', 'Ich nehme die Suppe, bitte.', 'ill_have', 'I’ll have ___', 'Ich nehme ___', 'the soup', '„I’ll have …“ ist die natürliche Bestellung.'),
      ],
      { promptDe: 'Frag den Kellner nach einer Empfehlung.', model: 'What do you recommend?', expect: ['recommend'] },
      q('Welche Frage ist richtig?', ['What do you recommend?', 'What you recommend?'], 0, 'Fragen brauchen do.'),
      'Fragen mit do: do + Person + Grundform. „I’ll have …“ ist die Bestellung.',
      ['supermarkt_l2_c2', 'cafe_l1_c2']),

    L('Die Rechnung', 'Ich kann bezahlen und mich freundlich verabschieden.', 'past_simple', 1,
      [
        ['you', 'Could we have the bill, please?', 'Könnten wir die Rechnung haben?'],
        ['waiter', 'Of course. Together or separate?', 'Natürlich. Zusammen oder getrennt?'],
        ['you', 'Together, please. Everything was delicious.', 'Zusammen, bitte. Alles war köstlich.'],
        ['waiter', 'Thank you very much.', 'Vielen Dank.'],
      ],
      [
        q('Wer bezahlt?', ['Zusammen', 'Getrennt'], 0, '„Together“ = zusammen.'),
        q('Wie war das Essen?', ['Köstlich', 'Zu kalt'], 0, '„delicious“ = köstlich.'),
      ],
      [
        chunk('Could we have the bill, please?', 'Könnten wir die Rechnung haben?', 'could_we_bill', 'Could we have ___?', 'Könnten wir ___ haben?', 'the bill', 'bill = Rechnung, nicht „invoice“ im Restaurant.'),
        chunk('Everything was delicious.', 'Alles war köstlich.', 'was_delicious', 'Everything was ___', 'Alles war ___', 'delicious', 'was = Vergangenheit von is.'),
      ],
      { promptDe: 'Sag dem Kellner, dass alles lecker war.', model: 'Everything was delicious.', expect: ['delicious'] },
      q('Welche Zeitform ist „was“?', ['Vergangenheit', 'Zukunft'], 0, 'was ist die Vergangenheit von is.'),
      'Für das Lob nach dem Essen reicht ein kurzer Satz in der Vergangenheit.',
      ['supermarkt_l3_c2', 'nachbarn_l1_c1']),
  ]),

  S('markt', 'einkaufen', 'Auf dem Markt', 'Fragen, vergleichen, kaufen', 'Hana zeigt dir den Samstagsmarkt und die besten Stände.', 'basket', [
    L('Was kostet das?', 'Ich kann nach Preisen fragen und etwas kaufen.', 'articles_quantity', 1,
      [
        ['hana', 'Look, the tomatoes are lovely today.', 'Schau, die Tomaten sind heute wunderbar.'],
        ['you', 'How much are they?', 'Wie viel kosten sie?'],
        ['hana', 'Three euros a kilo.', 'Drei Euro pro Kilo.'],
        ['you', 'I’ll take one kilo, please.', 'Ich nehme ein Kilo, bitte.'],
      ],
      [
        q('Was kauft die Person?', ['Tomaten', 'Äpfel'], 0, '„tomatoes“ = Tomaten.'),
        q('Wie viel kostet ein Kilo?', ['Drei Euro', 'Zwei Euro'], 0, '„three euros a kilo“ = drei Euro pro Kilo.'),
      ],
      [
        chunk('How much are they?', 'Wie viel kosten sie?', 'how_much_are', 'How much are ___?', 'Wie viel kosten ___?', 'they', 'Plural → are, nicht is.'),
        chunk('I’ll take one kilo, please.', 'Ich nehme ein Kilo, bitte.', 'ill_take', 'I’ll take ___', 'Ich nehme ___', 'one kilo', '„I’ll take …“ = ich nehme (beim Einkaufen).'),
      ],
      { promptDe: 'Frag Hana, wie viel die Tomaten kosten.', model: 'How much are they?', expect: ['how', 'much'] },
      q('Welcher Satz ist richtig?', ['How much are the tomatoes?', 'How much is the tomatoes?'], 0, 'Plural → are.'),
      'much gehört zu Unzählbarem, many zu Zählbarem – bei Preisen reicht „How much …?“.',
      ['supermarkt_l2_c1', 'supermarkt_l2_c2']),

    L('Die sind süßer', 'Ich kann Dinge vergleichen.', 'comparison', 2,
      [
        ['hana', 'These apples are sweeter than the green ones.', 'Diese Äpfel sind süßer als die grünen.'],
        ['you', 'Are they from the region?', 'Sind sie aus der Region?'],
        ['hana', 'Yes, from a farm outside town.', 'Ja, von einem Hof vor der Stadt.'],
        ['you', 'Then I’ll take two kilos.', 'Dann nehme ich zwei Kilo.'],
      ],
      [
        q('Welche Äpfel sind süßer?', ['Diese hier', 'Die grünen'], 0, '„sweeter than the green ones“ = süßer als die grünen.'),
        q('Woher kommen sie?', ['Von einem Hof vor der Stadt', 'Aus dem Ausland'], 0, '„from a farm outside town“ = von einem Hof vor der Stadt.'),
      ],
      [
        chunk('These apples are sweeter than the green ones.', 'Diese Äpfel sind süßer als die grünen.', 'comparative_than', 'These apples are ___ than the green ones', 'Diese Äpfel sind ___ als die grünen', 'sweeter', 'Komparativ + than, nicht as.'),
        chunk('They are from the region.', 'Sie sind aus der Region.', 'from_region', 'They are from ___', 'Sie sind aus ___', 'the region', 'from = aus einer Herkunft.'),
      ],
      { promptDe: 'Sag, dass die Äpfel süßer sind als die grünen.', model: 'These apples are sweeter than the green ones.', expect: ['sweeter', 'than'] },
      q('Wie vergleicht man?', ['sweeter than', 'sweeter as'], 0, 'Komparativ + than.'),
      'Kurze Adjektive bekommen -er, längere more: sweeter, more expensive.',
      ['supermarkt_l2_c1', 'nachbarn_l2_c1']),

    L('Marmelade für alle', 'Ich kann über Pläne sprechen.', 'future', 3,
      [
        ['hana', 'The strawberries are cheap today.', 'Die Erdbeeren sind heute günstig.'],
        ['you', 'I’m going to make jam.', 'Ich werde Marmelade machen.'],
        ['hana', 'Then take two boxes.', 'Dann nimm zwei Kisten.'],
        ['you', 'Good idea. I’ll bring you a glass.', 'Gute Idee. Ich bringe dir ein Glas mit.'],
      ],
      [
        q('Was will die Person machen?', ['Marmelade', 'Saft'], 0, '„jam“ = Marmelade.'),
        q('Was bekommt Hana?', ['Ein Glas Marmelade', 'Zwei Kisten Erdbeeren'], 0, '„I’ll bring you a glass“ = ich bringe dir ein Glas.'),
      ],
      [
        chunk('I’m going to make jam.', 'Ich werde Marmelade machen.', 'going_to', 'I’m going to ___', 'Ich werde ___', 'make jam', 'going to = Plan oder Absicht.'),
        chunk('I’ll bring you a glass.', 'Ich bringe dir ein Glas mit.', 'ill_bring', 'I’ll bring you ___', 'Ich bringe dir ___', 'a glass', 'will = spontane Zusage.'),
      ],
      { promptDe: 'Sag Hana, dass du Marmelade machen willst.', model: 'I’m going to make jam.', expect: ['jam'] },
      q('Welcher Satz ist ein Plan?', ['I’m going to make jam.', 'I make jam yesterday.'], 0, 'going to drückt einen Plan aus.'),
      'going to für Pläne, will für spontane Entscheidungen und Versprechen.',
      ['nachbarn_l2_c2', 'familie_l1_c1']),
  ]),

  S('flughafen', 'unterwegs', 'Am Flughafen', 'Einchecken und durch die Kontrolle', 'Lena und du fliegen zusammen – der erste Flug seit Jahren.', 'map', [
    L('Einchecken', 'Ich kann nach dem Schalter fragen und um Hilfe bitten.', 'modals_politeness', 1,
      [
        ['lena', 'Our flight leaves at six.', 'Unser Flug geht um sechs.'],
        ['you', 'Where do we check in?', 'Wo checken wir ein?'],
        ['lena', 'Desk twelve, over there.', 'Schalter zwölf, dort drüben.'],
        ['you', 'Could you help me with this bag, please?', 'Könntest du mir mit dieser Tasche helfen?'],
      ],
      [
        q('Wann geht der Flug?', ['Um sechs', 'Um zwölf'], 0, '„at six“ = um sechs.'),
        q('Wo ist der Schalter?', ['Dort drüben', 'Im ersten Stock'], 0, '„over there“ = dort drüben.'),
      ],
      [
        chunk('Where do we check in?', 'Wo checken wir ein?', 'where_check_in', 'Where do we ___?', 'Wo ___ wir?', 'check in', 'check in = einchecken.'),
        chunk('Could you help me with this bag?', 'Könntest du mir mit dieser Tasche helfen?', 'could_you_help', 'Could you help me with ___?', 'Könntest du mir mit ___ helfen?', 'this bag', 'help someone with something.'),
      ],
      { promptDe: 'Bitte Lena um Hilfe mit der Tasche.', model: 'Could you help me with this bag, please?', expect: ['help', 'bag'] },
      q('Welche Frage ist richtig?', ['Where do we check in?', 'Where we check in?'], 0, 'Fragen brauchen do.'),
      'Höfliche Bitten an Bekannte: Could you …?, bitte am Ende.',
      ['weg_l1_c1', 'hilfe_l1_c1']),

    L('Durch die Kontrolle', 'Ich kann Anweisungen verstehen und nachfragen.', 'prepositions_space', 2,
      [
        ['officer', 'Please put your bag on the belt.', 'Bitte legen Sie Ihre Tasche auf das Band.'],
        ['you', 'Do I have to take my shoes off?', 'Muss ich die Schuhe ausziehen?'],
        ['officer', 'No, you can keep them on.', 'Nein, Sie können sie anlassen.'],
        ['you', 'Thank you. That’s good to know.', 'Danke. Gut zu wissen.'],
      ],
      [
        q('Wohin soll die Tasche?', ['Auf das Band', 'In den Korb'], 0, '„on the belt“ = auf das Band.'),
        q('Muss die Person die Schuhe ausziehen?', ['Nein', 'Ja'], 0, '„you can keep them on“ = Sie können sie anlassen.'),
      ],
      [
        chunk('Please put your bag on the belt.', 'Bitte legen Sie Ihre Tasche auf das Band.', 'put_on_belt', 'Please put your bag ___ the belt', 'Bitte legen Sie Ihre Tasche ___ das Band', 'on', 'on = auf einer Fläche.'),
        chunk('Do I have to take my shoes off?', 'Muss ich die Schuhe ausziehen?', 'have_to_shoes', 'Do I have to ___?', 'Muss ich ___?', 'take my shoes off', 'have to = müssen.'),
      ],
      { promptDe: 'Frag, ob du die Schuhe ausziehen musst.', model: 'Do I have to take my shoes off?', expect: ['have', 'shoes'] },
      q('Welche Präposition passt?', ['on the belt', 'in the belt'], 0, 'on = auf einer Fläche.'),
      'on für Flächen, in für Behälter: on the belt, in the box.',
      ['bus_l3_c1', 'weg_l2_c1']),

    L('Im Flugzeug', 'Ich kann höflich um etwas bitten und Gefühle ausdrücken.', 'negation_short_answers', 2,
      [
        ['attendant', 'Would you like some water?', 'Möchten Sie etwas Wasser?'],
        ['you', 'Yes, please. And could I have a blanket?', 'Ja, bitte. Und könnte ich eine Decke haben?'],
        ['attendant', 'Of course.', 'Natürlich.'],
        ['you', 'Thank you. I’m a little nervous.', 'Danke. Ich bin ein wenig nervös.'],
      ],
      [
        q('Was möchte die Person?', ['Wasser und eine Decke', 'Einen Kaffee'], 0, '„water“ und „blanket“ = Wasser und Decke.'),
        q('Wie fühlt sie sich?', ['Ein wenig nervös', 'Sehr müde'], 0, '„a little nervous“ = ein wenig nervös.'),
      ],
      [
        chunk('Could I have a blanket, please?', 'Könnte ich eine Decke haben, bitte?', 'could_i_have_blanket', 'Could I have ___?', 'Könnte ich ___ haben?', 'a blanket', 'a blanket = eine Decke.'),
        chunk('I’m a little nervous.', 'Ich bin ein wenig nervös.', 'a_little_nervous', 'I’m a little ___', 'Ich bin ein wenig ___', 'nervous', 'a little = ein wenig.'),
      ],
      { promptDe: 'Bitte höflich um eine Decke.', model: 'Could I have a blanket, please?', expect: ['blanket'] },
      q('Welche Antwort ist höflich?', ['Yes, please.', 'Yes.'], 0, 'please macht die Kurzantwort freundlich.'),
      'Gefühle gehören zum Gespräch: „I’m a little nervous“ ist ehrlich und öffnet Hilfe.',
      ['cafe_l1_c1', 'arzt_l3_c2']),
  ]),

  S('hotel', 'unterwegs', 'Im Hotel', 'Ankommen, Frühstück, auschecken', 'Am Ziel angekommen, braucht ihr ein Zimmer mit zwei Betten.', 'map', [
    L('Ein Zimmer für zwei', 'Ich kann einchecken und sagen, was ich reserviert habe.', 'articles_quantity', 1,
      [
        ['you', 'Good evening. We have a reservation for two nights.', 'Guten Abend. Wir haben für zwei Nächte reserviert.'],
        ['receptionist', 'What’s your name, please?', 'Wie ist Ihr Name, bitte?'],
        ['you', 'It’s under Weber.', 'Er läuft auf Weber.'],
        ['receptionist', 'Here are your keys. Room 214.', 'Hier sind Ihre Schlüssel. Zimmer 214.'],
      ],
      [
        q('Wie lange bleibt die Person?', ['Zwei Nächte', 'Eine Woche'], 0, '„for two nights“ = für zwei Nächte.'),
        q('Welches Zimmer?', ['214', '241'], 0, '„Room 214“ = Zimmer 214.'),
      ],
      [
        chunk('We have a reservation for two nights.', 'Wir haben für zwei Nächte reserviert.', 'reservation_nights', 'We have a reservation for ___', 'Wir haben für ___ reserviert', 'two nights', 'for + Zeitraum beschreibt die Dauer.'),
        chunk('Here are your keys.', 'Hier sind Ihre Schlüssel.', 'here_are_keys', 'Here are your ___', 'Hier sind Ihre ___', 'keys', 'Plural → here are.'),
      ],
      { promptDe: 'Sag, dass ihr für zwei Nächte reserviert habt.', model: 'We have a reservation for two nights.', expect: ['reservation', 'nights'] },
      q('Welcher Satz ist richtig?', ['a reservation', 'reservation'], 0, 'reservation ist zählbar → a reservation.'),
      'Nomen mit a/an, wenn sie zählbar sind: a reservation, a key, a room.',
      ['cafe_l1_c2', 'telefon_l2_c1']),

    L('Frühstück um sieben', 'Ich kann nach Zeiten und Orten fragen.', 'prepositions_space', 2,
      [
        ['you', 'What time is breakfast?', 'Wann gibt es Frühstück?'],
        ['receptionist', 'From seven to ten, on the ground floor.', 'Von sieben bis zehn, im Erdgeschoss.'],
        ['you', 'Is there a lift?', 'Gibt es einen Aufzug?'],
        ['receptionist', 'Yes, next to the stairs.', 'Ja, neben der Treppe.'],
      ],
      [
        q('Wann gibt es Frühstück?', ['Von sieben bis zehn', 'Von acht bis elf'], 0, '„from seven to ten“ = von sieben bis zehn.'),
        q('Wo ist der Aufzug?', ['Neben der Treppe', 'Im ersten Stock'], 0, '„next to the stairs“ = neben der Treppe.'),
      ],
      [
        chunk('What time is breakfast?', 'Wann gibt es Frühstück?', 'what_time_breakfast', 'What time is ___?', 'Wann ist ___?', 'breakfast', 'What time …? fragt nach der Uhrzeit.'),
        chunk('It’s on the ground floor.', 'Es ist im Erdgeschoss.', 'ground_floor', 'It’s on the ___ floor', 'Es ist im ___', 'ground', 'ground floor = Erdgeschoss.'),
      ],
      { promptDe: 'Frag, wann das Frühstück ist.', model: 'What time is breakfast?', expect: ['time', 'breakfast'] },
      q('Welche Präposition passt?', ['at seven', 'on seven'], 0, 'at + Uhrzeit.'),
      'at für Uhrzeiten, on für Tage, in für Monate: at seven, on Monday, in May.',
      ['bus_l2_c2', 'weg_l2_c1']),

    L('Auschecken', 'Ich kann auschecken und sagen, wie es war.', 'past_simple', 3,
      [
        ['you', 'We’re leaving today. Could we have the bill?', 'Wir reisen heute ab. Könnten wir die Rechnung haben?'],
        ['receptionist', 'Of course. Did you enjoy your stay?', 'Natürlich. Hat Ihnen der Aufenthalt gefallen?'],
        ['you', 'Yes, we did. The room was very quiet.', 'Ja. Das Zimmer war sehr ruhig.'],
        ['receptionist', 'I’m glad to hear that.', 'Das freut mich.'],
      ],
      [
        q('Was fragt die Rezeption?', ['Ob der Aufenthalt gefallen hat', 'Ob sie bleiben wollen'], 0, '„Did you enjoy your stay?“ = Hat es Ihnen gefallen?'),
        q('Wie war das Zimmer?', ['Sehr ruhig', 'Sehr laut'], 0, '„quiet“ = ruhig.'),
      ],
      [
        chunk('Did you enjoy your stay?', 'Hat Ihnen der Aufenthalt gefallen?', 'did_enjoy', 'Did you enjoy ___?', 'Hat Ihnen ___ gefallen?', 'your stay', 'Fragen in der Vergangenheit: did + Grundform.'),
        chunk('The room was very quiet.', 'Das Zimmer war sehr ruhig.', 'room_quiet', 'The room was very ___', 'Das Zimmer war sehr ___', 'quiet', 'quiet = ruhig, nicht „still“.'),
      ],
      { promptDe: 'Sag, dass das Zimmer sehr ruhig war.', model: 'The room was very quiet.', expect: ['quiet'] },
      q('Wie lautet die Vergangenheit von enjoy?', ['enjoyed', 'enjoys'], 0, 'Regelmäßige Verben bekommen -ed.'),
      'Nach did steht die Grundform: Did you enjoy …?, nicht „Did you enjoyed“.',
      ['cafe_l3_c1', 'missverstaendnis_l2_c2']),
  ]),

  S('fahrkartenautomat', 'unterwegs', 'Am Fahrkartenautomaten', 'Ticket ziehen, ohne Hilfe klarkommen', 'Herr Krüger steht hilflos am Automaten, und du hilfst.', 'bus', [
    L('Der Automat', 'Ich kann fragen, was auf dem Bildschirm steht.', 'word_order_question', 1,
      [
        ['krueger', 'I can’t get a ticket.', 'Ich bekomme kein Ticket.'],
        ['you', 'What does the screen say?', 'Was steht auf dem Bildschirm?'],
        ['krueger', 'It asks for a card.', 'Es fragt nach einer Karte.'],
        ['you', 'Do you have coins?', 'Haben Sie Münzen?'],
      ],
      [
        q('Was will Herr Krüger?', ['Ein Ticket', 'Ein Taxi'], 0, '„ticket“ = Fahrkarte.'),
        q('Was verlangt der Automat?', ['Eine Karte', 'Münzen'], 0, '„It asks for a card“ = es fragt nach einer Karte.'),
      ],
      [
        chunk('What does the screen say?', 'Was steht auf dem Bildschirm?', 'screen_say', 'What does the screen ___?', 'Was ___ der Bildschirm?', 'say', 'does + Grundform in der Frage.'),
        chunk('Do you have coins?', 'Haben Sie Münzen?', 'do_you_have_coins', 'Do you have ___?', 'Haben Sie ___?', 'coins', 'coins = Münzen.'),
      ],
      { promptDe: 'Frag Herrn Krüger, ob er Münzen hat.', model: 'Do you have coins?', expect: ['coins'] },
      q('Welche Frage ist richtig?', ['What does the screen say?', 'What the screen says?'], 0, 'does + Grundform, Hilfsverb zuerst.'),
      'Dritte Person: does + Grundform – does the screen say, nicht „says“.',
      ['bus_l1_c2', 'supermarkt_l1_c1']),

    L('Hin und zurück', 'Ich kann eine Fahrkarte kaufen und einen Tag nennen.', 'negation_short_answers', 2,
      [
        ['you', 'A ticket to the airport, please.', 'Eine Fahrkarte zum Flughafen, bitte.'],
        ['krueger', 'Single or return?', 'Einfach oder hin und zurück?'],
        ['you', 'Return, please. We’re coming back on Sunday.', 'Hin und zurück, bitte. Wir kommen am Sonntag zurück.'],
        ['krueger', 'Then press this button.', 'Dann drücken Sie diese Taste.'],
      ],
      [
        q('Wohin fährt die Person?', ['Zum Flughafen', 'Zum Bahnhof'], 0, '„to the airport“ = zum Flughafen.'),
        q('Wann kommt sie zurück?', ['Am Sonntag', 'Am Samstag'], 0, '„on Sunday“ = am Sonntag.'),
      ],
      [
        chunk('A ticket to the airport, please.', 'Eine Fahrkarte zum Flughafen, bitte.', 'ticket_to', 'A ticket to ___, please', 'Eine Fahrkarte nach ___, bitte', 'the airport', 'to = Richtung oder Ziel.'),
        chunk('We’re coming back on Sunday.', 'Wir kommen am Sonntag zurück.', 'coming_back_sunday', 'We’re coming back on ___', 'Wir kommen am ___ zurück', 'Sunday', 'on + Wochentag.'),
      ],
      { promptDe: 'Sag, dass ihr am Sonntag zurückkommt.', model: 'We’re coming back on Sunday.', expect: ['Sunday'] },
      q('Welche Präposition passt?', ['on Sunday', 'in Sunday'], 0, 'on + Wochentag.'),
      'on + Wochentag, at + Uhrzeit, in + Monat oder Jahr.',
      ['bus_l2_c1', 'telefon_l2_c1']),

    L('Der Automat nimmt keine Scheine', 'Ich kann Verneinungen verstehen und Geld zurückgeben.', 'negation_short_answers', 4,
      [
        ['krueger', 'It doesn’t take notes.', 'Er nimmt keine Scheine.'],
        ['you', 'Then we need coins.', 'Dann brauchen wir Münzen.'],
        ['krueger', 'I have some.', 'Ich habe welche.'],
        ['you', 'Perfect. I’ll pay you back.', 'Perfekt. Ich zahle es dir zurück.'],
      ],
      [
        q('Was nimmt der Automat nicht?', ['Scheine', 'Münzen'], 0, '„notes“ = Geldscheine.'),
        q('Was will die Person tun?', ['Zurückzahlen', 'Noch ein Ticket kaufen'], 0, '„I’ll pay you back“ = ich zahle es dir zurück.'),
      ],
      [
        chunk('It doesn’t take notes.', 'Er nimmt keine Scheine.', 'doesnt_take_notes', 'It doesn’t take ___', 'Er nimmt keine ___', 'notes', 'doesn’t + Grundform.'),
        chunk('I’ll pay you back.', 'Ich zahle es dir zurück.', 'pay_back', 'I’ll pay you ___', 'Ich zahle es dir ___', 'back', 'pay back = zurückzahlen.'),
      ],
      { promptDe: 'Sag, dass der Automat keine Scheine nimmt.', model: 'It doesn’t take notes.', expect: ['notes'] },
      q('Welche Verneinung ist richtig?', ['It doesn’t take notes.', 'It not takes notes.'], 0, 'doesn’t + Grundform.'),
      'Verneinung mit don’t/doesn’t + Grundform; das -s wandert in das doesn’t.',
      ['supermarkt_l3_c1', 'hilfe_l2_c2']),
  ]),

  S('bank', 'alltag', 'Bei der Bank', 'Konto, Karte, Geld abheben', 'Frau Peters erklärt dir das Formular für die neue Karte.', 'help', [
    L('Ein Konto eröffnen', 'Ich kann höflich sagen, was ich möchte.', 'conditionals_would_like', 1,
      [
        ['you', 'Good morning. I’d like to open an account.', 'Guten Morgen. Ich möchte ein Konto eröffnen.'],
        ['peters', 'Do you have your ID with you?', 'Haben Sie Ihren Ausweis dabei?'],
        ['you', 'Yes, here it is.', 'Ja, hier bitte.'],
        ['peters', 'Thank you. Please fill in this form.', 'Danke. Bitte füllen Sie dieses Formular aus.'],
      ],
      [
        q('Was möchte die Person?', ['Ein Konto eröffnen', 'Geld abheben'], 0, '„open an account“ = ein Konto eröffnen.'),
        q('Was braucht sie?', ['Ihren Ausweis', 'Ihre Karte'], 0, '„ID“ = Ausweis.'),
      ],
      [
        chunk('I’d like to open an account.', 'Ich möchte ein Konto eröffnen.', 'would_like_account', 'I’d like to ___', 'Ich möchte ___', 'open an account', 'would like = möchte, höflicher als want.'),
        chunk('Please fill in this form.', 'Bitte füllen Sie dieses Formular aus.', 'fill_in_form', 'Please fill in ___', 'Bitte füllen Sie ___ aus', 'this form', 'fill in = ausfüllen.'),
      ],
      { promptDe: 'Sag höflich, dass du ein Konto eröffnen möchtest.', model: 'I’d like to open an account.', expect: ['account'] },
      q('Welcher Satz ist höflich?', ['I’d like to open an account.', 'I want account.'], 0, 'would like = möchte.'),
      'would like to + Grundform ist die höfliche Form von want.',
      ['apotheke_l1_c2', 'supermarkt_l1_c2']),

    L('Die neue Karte', 'Ich kann über die Zukunft sprechen.', 'future', 2,
      [
        ['peters', 'Your card will arrive next week.', 'Ihre Karte kommt nächste Woche.'],
        ['you', 'Will I get a PIN separately?', 'Bekomme ich die PIN separat?'],
        ['peters', 'Yes, by post.', 'Ja, mit der Post.'],
        ['you', 'Good. Thank you.', 'Gut. Danke.'],
      ],
      [
        q('Wann kommt die Karte?', ['Nächste Woche', 'Morgen'], 0, '„next week“ = nächste Woche.'),
        q('Wie kommt die PIN?', ['Mit der Post', 'Per E-Mail'], 0, '„by post“ = mit der Post.'),
      ],
      [
        chunk('Your card will arrive next week.', 'Ihre Karte kommt nächste Woche.', 'will_arrive', 'Your card will arrive ___', 'Ihre Karte kommt ___', 'next week', 'will + Grundform für die Zukunft.'),
        chunk('Will I get a PIN separately?', 'Bekomme ich die PIN separat?', 'will_get_pin', 'Will I get ___ separately?', 'Bekomme ich ___ separat?', 'a PIN', 'Frage mit will: will + Person + Grundform.'),
      ],
      { promptDe: 'Frag, ob du die PIN separat bekommst.', model: 'Will I get a PIN separately?', expect: ['PIN'] },
      q('Welcher Satz ist Zukunft?', ['will arrive', 'arrived'], 0, 'will + Grundform.'),
      'will für Vorhersagen und Zusagen; going to für Pläne.',
      ['hilfe_l2_c2', 'bus_l2_c2']),

    L('Geld abheben', 'Ich kann nach Orten fragen und Wege verstehen.', 'prepositions_space', 3,
      [
        ['you', 'Where is the cash machine?', 'Wo ist der Geldautomat?'],
        ['peters', 'Just outside, next to the door.', 'Gleich draußen, neben der Tür.'],
        ['you', 'Does it take foreign cards?', 'Nimmt er ausländische Karten?'],
        ['peters', 'Yes, it does.', 'Ja.'],
      ],
      [
        q('Wo ist der Geldautomat?', ['Draußen neben der Tür', 'Im ersten Stock'], 0, '„next to the door“ = neben der Tür.'),
        q('Nimmt er ausländische Karten?', ['Ja', 'Nein'], 0, '„Yes, it does.“ = Ja.'),
      ],
      [
        chunk('Where is the cash machine?', 'Wo ist der Geldautomat?', 'cash_machine', 'Where is ___?', 'Wo ist ___?', 'the cash machine', 'cash machine = Geldautomat.'),
        chunk('It’s next to the door.', 'Er ist neben der Tür.', 'next_to_door', 'It’s next to ___', 'Er ist neben ___', 'the door', 'next to = neben.'),
      ],
      { promptDe: 'Frag, wo der Geldautomat ist.', model: 'Where is the cash machine?', expect: ['cash', 'machine'] },
      q('Welche Form ist richtig?', ['next to the door', 'next the door'], 0, 'next to = neben.'),
      'next to, between, opposite – Wegbeschreibungen brauchen die volle Präposition.',
      ['weg_l2_c1', 'bus_l3_c1']),
  ]),
];
