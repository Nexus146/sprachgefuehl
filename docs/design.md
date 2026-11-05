# Sprachgefühl – Design & Motion

## Haltung

Warm, ruhig, erwachsen. Keine Maskottchen, kein Konfetti, keine Ranglisten. Die App spricht
Erwachsene an, nicht Kinder. Freundlichkeit entsteht durch Großzügigkeit (Weißraum, große
Schrift, klare Konturen), nicht durch Verspieltheit.

## Farbe

| Rolle | Hell | Dunkel |
|---|---|---|
| Hintergrund | `#FBF7F1` Warmweiß | `#14150F` |
| Fläche / Karte | `#FFFFFF` | `#1E211A` |
| Primär (Salbei) | `#3F6B4F` | `#9CC4A9` |
| Sekundär (Terrakotta) | `#B85C33` | `#E0A07C` |
| Text | `#23201C` | `#F2EEE5` |
| Sekundärtext | `#5A544C` | `#C3BDB1` |
| Linie | `#E0D7C9` | `#3A3F33` |

Regeln:
- Fließtext-Kontrast mindestens 7:1.
- Farbe nie als einzige Bedeutung. Richtig/falsch wird immer durch Symbol und Text getragen.
- Kein Rot als Fehlersignal. Fehler sind Lernstoff, nicht Strafe.
- Keine Verläufe hinter Text, keine Glas-Effekte.

## Typografie

- **Atkinson Hyperlegible** (OFL) für Fließtext. Speziell für Sehschwäche entworfen:
  unterscheidbare Buchstabenformen (I/l, O/0).
- **Lexend** (OFL, variable) für Überschriften.
- Basis 20 sp, Fließtext-Zeilenhöhe 30 sp. Überschriften 26–34 sp.
- Schriftgröße in den Einstellungen: 100 %, 130 %, 155 %.
- Zeilenlänge ca. 45 Zeichen, linksbündig, kein Blocksatz.

## Abstände, Formen, Flächen

- Abstandsraster: 4 / 8 / 16 / 24 / 32 / 48 dp.
- Eckenradius: 24 dp (Karten), 16 dp (kleine Flächen), 100 dp (Pills).
- Kontur: 1,5 dp, damit Flächen auch ohne Schatten erkennbar sind.
- Touch-Ziele: mindestens 56 dp, primäre Buttons 64 dp hoch.
- Ein primärer Knopf pro Bildschirm. Sekundäres immer unterhalb, nie daneben gedrängt.

## Motion

Tokens (`ui/theme/Motion.kt`):

| Token | Dauer | Einsatz |
|---|---|---|
| `fast` | 120 ms | Knopf-Feedback, Auswahl |
| `base` | 220 ms | Standard-Übergänge, Feedback-Panel |
| `slow` | 380 ms | Bildschirmwechsel |
| `garden` | 900 ms | Gartenwachstum |
| Reduced | 80–200 ms | „Ruhige Darstellung“ |

Easing: `CubicBezier(0.2, 0, 0, 1)`. Garten: `spring(dampingRatio = 0.7, stiffness = 40)`.

Regeln:
- Bewegung erklärt Ursache und Wirkung. Nie Dekoration.
- Maximal eine aufwendige Animation pro Bildschirm.
- Kein Dauer-Effekt außer der Aufnahme-Wellenform.
- Bei Fehlern: kein Shake, kein Blitz. Das Erklärpanel blendet ein und schiebt sich 8 dp.
- Fehlerkarten erscheinen in drei Blöcken (Beobachtung → Erklärung → nächster Schritt),
  damit sie lesbar bleiben.
- Systemeinstellung „Animationen entfernen“ wird respektiert; zusätzlich In-App-Schalter.

## Komponenten

- `BigButton` – 64 dp, eine klare Aktion, optional Icon.
- `SoftCard` – weiße Fläche, 1,5 dp Kontur, 24 dp Radius, optional klickbar.
- `ProgressChain` – Punkt-Kette statt Prozentzahl.
- `GardenPlant` – fünf Stufen, wächst nur, verwelkt nie.
- `Waveform` – echte Aufnahme-Rückmeldung, ruhiger Puls.
- `FeedbackPanel` – drei Töne (gut, fast, nochmal) plus Fehlerkarte.
- `ErrorCardContent` – sechs Teile: Beobachtung, Warum, richtig, Merkanker, Checkliste,
  nächster Schritt.

## Barrierefreiheit

- TalkBack-Beschriftungen für alle Icons und Aktionen.
- Keine Wischgesten, kein Long-Press, keine versteckten Menüs.
- Keine Timer, keine Herzen, keine Verluste.
- Jede Anweisung ist auch hörbar (TTS).
- Aufnahmen bleiben lokal und sind löschbar.

## Sprachprofil und Standortbestimmung

Das Profil hat **fünf Bereiche** statt einer Stufe: Hören, Grammatik, Bausteine, Sprechen,
Verknüpfen. Jeder Bereich ist ein Wert zwischen 0 und 100 mit einer Zahl von Belegen.

- **Belege aus dem Alltag**: Jede geprüfte Aufgabe meldet ein Signal. Es wird geglättet
  eingerechnet (`neu = alt + α · (Signal − alt)`); α ist am Anfang groß (0,7) und sinkt mit
  jedem Beleg auf mindestens 0,12. Ein schlechter Tag verschiebt deshalb wenig, die ersten
  Aufgaben dafür deutlich – so entsteht kein Gefühl von „ich werde dauernd neu bewertet“.
- **Vier Stufen** statt Prozent: „Wir fangen an“, „Du baust auf“, „Du bist sicher“,
  „Du kannst es frei“. Angezeigt wird immer, was das konkret heißt und was als Nächstes kommt.
- **Standortbestimmung beim Start**: fünf Blöcke, einer je Bereich, mit adaptiver Treppe.
  Zwei richtige Aufgaben in Folge heben die Stufe (0–3), eine falsche senkt sie. Ein Block endet
  nach sechs Aufgaben oder wenn Stufe 3 gesichert ist. Rund 30 Aufgaben, 12–15 Minuten,
  jederzeit unterbrechbar; der Stand liegt im Fortschrittsdokument.
- **Keine Note, keine Korrektur** während der Messung. Die Auswertung erscheint am Ende als
  fünf Sätze, nicht als Punktzahl. Die Selbsteinschätzung zählt zu 22 % mit, damit ein nervöser
  erster Moment nicht das ganze Bild bestimmt.
- **Tagesplan**: Der schwächste Bereich mit mindestens zwei Belegen bestimmt den Fokusblock.
  Die Startseite nennt ihn im Klartext („Heute passt zu dir: Fünf Minuten sprechen“) und führt
  in den passenden Raum – Üben, Wiederholen, Werkstatt oder die nächste Lektion.

## Varianten: ein Viertel, viele Geschichten

Jede Situation hat zwei weitere Erzählrahmen (`variants`). Sie ändern **wer, wo und warum** –
Story, Ort, Nachbar, Begrüßung –, nie das Lernziel: Grammatik-Säule, Bausteine und Dialoge
bleiben gleich, damit der Weg fair bleibt und der Prüfbogen gültig.

Die Wahl ist stabil: `VariantPicker` mischt `seed` mit der Situations-ID, bei Gleichstand
entscheiden die Interessen aus dem Profil. Die Entscheidung wird in `variantIds` gespeichert und
erst geschrieben, wenn eine Lektion geöffnet wird – nie während des Zeichnens.

## Wiederaufnahme statt neuer Wörter

Wo eine Lektion zur selben Grammatik-Säule schon einen früheren Baustein findet, schiebt der
Generator einen dritten Baustein ein: „Wieder getroffen“ zeigt die alte Wendung in der neuen
Situation, gefolgt von einer Sprechaufgabe. 72 der 104 Lektionen haben diesen dritten Kontakt –
Abruf im neuen Kontext wirkt stärker als eine weitere Wortliste.

## Üben: was gerade ansteht

Vier Übungsarten, ein Katalog (`PracticeCatalog`). Jede Art nennt die Bereiche, die sie wirklich
trainiert; `recommended` folgt dem schwächsten Bereich, ohne Belege ruhig „Nachsprechen“. Die
Satzauswahl (`SessionBuilder.practiceChunks`) nimmt erst Fälliges, dann Wackeliges, dann Sätze aus
geschaffenen Lektionen, dann die nächste Lektion – nie ein Thema, das noch nicht dran war. Nur
Runden mit Sprachaufgabe zählen als „gesprochen“; eine angefangene Runde wird auch beim Verlassen
als fünf Minuten verbucht, doppelt zählt sie nicht.

## Bereichs-Historie für den Stand

`SkillHistory` schreibt mit dem ersten Speichern eines Tages einen Stand der fünf Bereiche
(höchstens 60 Einträge). Das Sprachprofil vergleicht den Wert von vor sieben Tagen mit heute und
zeigt nur echte Stufenwechsel, in Worten statt Prozenten. Fehlt das Feld, zeigt das Profil keine
Bewegung – alles andere bleibt gültig.

## Standortbestimmung mit Pause

`CheckupState` war schon pausierbar, der Weg zurück fehlte. Jetzt gibt es „Pause“ in der Prüfung,
eine Karte auf der Startseite, solange eine Prüfung offen ist, und „Standort neu bestimmen“ im
Sprachprofil – mit Rückfrage, weil der alte Stand dabei ersetzt wird.

## Stimme: eine Einstellung, die überall gilt

Die Wahl „Langsam / Normal / Zügig“ stand in den Einstellungen, kam aber nur beim Testknopf an:
der Wiedergabe-Helfer hatte 0,85 fest eingebaut. Jetzt liest er den Wert beim Sprechen aus den
Einstellungen. Feste Tempi bleiben nur, wo sie didaktisch gemeint sind – die langsame
Dialogzeile (0,65), das Diktat („Langsam“ 0,6 / „Normal“ 0,9) und die Prüfaufgabe (0,6).
Rückmeldungen bieten zusätzlich „Langsam noch einmal“, damit ein einzelner Satz auch ohne
Änderung der Einstellung erreichbar bleibt.

Die Balken stehen auf einer gemeinsamen Grundlinie: kurze Wochen sehen dadurch nicht wie ein Fehler aus.

Deutsche Zahlen kommen aus `Phrasing` (Minuten, Sätze, aktive Tage, Sprechaufgaben), damit
„1 Minuten“ oder „0 Sätze gesprochen“ nicht wiederkehren. Die Woche nennt die Einheit im Titel
und markiert den heutigen Tag zusätzlich fett – Farbe allein trägt keine Information.

## Eine Empfehlung statt sechs Möglichkeiten

Die Startseite beantwortet zuerst eine Frage: **Was ist jetzt dran?** `NextPlanner`
(`domain/NextStep.kt`) liefert genau einen Schritt – mit Begründung, Dauer und einem primären
Knopf. Die Regeln greifen in dieser Reihenfolge:

1. **Offene Standortbestimmung** – sie misst und geht deshalb allem anderen vor.
2. **Halb fertige Lektion** – der Platz wird gehalten („Weiter bei Schritt 3 von 6“).
3. **Tagesziel erreicht** – die App drängt nicht mehr; Üben und Weitermachen bleiben offen.
4. **Acht oder mehr fällige Sätze** – Wiederholen vor neuem Stoff.
5. **Weniger als fünf Minuten bis zum Ziel** – eine kurze Runde passt, eine Lektion nicht.
6. **Sonst** die nächste Lektion, die das Profil nach vorn schiebt.

Der Zweitknopf ist immer die *andere* sinnvolle Handlung, nie eine zweite Empfehlung. Die früheren
Karten „Heute passt zu dir“ und „Du warst hier“ sind darin aufgegangen: dieselbe Handlung, ein Ort.

Darunter stehen Tagesziel, Garten und **„Wo du stehst“** (geschaffene Lektionen, Ort im Weg,
schwächster Bereich) als Zustand – sie fordern nichts auf. Beim ersten Besuch erklärt ein
einmaliger Hinweis diese Ordnung in drei Sätzen; danach nie wieder.

## Eine Linie statt eines Katalogs

Neun Bereiche mit 104 Lektionen sind keine Übersicht, sondern ein Regal. Die Startseite zeigt
deshalb nur, was man in zwei Sätzen sagen kann: **wo ich stehe** und **was als Nächstes kommt**.

- `JourneyPlanner` (`domain/Journey.kt`) liefert das aktuelle Kapitel, den Schritt darin, zwei
  Schritte voraus und das Folgekapitel. Der Satz „Kapitel 4 von 9 · Unterwegs · 1 von 15 Lektionen“
  ist die ganze Ortsangabe – keine Prozentzahl, keine Gesamtzahl im Vordergrund.
- **Eine Quelle für „was jetzt“:** `Personalization.suggestion` fragt zuerst `PathProgress.nextUp`.
  Eine angefangene Situation gewinnt damit überall gleichzeitig; vorher nannte die Startseite
  „Gesundheit“, während die Bereichsseite „Unterwegs“ öffnete.
- **Startseite in vier Blöcken:** Empfehlung, „Dein Weg“ (Kapitel, Balken, Danach, Folgekapitel,
  Gesamtstand), Tagesziel, Garten. Danach folgt „Auch möglich“ als leise Liste statt sechs
  gleich großer Kacheln.
- **Weggefallen, weil doppelt:** die Wochenkarte (der „Stand“ ist ein Tab), die Kacheln für Pfade
  (jetzt „Alle Bereiche“ in „Dein Weg“) und Wiederholen (eigene Zeile in „Auch möglich“).
- **Bereichsseite:** das aktuelle Kapitel ist offen, jedes andere ist eine Zeile mit Fortschritt
  und lässt sich antippen. Der ganze Inhalt bleibt erreichbar, aber nicht gleichzeitig sichtbar.

## Jede Antwort hat eine Folge

Eine Frage, die nichts ändert, wird nicht gestellt. `ProfileChoice` trägt deshalb drei Dinge:
`paths` (welche Begegnungen zuerst kommen), `storyTags` (welche Geschichten gespielt werden) und
`effect` (der Satz, der genau das benennt).

- **Wirkungstext live:** Onboarding und Profil zeigen unter jeder Frage „Das ändert: …“. Die
  Antwort erklärt sich damit selbst, ohne Hilfetext.
- **„Nachrichten“ wurde gestrichen**, weil es weder Pfad noch Geschichte hatte.
- **„Kopf fit halten“** führt jetzt zu Werkstatt und Gesprächen statt ins Leere.
- **„Bewegung & Gesundheit“** öffnet über `storyTags` die Gesundheitsgeschichten – der Profilname
  und der Tag im Inhalt müssen nicht identisch sein (`VariantPicker` bekommt Tags, keine IDs).
- **Auffrischen:** Nach drei Wochen erscheint auf der Startseite „Passt das noch?“ mit „Angaben
  ansehen“ und „Passt noch so“. Letzteres setzt `profile.reviewedDay` und die Karte schweigt
  wieder drei Wochen.

`ProfileCatalogTest` hält das fest: jede Antwort braucht einen Wirkungstext, mindestens einen
Pfad oder Tag, und jeder versprochene Pfad und Tag muss im Inhalt existieren.

## Jede Runde anders

Eine Lektion ist kein festes Raster aus fünfzehn gleichen Schritten mehr. `LessonMixer`
(`domain/LessonMixer.kt`) baut den Praxisblock bei jedem Start neu:

- **Kern bleibt:** Dialog, Verstehen, Form, Bausteine, Rollenspiel, Grammatik, Abschluss.
- **Neun Formate aus vorhandenem Inhalt:** Nachsprechen, Übersetzen, Abrufen (`Recall`),
  Wortstellung, Bedeutungswahl, Hör-Bedeutung, Diktat, Lückensatz und Verbindungswörter; dazu
  ein Muster-Transfer in einen Satz aus einer anderen Lektion. Pro Baustein 1–2 Aufgaben, immer
  eine Sprechaufgabe zuerst; die Runde bleibt bei 13–17 Schritten.
- **Onboarding gewichtet:** Jeder Grund und jedes Interesse trägt `focusSkills`; gewählte
  Bereiche bekommen fast doppelt so viele Aufgaben. Onboarding und Einstellungen zeigen das als
  „Übt besonders: Hören · Sprechen“.
- **Fehler wandern mit:** `Mistake` speichert Baustein, Lektion, Ziel, das tatsächlich Gehörte,
  Tag und Skill. Die nächste geöffnete Lektion nimmt den jüngsten Fehler auf, der nicht zu ihren
  eigenen Bausteinen gehört – als Diktat, Hörverstehen, Wortstellung, Lücke oder Nachsprechen.
  `originNote` nennt Herkunft und Gehörtes; eine richtige Antwort löst den Eintrag auf.
- **Schwierigkeit folgt dem Können:** Wackelige Bausteine (`lastGrade` 1–2 oder Lapses) bekommen
  Stützen, sichere mehr Abruf (Diktat, Übersetzen, aus dem Kopf). Das Niveau aus
  Selbsteinschätzung, Platzierung und Skillschnitt verschiebt den Pool zusätzlich.
- **Schwäche und Ziel sichtbar:** Der schwächste geprüfte Bereich bekommt genau eine
  Extraaufgabe, dazu kommt ein Transfer aus bevorzugten Pfaden oder demselben Muster. `Step.why`
  sagt in einem ruhigen Satz, warum diese Aufgabe gerade kommt.
- **Stabil mitten in der Stunde:** Der Seed besteht aus App-Seed, Lektions-ID, Kalendertag und
  Lektionslauf. Ein Neustart setzt dieselbe Runde fort, der nächste Tag sieht anders aus.
- **Modell nur, wenn es hilft:** Bei Abruf und Diktat bleibt das Modell verborgen
  (`modelHidden`); Diktat prüft streng, der Lückensatz hat genau eine richtige Option.
- **Verstehen vor Sprechen:** Genau eine lange Sprechaufgabe (ab sechs Wörtern) bekommt drei
  deutsche Bedeutungen. Richtig → sprechen; falsch → richtige Bedeutung und Modell, dann sprechen.
  Übersetzungsaufgaben bleiben außen vor, weil das Deutsche dort schon die Aufgabe ist.

## Die Übersicht: Garten zuerst, Details zugeklappt

Der Tab „Stand“ ist kein Wochenrückblick mehr, sondern die eine Antwort auf „Wo stehe ich?“.
Oben steht der Sprachgarten als große Fläche – nicht als kleine Karte am Ende. Pool und Können
sind aus der Übersicht verschwunden; sie liegen zugeklappt unter „Dein Lernweg“ in den
Einstellungen. `LearningOverview` (`domain/LearningOverview.kt`) rechnet die Zahlen, die im Garten
und in den Details erscheinen:

- **Garten:** Stufe, Runden, gemessene Zeit, aktive Tage und der nächste Wachstumsschritt. Die
  Pflanze nimmt die obere Hälfte des Stands ein; „Garten groß ansehen“ öffnet dieselbe Ansicht
  bildschirmfüllend.
- **Nächste Runde:** dieselbe Empfehlung wie auf der Startseite. Die Vorschau kommt aus
  `LessonMixer`, nicht aus einer zweiten Schätzung.
- **Fehler:** die drei jüngsten mit Satz, Herkunft und dem, was damals gehört oder getippt wurde.
- **Woche:** Minuten, gesprochene Sätze, Runden, Vorwoche und Tagesziel. Die Balken zeigen nur,
  was war – kein Tag ist Pflicht.
- **Details nur auf Wunsch:** Der Aufgabenpool (Bausteine, Lektionen, Muster, neun Formate,
  Sammlung) und die fünf Bereiche mit ihrer Bewegung stehen in den Einstellungen und sind
  standardmäßig zugeklappt.

`TaskFormats` (`domain/TaskFormats.kt`) hält die neun Formate an einer Stelle – Name, Zielbereich
und ein Satz „So geht’s“. Der Unterricht zeigt unter dem Titel „Übt: Sprechen“ (oder Hören,
Grammatik …) und nennt die Begründung „Warum diese Aufgabe?“. Am Abschluss steht, wie viele
Bausteine in der Sammlung liegen und wie viele Sätze auf eine zweite Runde warten. Start und
Profil führen mit einer leisen Zeile beziehungsweise Karte in den Stand.

## Gemessene Zeit und Tagesstreak

`LessonScreen` startet beim Öffnen einen Timer und bucht die Sekunden bei jedem Schritt und beim
Verlassen (`Progress.studySeconds`, `Progress.timeByDay`). Der Abschluss nennt „X Minuten –
gemessen“; `measuredMinutesOn` ersetzt die alte Schätzung in Tagesziel, Profil und Stand.

`Streak` zählt zusammenhängende aktive Tage aus den Sessions. Der laufende Wert bleibt bis
gestern stehen, solange heute offen ist – morgens entsteht kein Druck. Die Startseite zeigt Zahl,
Wochenpunkte und besten Lauf; ein Pausentag löscht nichts.

## Wörter zum Antippen

`Glossary` (`domain/Glossary.kt`) ist ein kleines, gepflegtes Wörterbuch der Wörter, die im
Inhalt wirklich vorkommen. `GlossedSentence` unterstreicht nur Wörter mit Eintrag gepunktet;
ein Tipp öffnet Bedeutung und Stimme. Unbekannte Wörter bleiben unmarkiert, statt eine geratene
Übersetzung anzubieten.

## Weiter erst nach der Aufgabe

Der Weiter-Knopf ist gesperrt, bis die Aufgabe gelöst ist; darunter steht der Grund. Verständnis-
fragen bestätigen jede richtige Antwort einzeln („Richtig – weiter mit Frage 2“), bevor sie zur
nächsten Frage gehen. Die Auswahlfarbe ist Grün, nicht Terrakotta: Rot und Gelb bleiben dem
Feedback vorbehalten. Sprechen hat keinen Selbsteinschätzungs-Ausweg mehr; wenn das Mikrofon
nicht funktioniert, erscheint ein Tippfeld mit strenger Bewertung.

## Wie die App spricht

Zwei Regeln für alle sichtbaren Texte:

- **Auf Handlungsseiten: eine Beobachtung, dann eine Handlung**, die auf dem Bildschirm möglich
  ist. Was der Knopf darunter schon anbietet, steht nicht noch einmal im Satz – „Höre es einmal
  langsam und sprich dann nach." statt „Höre das Modell noch einmal und sprich langsamer. Das ist
  normal."
- **Anrede „du“, Imperativ mit -e** („Höre“, „Sprich“, „Tippe“, „Lies“). Keine Floskeln wie
  „Genau so funktioniert es im Gespräch“ – sie kosten eine Zeile und sagen nichts Neues.

Ausführlich bleiben nur zwei Orte, weil dort der Text die Funktion ist: die Anleitung und die
sechsteiligen Fehlerkarten. Auf Karten, in Rückmeldungen und in Dialogen genügt ein Satz.
