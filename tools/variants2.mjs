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
};
