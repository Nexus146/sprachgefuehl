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

## Bereichs-Historie für die Woche

`SkillHistory` schreibt mit dem ersten Speichern eines Tages einen Stand der fünf Bereiche
(höchstens 60 Einträge). Die Woche vergleicht den Stand von vor sieben Tagen mit heute und zeigt
nur echte Stufenwechsel, in Worten statt Prozenten. Fehlt das Feld, zeigt die Woche keine
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
