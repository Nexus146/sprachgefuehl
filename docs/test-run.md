# Testlauf am 27. Oktober 2026

Durchlauf der App auf dem Emulator, um die reparierten Stellen zu prüfen und die restlichen
Punkte aus [test-checklist.md](test-checklist.md) abzuhaken.

## Umgebung

| | |
|---|---|
| Gerät | Emulator AVD `sg`, Android 14 (API 34), x86_64 |
| Build | `./gradlew assembleDebug` (Debug-APK), `minSdk 26`, `targetSdk 35` |
| Prüfung | `./gradlew testDebugUnitTest` – 22 Testklassen, alle grün (u. a. `ScorerTest` mit 9 Fällen) |
| Bedienung | `adb`-Taps über `uiautomator dump`, Screenshots in `screenshots/run/` (120–221) |

Der Emulator hat **kein englisches Offline-Sprachpaket**. Spracherkennung scheitert dort mit
Fehlercode 13 (`ERROR_LANGUAGE_UNAVAILABLE`). Alles, was echte Erkennung braucht, ist unten als
nicht prüfbar vermerkt; alles andere lief gegen die echte App.

## Was geprüft wurde

**Onboarding und Standortbestimmung**
- Willkommensseite, Name, Lerngrund, Interessen, Tagesziel, Lerntage, Selbsteinschätzung: laufen durch.
- „Standort bestimmen“ startet mit „Teil 1 von 5 · Hören“; zwei Aufgaben beantwortet, dann die App
  hart beendet (`force-stop`) und neu geöffnet: es ging bei **2/30** weiter, nicht von vorn.
- „Pause“ führt zurück zur Selbsteinschätzung; „Ohne Prüfung starten“ setzt einen vorsichtigen
  Startpunkt mit allen fünf Bereichen.
- „Erste Runde starten“ öffnet direkt die Lektion **1/13** (früher passierte hier nichts).

**Lektion**
- Dialog, „Verstanden?“, Muster-Karte, Bausteine, „Bau den Satz“, Wortstellung, „Sätze verbinden“,
  freies Sprechen, Abschluss: alle Schritte erreichbar.
- Falsche Antwort: freundliche Rückmeldung mit richtigem Satz, kein rotes Kreuz, „Weiter“ bleibt offen.
- Abschluss: „Lektion geschafft“ mit Kann-Satz, Gartenstufe „Der erste Trieb · 1 Runde“,
  Meilenstein „Der erste Kaffee“.
- Der Hinweis über dem Sprechschritt ist jetzt ein inhaltliches Wort („Sag es mit: ‚last week‘“),
  keine leeren Anführungszeichen und kein Fürwort.

**Untere Leiste und Bereiche**
- Start, Üben, Woche, Profil: Auswahl bleibt markiert, Inhalte passen.
- Profil: Name/Grund/Tagesziel/Lerntage in einer Zeile, „Deine Angaben“, Sprachprofil, „Neues Handy?“,
  Statistik („1 von 110 Lektionen“), Meilensteine, Weg in die Einstellungen.
- „Deine Angaben“: Name mit Begrüßungsvorschau, Grund, Interessen, Tagesziel, Lerntage, Startpunkt
  und Schlusshinweis – alles an einer Stelle, Zurück-Pfeil und Systemtaste führen ins Profil.
- Woche: Gartenstufe, „Noch x Runden bis …“, Minuten, gesprochene Sätze, Runden, Tagesbalken.
- Meine Sätze: Filter, „Alle Sätze anhören“, Aufnahme starten/stoppen, Aufnahme erscheint mit Datum,
  Abspielen und Löschen vorhanden.
- Fehler-Werkstatt: leerer Zustand mit Erklärung, „Alle Erklärungen“, Karte („for oder since?“)
  zeigt Beobachtung und Begründung.
- Suche: „headache“ liefert den Satz mit Übersetzung, „In der Lektion üben“ vorhanden.
  Zurück-Taste schließt erst die Tastatur (Suchbegriff bleibt), danach führt sie zur Startseite.
- Üben: alle vier Arten gestartet und bedient – Nachsprechen, Diktat, Wortstellung, Frei sprechen.
  Beenden zählt die Runde (Minuten im Profil steigen).
- Wiederholen: getippte Antwort wird bewertet und zählt nicht als gesprochen; Runde läuft weiter.

**Sicherung und Gerätewechsel**
- „Sicherung erstellen“ → Speichern-Dialog → Datei `sprachgefuehl-sicherung (3).zip` in Downloads,
  Inhalt: `fortschritt.json` und `aufnahmen/rec_….m4a`.
- „Sicherung laden“ → Rückfrage „Sicherung laden?“ → „Laden“ → „Sicherung geladen. Dein Fortschritt
  und eine Aufnahme sind wieder da.“; `progress.json` auf dem Gerät wurde tatsächlich ersetzt
  (abgeschlossene Lektionen, Sätze, Runden, Garten), die Aufnahme in den privaten Ordner kopiert.

**Darstellung und Berechtigungen**
- Schriftgröße „Sehr groß“ und „Dunkel“: kein abgeschnittener Text, Auswahl bleibt nach Neustart.
- Anträge der App: nur `RECORD_AUDIO`. Keine Internet-Berechtigung.
- Mikrofon verweigern: Hinweis „Ohne Mikrofon kann ich nicht zuhören. Schätze selbst ein – das zählt
  genauso.“ plus Selbstcheck „Hat geklappt / Fast“, danach normale Rückmeldung und „Weiter“.

## Gefunden und behoben

1. **Nach dem Erlauben des Mikrofons kam keine Aufnahme zustande.** Die App fragte nach der
   Berechtigung und meldete im selben Moment „Bitte erlaube das Mikrofon“ – also genau das, was die
   Lernende gerade getan hatte. Jetzt hört die App nach der Erlaubnis von selbst zu; nur eine
   echte Ablehnung führt zum Hinweis.
2. **Fehlendes Sprachpaket wurde nicht erklärt.** Fehlte die englische Offline-Erkennung, stand dort
   nur „Das hat gerade nicht geklappt. Noch einmal?“ – ein zweiter Versuch kann aber nicht helfen.
   Jetzt nennt die App die Ursache und den Weg dorthin (Android-Einstellungen, mit WLAN nachladen)
   und bietet den Selbstcheck an.
3. **Ein halber Satz galt als perfekt.** Für „How are you?“ reichte das Wort „how“ für „Klar
   verstanden“, weil in kurzen Formeln fast alle Wörter Funktionswörter sind. Die beste Stufe
   verlangt jetzt zusätzlich Deckung über den ganzen Satz; „how“ ergibt „Fast geschafft“ mit dem
   Hinweis, was fehlte. Zwei Fälle sind als Test in `ScorerTest` festgehalten.
4. **Die Karte „Nächste Begegnung“ las sich wie etwas Neues**, obwohl die Lektion schon lief
   („ca. 11 Minuten · 13 Schritte“). Sie sagt jetzt „Weiter bei Schritt 7 von 13 · ca. 11 Minuten“
   und der Knopf heißt „Weitermachen“ – der Sprung landet auch wirklich bei Schritt 7.
5. **Rückmeldung im Wiederholen kannte nur zwei Stufen** („Klar verstanden“ für alles ab „fast“).
   Jetzt gibt es drei Stufen wie in den Lektionen, und bei „fast“ steht dabei, welche Wörter fehlten.
6. **Bei „Sehr groß“ klebten die Auswahlknöpfe am Rand** („Wie Handy“ brach zweizeilig um) –
   `197-gross.png` zeigt die angeschnittene Beschriftung. Die Auswahl hat jetzt Polster
   (`ChoiceButton` in `SettingsScreen.kt`); am Gerät nachgeprüft: `221-einstellungen-sehr-gross-nach-fix.png`
   zeigt beide mehrzeiligen Beschriftungen mit Abstand zum Rand.

## Offen / nicht prüfbar in dieser Umgebung

- **Spracherkennung mit echter Stimme**: Im Emulator fehlt das englische Offline-Paket, deshalb
  kamen keine Erkennungsergebnisse zustande. Bewertung, Rückmeldung und Selbstcheck wurden nur über
  den getippten Weg und den Ausfallweg geprüft. Für echte Sprechaufgaben braucht es ein Handy.
- **Fehlerkarte mit allen sechs Teilen in einer Lektion**: braucht eine erkannte Äußerung und ist
  damit an dieselbe Bedingung geknüpft. Die Werkstatt-Karte (Beobachtung, Begründung) wurde gesehen.
- **TalkBack-Beschriftungen** (Punkt 27) und **Flugmodus** (Punkt 29/47): nicht in diesem Lauf.
- **Stimmgeschwindigkeit hörbar vergleichen** (Punkt 72): die TTS-Stimme des Emulators ist kein
  Maßstab; auf einem Handy prüfen.
- **Zurücksetzen** (Punkt 42/48): der Weg über „Sicherung laden“ wurde geprüft, „Fortschritt
  zurücksetzen“ nicht, um den aufgebauten Lernstand für den Rest des Laufs zu behalten.

## Wiederherstellung nach dem Lauf

Auf dem Emulator steht wieder Schriftgröße „Normal“, die Mikrofon-Erlaubnis ist nach dem
Verweigern-Test nicht erteilt. Die Sicherung `sprachgefuehl-sicherung (3).zip` in Downloads bleibt
als Beispiel für Punkt 40/42 liegen. Der Lernstand (Ingrid, 21 Minuten, Lektion „Das Wetter
als Nächste“) ist aus der geladenen Sicherung erhalten und wurde für die Nachprüfung des
Polsters nur gelesen, nicht verändert.
