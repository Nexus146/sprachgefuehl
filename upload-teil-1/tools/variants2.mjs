// A third framing for the situations the learner meets most often, plus the two new ones.
// Same goal, same grammar, same chunks – only who, where and why change.
const V = (id, story, scene, place, personaId, greetingEn, greetingDe, interestId = '') => ({
  id, story, scene, place, personaId, greetingEn, greetingDe, interestId,
});

export const extraVariants = {
  arzt: [
    V('arzt_rezept', 'Du kommst nur wegen eines Rezepts vorbei, ohne Termin.',
      'Die Praxis ist voll, und du stehst kurz an der Anmeldung.', 'Hausarztpraxis', 'weber',
      'Just the prescription today?', 'Nur das Rezept heute?', 'gesundheit'),
  ],
  apotheke: [
    V('apotheke_knie', 'Ruth kennt dich schon und fragt nach deinem Knie.',
      'Du kommst mit dem leeren Rezept zurück.', 'Apotheke am Markt', 'ruth',
      'How is the knee today?', 'Wie geht es dem Knie heute?', 'gesundheit'),
  ],
  nachbarn: [
    V('nachbarn_hund', 'Herr Krüger sucht seinen Hund und fragt dich im Hof.',
      'Ihr steht beide im Innenhof und schauen um die Ecke.', 'Innenhof', 'krueger',
      'Have you seen Bruno?', 'Haben Sie Bruno gesehen?', 'garten'),
  ],
  familie: [
    V('familie_video', 'Lena meldet sich per Video aus dem Auslandssemester.',
      'Das Tablet steht am Küchentisch, und das Bild wackelt.', 'Küche', 'lena',
      'Can you hear me okay?', 'Können Sie mich gut hören?'),
  ],
  cafe: [
    V('cafe_stammtisch', 'Sam stellt dir Herrn Krüger vor, der jeden Morgen hier sitzt.',
      'Du kommst herein und beide schauen auf.', 'Café Linde', 'sam',
      'This is my best customer.', 'Das ist mein bester Kunde.', 'kochen'),
  ],
  supermarkt: [
    V('supermarkt_rezept', 'Herr Krüger fragt dich nach einem Rezept für Suppe.',
      'Ihr steht beide vor dem Gemüse.', 'Supermarkt', 'krueger',
      'What do I need for a soup?', 'Was brauche ich für eine Suppe?', 'kochen'),
  ],
  weg: [
    V('weg_bussteig', 'Du suchst den richtigen Bussteig und fragst Frau Peters.',
      'Am Busbahnhof sind viele Steige, und der Regen wird stärker.', 'Busbahnhof', 'peters',
      'Platform three, over there.', 'Steig drei, dort drüben.', 'reisen'),
  ],
  post: [
    V('post_paket', 'Du willst ein Paket abholen, aber der Abholschein fehlt.',
      'Frau Peters sucht in der Schublade.', 'Post', 'peters',
      'Do you have the card with you?', 'Haben Sie die Karte dabei?'),
  ],
  ausweis: [
    V('ausweis_lena', 'Lena begleitet dich und übersetzt die schwierigen Wörter.',
      'Ihr sitzt nebeneinander im Wartezimmer.', 'Bürgeramt', 'lena',
      'They’re calling your number.', 'Sie rufen Ihre Nummer auf.'),
    V('ausweis_termin', 'Du hast den Termin online gebucht und bringst alle Papiere mit.',
      'Frau Peters prüft die Kopien am Tresen.', 'Bürgeramt', 'peters',
      'Everything is here – good.', 'Alles ist da – gut.'),
  ],
  abendessen: [
    V('abendessen_lena', 'Lena ist auch eingeladen, und ihr bringt zusammen einen Nachtisch mit.',
      'Zu dritt sitzt ihr am kleinen Tisch.', 'Bei Hana', 'lena',
      'We brought dessert!', 'Wir haben Nachtisch mitgebracht!', 'kochen'),
    V('abendessen_krueger', 'Herr Krüger erzählt beim Essen von früher.',
      'Er sitzt schon am Tisch, als du kommst.', 'Bei Hana', 'krueger',
      'Sit down, the soup is hot.', 'Setzen Sie sich, die Suppe ist heiß.', 'kochen'),
  ],
  restaurant: [
    V('restaurant_sonntag', 'Der Sonntagsbraten ist aus, und Sam schlägt dir etwas anderes vor.',
      'Die Karte ist neu gedruckt, und Sam entschuldigt sich.', 'Restaurant Linde', 'sam',
      'The roast is off today – sorry.', 'Der Braten ist heute aus – tut mir leid.', 'kochen'),
  ],
  bus: [
    V('bus_nacht', 'Es ist dunkel, und du fragst Hana nach dem letzten Bus.',
      'Nur eine Laterne brennt an der Haltestelle.', 'Haltestelle Marktplatz', 'hana',
      'The last bus is at quarter to twelve.', 'Der letzte Bus fährt um Viertel vor zwölf.', 'reisen'),
  ],
  telefon: [
    V('telefon_falsch', 'Du hast dich verwählt und entschuldigst dich am Telefon.',
      'Am anderen Ende meldet sich eine fremde Stimme.', 'Küche', 'weber',
      'I think you have the wrong number.', 'Ich glaube, Sie sind falsch verbunden.'),
  ],
  hotel: [
    V('hotel_lobby', 'Dein Zimmer ist noch nicht fertig, und du wartest in der Lobby.',
      'Viele Koffer stehen an der Rezeption.', 'Hotel, Lobby', 'lena',
      'Your room will be ready at two.', 'Ihr Zimmer ist um zwei fertig.', 'reisen'),
  ],
  markt: [
    V('markt_kurz', 'Der Markt schließt gleich, und Hana verkauft dir schnell noch etwas.',
      'Die Stände werden schon abgebaut.', 'Marktplatz', 'hana',
      'Two pounds, love – all right?', 'Zwei Pfund – in Ordnung?', 'kochen'),
  ],
  fahrkartenautomat: [
    V('automat_karte', 'Der Automat nimmt deine Karte nicht, und Herr Krüger hilft dir.',
      'Auf dem Bildschirm steht eine Fehlermeldung.', 'Bahnhof', 'krueger',
      'Try the machine next to it.', 'Versuchen Sie den Automaten daneben.', 'reisen'),
  ],
  zahnarzt: [
    V('zahnarzt_termin', 'Die Kontrolle steht an, und du fragst nach einer früheren Zeit.',
      'Die Anmeldung hat noch freie Termine.', 'Zahnarztpraxis', 'weber',
      'Can you come in at four?', 'Können Sie um vier kommen?', 'gesundheit'),
  ],
  amt: [
    V('amt_nummer', 'Du ziehst eine Wartemarke und wartest auf deine Nummer.',
      'Das Wartezimmer ist voll, die Anzeige springt weiter.', 'Bürgeramt', 'peters',
      'Number forty-two, please!', 'Nummer zweiundvierzig, bitte!'),
  ],
};
