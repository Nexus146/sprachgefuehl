// Erzeugt von scripts/build-content.mjs aus app/src/main/assets/content/content.json und
// app/build.gradle.kts. Nicht von Hand bearbeiten – mit "npm run content" neu erzeugen.

export type ChapterDetail = { title: string; subtitle: string; lessons: number };
export type Chapter = {
  id: string;
  order: number;
  title: string;
  subtitle: string;
  situations: number;
  lessons: number;
  detail: ChapterDetail[];
};
export type Season = { id: string; title: string; note: string };
export type Persona = { name: string; role: string; note: string };

export const world = {
  "name": "Dein Viertel",
  "intro": "Ein Viertel, ein Jahr, feste Nachbarn. Jede Lektion ist eine Begegnung – keine Insel."
} as const;

export const stats = {
  "chapters": 15,
  "situations": 52,
  "lessons": 186,
  "chunks": 318,
  "patterns": 221,
  "canDo": 178,
  "errorCards": 56,
  "minimalPairs": 47,
  "variants": 115,
  "maxVariants": 3,
  "checkup": 85,
  "personas": 7,
  "minSdk": 26,
  "minAndroid": "8.0"
} as const;

export const chapters: Chapter[] = [
  {
    "id": "gesundheit",
    "order": 1,
    "title": "Gesundheit",
    "subtitle": "Arzt, Apotheke, Termin",
    "situations": 4,
    "lessons": 13,
    "detail": [
      {
        "title": "Beim Arzt",
        "subtitle": "Sagen, was weh tut",
        "lessons": 3
      },
      {
        "title": "In der Apotheke",
        "subtitle": "Etwas gegen Beschwerden holen",
        "lessons": 3
      },
      {
        "title": "Beim Zahnarzt",
        "subtitle": "Termin, Schmerzen, Nachsorge",
        "lessons": 4
      },
      {
        "title": "Im Krankenhaus",
        "subtitle": "Überweisung und Untersuchung",
        "lessons": 3
      }
    ]
  },
  {
    "id": "leute",
    "order": 2,
    "title": "Leute & Small Talk",
    "subtitle": "Nachbarn, Familie, Alltag",
    "situations": 4,
    "lessons": 12,
    "detail": [
      {
        "title": "Nachbarn & Small Talk",
        "subtitle": "Ins Gespräch kommen",
        "lessons": 3
      },
      {
        "title": "Über die Familie",
        "subtitle": "Von den Enkeln erzählen",
        "lessons": 3
      },
      {
        "title": "Über früher erzählen",
        "subtitle": "Wie das Viertel war",
        "lessons": 3
      },
      {
        "title": "Fotos zeigen",
        "subtitle": "Erinnerungen teilen",
        "lessons": 3
      }
    ]
  },
  {
    "id": "einkaufen",
    "order": 3,
    "title": "Einkaufen & Essen",
    "subtitle": "Café, Supermarkt, Preise",
    "situations": 4,
    "lessons": 15,
    "detail": [
      {
        "title": "Bäckerei & Café",
        "subtitle": "Höflich bestellen",
        "lessons": 3
      },
      {
        "title": "Im Supermarkt",
        "subtitle": "Finden, fragen, bezahlen",
        "lessons": 4
      },
      {
        "title": "Im Restaurant",
        "subtitle": "Zu zweit essen gehen",
        "lessons": 4
      },
      {
        "title": "Auf dem Markt",
        "subtitle": "Fragen, vergleichen, kaufen",
        "lessons": 4
      }
    ]
  },
  {
    "id": "unterwegs",
    "order": 4,
    "title": "Unterwegs",
    "subtitle": "Weg, Bus, Bahn",
    "situations": 5,
    "lessons": 18,
    "detail": [
      {
        "title": "Nach dem Weg fragen",
        "subtitle": "Höflich nach dem Weg fragen",
        "lessons": 3
      },
      {
        "title": "Bus & Bahn",
        "subtitle": "Ticket, Gleis, Umsteigen",
        "lessons": 4
      },
      {
        "title": "Am Flughafen",
        "subtitle": "Einchecken und durch die Kontrolle",
        "lessons": 3
      },
      {
        "title": "Im Hotel",
        "subtitle": "Ankommen, Frühstück, auschecken",
        "lessons": 4
      },
      {
        "title": "Am Fahrkartenautomaten",
        "subtitle": "Ticket ziehen, ohne Hilfe klarkommen",
        "lessons": 4
      }
    ]
  },
  {
    "id": "alltag",
    "order": 5,
    "title": "Alltag & Zuhause",
    "subtitle": "Telefon, Hilfe, Nachbarn",
    "situations": 5,
    "lessons": 18,
    "detail": [
      {
        "title": "Telefon & Termin",
        "subtitle": "Am Telefon einen Termin machen",
        "lessons": 4
      },
      {
        "title": "Um Hilfe bitten",
        "subtitle": "Hilfe holen und danken",
        "lessons": 3
      },
      {
        "title": "Bei der Bank",
        "subtitle": "Konto, Karte, Geld abheben",
        "lessons": 4
      },
      {
        "title": "Auf der Post",
        "subtitle": "Paket, Brief, Versand",
        "lessons": 4
      },
      {
        "title": "Beim Friseur",
        "subtitle": "Termin, Wunsch, Bezahlen",
        "lessons": 3
      }
    ]
  },
  {
    "id": "behoerden",
    "order": 6,
    "title": "Behörden & Termine",
    "subtitle": "Amt, Kasse, Formulare",
    "situations": 3,
    "lessons": 11,
    "detail": [
      {
        "title": "Im Bürgeramt",
        "subtitle": "Termin, Formular, Nachfrage",
        "lessons": 5
      },
      {
        "title": "Bei der Krankenkasse",
        "subtitle": "Am Telefon klären",
        "lessons": 3
      },
      {
        "title": "Neuer Ausweis",
        "subtitle": "Antrag, Preis, Warten",
        "lessons": 3
      }
    ]
  },
  {
    "id": "feste",
    "order": 7,
    "title": "Feste & Einladen",
    "subtitle": "Geburtstag, Nachbarschaft, Beiträge",
    "situations": 3,
    "lessons": 9,
    "detail": [
      {
        "title": "Zum Geburtstag einladen",
        "subtitle": "Einladen und absagen",
        "lessons": 3
      },
      {
        "title": "Beim Nachbarschaftsfest",
        "subtitle": "Helfen und mitreden",
        "lessons": 3
      },
      {
        "title": "Zu Gast zum Abendessen",
        "subtitle": "Loben, helfen, verabschieden",
        "lessons": 3
      }
    ]
  },
  {
    "id": "hakten",
    "order": 8,
    "title": "Wenn’s hakt",
    "subtitle": "Nachfragen, reparieren, klären",
    "situations": 3,
    "lessons": 9,
    "detail": [
      {
        "title": "Langsamer, bitte",
        "subtitle": "Sagen, dass du nicht verstehst",
        "lessons": 3
      },
      {
        "title": "Missverständnisse klären",
        "subtitle": "Sich verständlich machen",
        "lessons": 3
      },
      {
        "title": "Etwas reklamieren",
        "subtitle": "Freundlich sagen, was nicht stimmt",
        "lessons": 3
      }
    ]
  },
  {
    "id": "werkstatt",
    "order": 9,
    "title": "Werkstatt",
    "subtitle": "Grammatik, Gelenke, Strategien",
    "situations": 3,
    "lessons": 27,
    "detail": [
      {
        "title": "Grammatik-Werkstatt",
        "subtitle": "Die zwölf Säulen",
        "lessons": 12
      },
      {
        "title": "Verbindungswörter",
        "subtitle": "Sätze verbinden",
        "lessons": 5
      },
      {
        "title": "Klang & Strategien",
        "subtitle": "Hören, nachfragen, antworten",
        "lessons": 10
      }
    ]
  },
  {
    "id": "arbeit",
    "order": 10,
    "title": "Arbeit & Büro",
    "subtitle": "Ankommen, mitreden, nachfragen",
    "situations": 3,
    "lessons": 9,
    "detail": [
      {
        "title": "Im Büro",
        "subtitle": "Erster Tag und Alltag",
        "lessons": 3
      },
      {
        "title": "Besprechung",
        "subtitle": "Mitreden und widersprechen",
        "lessons": 3
      },
      {
        "title": "Bewerbungsgespräch",
        "subtitle": "Über Erfahrung sprechen",
        "lessons": 3
      }
    ]
  },
  {
    "id": "reisen",
    "order": 11,
    "title": "Reisen & Urlaub",
    "subtitle": "Unterwegs zurechtkommen",
    "situations": 3,
    "lessons": 9,
    "detail": [
      {
        "title": "Im Hotel",
        "subtitle": "Einchecken und Fragen",
        "lessons": 3
      },
      {
        "title": "Zug & Flughafen",
        "subtitle": "Tickets und Anschlüsse",
        "lessons": 3
      },
      {
        "title": "Ausflüge",
        "subtitle": "Was unternehmen und erzählen",
        "lessons": 3
      }
    ]
  },
  {
    "id": "wohnen",
    "order": 12,
    "title": "Wohnen & Nachbarn",
    "subtitle": "Zuhause und im Haus",
    "situations": 3,
    "lessons": 9,
    "detail": [
      {
        "title": "Nachbarn",
        "subtitle": "Begrüßen und bitten",
        "lessons": 3
      },
      {
        "title": "Wohnung & Mängel",
        "subtitle": "Etwas ist kaputt",
        "lessons": 3
      },
      {
        "title": "Umzug",
        "subtitle": "Helfen und fragen",
        "lessons": 3
      }
    ]
  },
  {
    "id": "telefon",
    "order": 13,
    "title": "Telefon & Digitales",
    "subtitle": "Anrufen, schreiben, Termine",
    "situations": 3,
    "lessons": 9,
    "detail": [
      {
        "title": "Am Telefon",
        "subtitle": "Melden und nachfragen",
        "lessons": 3
      },
      {
        "title": "Termine online",
        "subtitle": "Buchen und verschieben",
        "lessons": 3
      },
      {
        "title": "Nachrichten",
        "subtitle": "Kurz schreiben",
        "lessons": 3
      }
    ]
  },
  {
    "id": "sport",
    "order": 14,
    "title": "Sport & Freizeit",
    "subtitle": "Verein, Studio, verabreden",
    "situations": 3,
    "lessons": 9,
    "detail": [
      {
        "title": "Im Verein",
        "subtitle": "Anmelden und mitreden",
        "lessons": 3
      },
      {
        "title": "Fitnessstudio",
        "subtitle": "Probetraining und Kurse",
        "lessons": 3
      },
      {
        "title": "Verabreden",
        "subtitle": "Einladen und absagen",
        "lessons": 3
      }
    ]
  },
  {
    "id": "notfall",
    "order": 15,
    "title": "Notfälle & Hilfe",
    "subtitle": "Wenn es schnell gehen muss",
    "situations": 3,
    "lessons": 9,
    "detail": [
      {
        "title": "Apotheke & Notdienst",
        "subtitle": "Beschwerden und Medikamente",
        "lessons": 3
      },
      {
        "title": "Etwas verloren",
        "subtitle": "Melden und beschreiben",
        "lessons": 3
      },
      {
        "title": "Hilfe rufen",
        "subtitle": "Notfall und Weg",
        "lessons": 3
      }
    ]
  }
];

export const seasons: Season[] = [
  {
    "id": "herbst",
    "title": "Herbst – Ankommen",
    "note": "Du ziehst ins Viertel und lernst die ersten Gesichter kennen."
  },
  {
    "id": "winter",
    "title": "Winter – Vertrauen",
    "note": "Aus Bekannten werden Nachbarn, aus Nachbarn Freunde."
  },
  {
    "id": "fruehling",
    "title": "Frühling – Ausflüge",
    "note": "Du wirst sicherer und gehst weiter hinaus."
  },
  {
    "id": "sommer",
    "title": "Sommer – Die Reise",
    "note": "Du reist mit Lena – und kommst mit Geschichten zurück."
  }
];

export const personas: Persona[] = [
  {
    "name": "Hana",
    "role": "Nachbarin",
    "note": "Wohnt gegenüber, kennt jeden im Haus und hilft sofort."
  },
  {
    "name": "Sam",
    "role": "Café Linde",
    "note": "Führt das Café an der Ecke und merkt sich Stammgäste."
  },
  {
    "name": "Ruth",
    "role": "Apotheke",
    "note": "Erklärt jede Anwendung zweimal, ohne zu bevormunden."
  },
  {
    "name": "Dr. Weber",
    "role": "Hausärztin",
    "note": "Fragt nach Fakten und wiederholt sie zur Sicherheit."
  },
  {
    "name": "Lena",
    "role": "Enkelin",
    "note": "Studiert in der Stadt und nimmt dich mit auf Reisen."
  },
  {
    "name": "Herr Krüger",
    "role": "Nachbar mit Hund",
    "note": "Geht dreimal am Tag mit Bruno um den Block."
  },
  {
    "name": "Frau Peters",
    "role": "Bank & Post",
    "note": "Erklärt Formulare in einfachen Schritten."
  }
];
