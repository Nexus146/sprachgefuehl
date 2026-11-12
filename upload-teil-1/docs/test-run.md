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
- Anträge der App: `RECORD_AUDIO` und `INTERNET`. Ohne Konto wird die Netzwerk-Berechtigung nicht genutzt; die Konto-Seite zeigt ohne Supabase-Konfiguration den Einrichtungshinweis.
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

## Konto und Cloud (6. November 2026)

Erster Durchlauf der Konto-Schicht gegen das **echte** Supabase-Projekt, nicht gegen einen Mock.
Der Test liegt in `app/src/androidTest/java/de/sprachgefuehl/app/account/CloudAccountLiveTest.kt`
und läuft mit `./gradlew :app:connectedDebugAndroidTest` auf dem Emulator.

Ergebnis: `tests=1 failures=0 errors=0`, Laufzeit 7,2 s auf AVD `sg` (Android 14, API 34).
Die geprüften Schritte in einem Durchlauf:

1. **Registrierung** legt eine Sitzung an; die E-Mail gilt sofort als bestätigt.
2. **Erste Sicherung** schreibt Revision 1. Direkt gegen PostgREST geprüft: `learning_states`
   enthält `completedLessons`, `studySeconds` und die Aufnahme im Bucket `recordings`.
3. **Zweites Gerät** (SharedPreferences geleert, lokale Datei gelöscht): der Sync lädt den
   Cloud-Stand statt einen Konflikt zu fragen, und die Aufnahme kommt byte-genau zurück.
4. **Konflikt**: ein zweiter Client erzwingt Revision 2, während das Gerät eigene Änderungen hat.
   Der Sync erkennt den Konflikt und überschreibt nichts.
5. **Konfliktlösung „Cloud-Stand laden“** ersetzt das lokale Dokument und endet im Zustand
   `SYNCED`.
6. **Konto löschen** ruft die Edge Function auf. Danach: `auth.users` = 0, `learning_states` = 0,
   `storage.objects` (Bucket `recordings`) = 0, und die Anmeldung mit denselben Daten scheitert.

Für den Lauf war „Confirm email" vorübergehend aus (Autoconfirm), weil es auf dem Emulator kein
Postfach gibt; danach wurde es wieder eingeschaltet. Der Test hinterlässt kein Konto.

Dabei gefunden und behoben: ein frisch installiertes Gerät hätte beim ersten Login einen
Konflikt-Dialog statt des Cloud-Stands gezeigt. Ursache war die Auswertung „kein gespeicherter
Hash ⇒ lokal geändert" auch für ein leeres Dokument. `SyncPlanner` lädt jetzt, solange lokal
noch kein Onboarding-Stand existiert; zwei echte Stände bleiben ein Konflikt (`SyncPlannerTest`,
zwei neue Fälle).

Beim Sichtprüfen der Konto-Seite im Emulator fiel außerdem auf, dass „Passwort vergessen" neben
„Neu hier? Konto erstellen" zu einem Buchstabenstapel gequetscht wurde („Pas / swo / rt …"). Die
beiden Verweise stehen jetzt untereinander über die volle Breite; `screenshots/run/konto-2026-11-06.png`
zeigt den Zustand danach.

Der Emulator-Lauf hat den Startbildschirm, das Profil, die Einstellungen und die Konto-Seite
gerendert – mit echter `SUPABASE_URL`/`SUPABASE_ANON_KEY`, also mit Anmeldeformular statt
Einrichtungshinweis. Anmeldung, Sync und Löschung wurden dabei nicht per Tippen geprüft, sondern
im automatisierten Test oben.

## Abmelden (6. November 2026)

Rückmeldung aus dem Test: Nach dem Abmelden sei man „immer noch im Account“, und unklar sei, was
beim nächsten Anmelden übernommen wird.

Rückmeldung danach: Der Fortschritt soll beim Abmelden **mit** verschwinden – wie in jeder App.
Abmelden ist deshalb ein echter Kontowechsel, kein bloßes „Cloud aus“:

1. Vor dem Abmelden sichert die App einmal, was lokal noch offen ist.
2. Die Sitzung wird beendet, auch wenn der Server nicht antwortet (`clearSession()` als Rückfall).
3. Der lokale Lernstand wird entfernt, samt Revision, Hash und Herkunft (`owner_uid`/
   `owner_email`). Danach steht der Erststart wieder am Anfang.
4. Beim nächsten Anmelden ist lokal nichts onboarded – der Cloud-Stand wird als Download geladen,
   ohne Konfliktfrage. Ein Dialog vor dem Abmelden sagt beides an.

Geprüft mit `SessionLogoutTest` gegen das echte Projekt (Zugangsdaten als
Instrumentation-Argument, `tests=1 failures=0`):

- Anmelden lädt den Cloud-Stand (`imports=1`, `localOwner` gesetzt).
- Abmelden → `signedIn=false`, Adresse und Herkunft leer, lokaler Fortschritt leer, im
  Gerätespeicher kein `revision_*`/`hash_*`/`owner_*`.
- Nächster App-Start (neues Repository, neuer Supabase-Client): keine Sitzung vorhanden.
- Erneut anmelden → Stand kommt zurück (`imports=2`), Sync endet in `SYNCED`.

Der Emulator-Lauf zeigt den Zustand davor: Nach „Abmelden“ stand dort noch der Lernstand samt
Name, die Konto-Seite sah aus wie vorher. Auf die neue Fassung übertragen heißt das: Der Erststart
beginnt wieder bei der Begrüßung, und der Fortschritt kommt erst mit der Anmeldung zurück.

## Erststart mit Konto (6. November 2026)

Rückmeldung aus dem Test: „Ich habe schon ein Konto“, angemeldet, danach führte „Weiter“ zurück
auf die Begrüßung – man landete im Kreis und konnte nicht erkennen, ob die Anmeldung gilt.

Ursache: Im angemeldeten Zustand hing der Knopf an demselben Rückweg wie „Ohne Konto
weitermachen“. Er heißt jetzt **„Weiter mit der Einrichtung“** und führt in den Namensschritt.
Die Karte sagt außerdem, was Sache ist: „Angemeldet“, die Adresse, der Bestätigungsstatus und –
wenn die Cloud für dieses Konto leer ist – „In der Cloud liegt für dieses Konto noch nichts.“

Zwei Wege im Emulator durchgetippt (AVD `sg`, frische Installation):

1. **Konto ohne Cloud-Stand** (`waltraud.kelch1@gmail.com`): Anmelden → „Angemeldet“, „E-Mail
   bestätigt“, „In der Cloud liegt für dieses Konto noch nichts.“ → „Weiter mit der Einrichtung“
   → „Wie dürfen wir dich nennen?“.
2. **Konto mit Cloud-Stand** (`opboxnull@gmail.com`, per `SeedCloudAccountTest` gefüllt):
   Anmelden → der Stand wird geladen und die App springt ohne Onboarding in den Startbildschirm:
   „Guten Abend, Tobi“, 15 fällige Sätze, 7 von 110 Lektionen. Screenshot:
   `screenshots/run/onboarding-cloud-geladen.png`.

## Admin-Konsole (7. November 2026)

Wunsch aus dem Test: ein Admin-Konto, das sich über dasselbe „Ich habe schon ein Konto“ anmeldet,
aber **nicht** in der Lern-App landet, sondern in einem eigenen Bereich, in dem sich Konten
anlegen und verwalten lassen.

Gebaut: `public.admins` (RLS an, jeder sieht nur die eigene Zeile) entscheidet über den Zugang;
die Edge Function `admin-users` prüft den Aufrufer dagegen und nutzt erst dann den Service-Role-Key
für `list`, `create`, `set-password` und `delete`. Die App kennt keinen Service-Key. `AdminScreen`
zeigt „Angemeldet“, „Neues Konto“ und „Konten (n)“ mit Adresse, Bestätigung, Lektionen, Minuten,
Revision und letzter Sicherung; Löschen fragt nach. `AppRoot` führt ein Admin-Konto in diese
Konsole, ohne Tab-Leiste – es gibt keinen Weg von dort in die Lektionen.

Geprüft mit `AdminConsoleTest` gegen das echte Projekt (`tests=1 failures=0`, AVD `sg`):

- Login mit dem Kurznamen `admin-11` (ohne `@`) → `admin-11@sprachgefuehl.local`, `isAdmin=true`.
- `list` liefert die Konten samt Cloud-Stand; das bekannte Lernkonto erscheint mit Lektionen.
- `create` legt ein Konto an, das sofort als bestätigt gilt (ohne Mail, solange kein SMTP da ist).
- `delete` entfernt es wieder; `logout` räumt Sitzung, Admin-Status und Kontenliste ab.

Der Test legt nur ein eigenes Wegwerf-Konto an und löscht es im Lauf wieder; bestehende Konten
werden nicht verändert. Lauf:

```
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=de.sprachgefuehl.app.account.AdminConsoleTest \
  -Pandroid.testInstrumentationRunnerArguments.email=admin-11 \
  -Pandroid.testInstrumentationRunnerArguments.password=…
```

Im Emulator zusätzlich getippt: „Ich habe schon ein Konto“ → `admin-11` und Passwort → die Konsole
erscheint statt der Lern-App (`screenshots/run/admin-konsole.png`); die Liste zeigt drei Konten,
darunter `opboxnull@gmail.com` mit „7 Lektionen · 63 Minuten · Revision 1“ und „Zuletzt gesichert“,
während das eigene Konto „dieses Konto“ heißt und keinen Löschen-Knopf hat
(`screenshots/run/admin-konsole-konten.png`).

## Admin-Konsole, erweitert (7. November 2026)

Rückmeldung: Die Konsole konnte nur auflisten, anlegen, Passwort setzen und löschen – „da muss noch
mehr kommen“. Hinzugekommen sind:

1. **Überblick** über allen Karten: Konten, mit Cloud-Stand, gesperrt, Lernminuten gesamt, aktiv in
den letzten sieben Tagen. Am Gerät standen dort 4 Konten, 1 mit Cloud, 0 gesperrt, 63 Lernminuten
und 4 aktiv (`screenshots/run/admin-konsole-neu.png`).
2. **Einblick in ein Konto** („Ansehen“): Name, Tagesziel und Interessen aus dem Lernstand, dazu
Lektionen, Minuten, Anzahl und Größe der Aufnahmen, Revision und letzte Sicherung. Für
`opboxnull@gmail.com` am Gerät: „Name: Tobi“, „Tagesziel: 10 Minuten“, „Interessen: kochen“,
„Lektionen: 7 · Minuten: 63“, „Revision 1“ (`screenshots/run/admin-konto-detail.png`).
3. **Sperren und entsperren**: ein gesperrtes Konto kann sich nicht mehr anmelden, der Lernstand
bleibt. Das eigene Admin-Konto lehnt der Server ab.
4. **Fortschritt zurücksetzen**: Lernstand und Aufnahmen weg, Konto bleibt – für „nochmal von
vorn“ am Telefon.
5. **E-Mail-Adresse ändern**, sofort bestätigt.
6. **Suchen und filtern** nach Adresse oder Name, „Alle / Mit Cloud / Gesperrt“.
7. **Zugangsdaten teilen**: Adresse und Startpasswort über das Teilen-Fenster von Android.

Die Serverfunktion `admin-users` liegt jetzt als `supabase/functions/admin-users/index.ts` im Repo
(version 2, `verify_jwt` an) und kennt `list`, `detail`, `create`, `set-password`, `set-email`,
`ban`, `unban`, `reset-progress` und `delete`.

Geprüft mit `AdminConsoleTest` gegen das echte Projekt: `tests=1 failures=0`, 17,3 s. Der Lauf
prüft nacheinander: Admin-Login mit Kurznamen, Liste, Konto anlegen, Passwort setzen, Einblick
(leerer Stand), Adresse ändern, sperren → Anmeldung scheitert, entsperren → Anmeldung klappt,
Fortschritt zurücksetzen, löschen. Bestehende Konten blieben unberührt; ein übrig gebliebenes
Wegwerf-Konto aus einem früheren Lauf wurde anschließend in der Datenbank gelöscht.
