# Sprachgefühl

Android-App für alle, die Englisch nicht nur erkennen, sondern **verstehen und selbst sprechen** wollen. Ruhig, offline nutzbar, ohne Zeitdruck – ein Konto ist optional. Der Fortschritt zieht per Sicherungsdatei oder Cloud auf ein neues Handy um.

Arbeitstitel: Sprachgefühl. UI-Sprache Deutsch, Lernsprache Englisch.

Aktuelle Fassung: **2.0** (versionCode 8). Die signierte APK, das App-Bundle für den Play Store und das fertige Upload-Paket liegen unter [`github-vorbereitung/v2.0/`](github-vorbereitung/v2.0/); Store-Grafiken und Screenshots unter [`store/`](store/).

## Was die App macht

Jede Lektion verbindet fünf Dinge, die in Sprachlern-Apps oft getrennt sind:

1. **Hören und verstehen** – ein kurzer Alltagsdialog, zwei Tempi, deutsche Hilfe auf Wunsch.
2. **Die Form dahinter** – eine Grammatikstelle, deutsch erklärt, mit Kontrast zur Muttersprache.
3. **Bausteine** – formelhafte Wendungen („Could I have …?“), nicht isolierte Wörter.
4. **Selbst sprechen** – Nachsprechen, Lücken füllen, Rollenspiel, Übertragen in neue Situationen.
5. **Fehler verstehen** – sechs Teile: was passiert ist, warum es anders ist, wie es richtig geht, Merkanker, Checkliste fürs nächste Mal, nächster Schritt.

### Der Weg durch die App

- **Vier Bereiche unten**: Start, Üben, Stand, Profil. In einer Lektion verschwindet die Leiste, damit der Weg nach vorn der einzige bleibt.
- **Profil beim ersten Start**: Name (freiwillig), Lerngrund, Interessen, Tagesziel und Lerntage entstehen im Onboarding – in zwei Schritten, jeder überspringbar. Später liegen alle Angaben zusammen unter **„Deine Angaben“** (oberste Karte im Profil), getrennt von der Statistik und den Einstellungen.
- **Wo war ich?**: Die Startseite zeigt „Du warst hier stehen geblieben – bei Schritt 6 von 17“ samt der Zeit, die der nächste Schritt braucht, und führt mit einem Knopf zurück in die angefangene Lektion.
- **Fünfzehn Kapitel**: Gesundheit, Leute & Small Talk, Einkaufen & Essen, Unterwegs, Alltag & Zuhause, Behörden & Termine, Feste & Einladen, Wenn’s hakt, Arbeit & Büro, Reisen & Urlaub, Wohnen & Nachbarn, Telefon & Digitales, Sport & Freizeit, Notfälle & Hilfe – dazu die Werkstatt. Insgesamt 52 Situationen mit 186 Lektionen, 3 bis 12 je Situation.
- **Ein eigenes Viertel pro Lernendem**: Zu einer Situation gehören bis zu drei Erzählrahmen (insgesamt 115) – dieselben Bausteine, eine andere Geschichte. Ein gespeicherter Seed wählt aus, Interessen entscheiden bei Gleichstand: das Ziel bleibt gleich, die Geschichte nicht.
- **Sichtbarer Fortschritt**: Jedes Kapitel zeigt „1 von 12 Lektionen in diesem Kapitel“, jede Situation ihren Zustand (Neu, Angefangen, Geschafft) und die nächste Lektion; die empfohlene Situation trägt „Als Nächstes“.
- **Suche**: Sätze, Muster und Situationen auf Deutsch oder Englisch wiederfinden und direkt in der passenden Lektion üben.
- **Tablet und große Handys**: Der Inhalt bleibt in einer ruhigen Spalte von höchstens 560 dp; die Tab-Leiste liegt über die volle Breite darunter, ihre Ziele stehen in derselben Spalte wie der Text.

### Üben und Wiederholen

- **Üben mit Ansage**: Oben steht, was gerade ansteht („12 Sätze sind bald fällig“, „3 Sätze wackeln noch“) und welche der vier Übungsarten dazu passt; jede Karte nennt die Bereiche, die sie trainiert. Geübt wird zuerst, was fällig ist, dann Geschaffenes – nie ein Thema, das noch nicht dran war.
- **Wiederholen auf zwei Wegen**: sprechen oder tippen, wenn es gerade laut ist. Am Ende „Noch eine Runde“, und eine angefangene Runde zählt auch beim Verlassen.
- **Jede Runde anders – und passend**: Aus den 318 Bausteinen entstehen Runden mit 6 bis 15 Schritten (meist 13) und 11 bis 14 verschiedenen Schrittarten. Neunzehn Arten kommen vor: Baustein neu, Nachsprechen, Grammatikstelle, Noticing, Selbsteinschätzung, frei sprechen, Dialog hören, Hörverstehen vertiefen, Satz bauen, Verbindungswort wählen, Lücke sprechen, Übertragen, Sätze verbinden, Klangpaar, schnelles Sprechen, kurze Nachricht, auf die Zeile des Gegenübers antworten, Satzende wählen, Gespräch ordnen. Onboarding-Antworten gewichten die Bereiche, Fehler kommen in der nächsten Lektion mit dem Gehörten zurück, wackelige Sätze bekommen Stützen, sichere mehr Abruf.
- **Im Unterricht** steht unter dem Titel „Übt: …“, die Begründung heißt „Warum diese Aufgabe?“; am Ende siehst du, wie viele Bausteine deine Sammlung hat und wie viele Sätze zurückkommen.
- **Standortbestimmung beim Start**: Prüfaufgaben je Bereich mit adaptiver Treppe (zwei richtige Aufgaben heben die Stufe, eine falsche senkt sie) – rund 12–15 Minuten aus 85 Aufgaben, pausierbar, ohne Noten. Wer nicht prüfen mag, startet vorsichtig nach Selbsteinschätzung.
- **Standortbestimmung mit Pause**: jederzeit unterbrechbar; die Startseite bietet danach „weiter bei Teil 3 von 5“, das Sprachprofil ein „Standort neu bestimmen“.
- **Tagesplan mit Fokus**: Die Startseite nennt den schwächsten Bereich und schlägt genau eine passende Runde vor (Sprechen, Wiederholen, Grammatik oder Verbinden).

### Sehen, was wächst

- **Stand in Worten**: Der Tab „Stand“ stellt den Sprachgarten in den Mittelpunkt – groß, mit Gartenstufe und dem nächsten Wachstumsschritt –, darunter die letzte Woche, offene Fehler, Vorwoche und Tagesziel. Pool und die fünf Bereiche stehen nur noch zugeklappt unter „Dein Lernweg“ in den Einstellungen. Die Minuten stammen aus der gemessenen Lernzeit.
- **Sprachprofil statt einer Stufe**: fünf Bereiche (Hören, Grammatik, Bausteine, Sprechen, Verknüpfen) mit vier Stufen, fortgeschrieben aus jeder echten Aufgabe.
- **Werkstatt**: 12 Grammatik-Säulen, 5 Stufen Verbindungswörter, Strategien (nachfragen, umschreiben, Zeit gewinnen), Klangpaare, schnelles Sprechen, kurze Nachrichten, Zahlen und Zeiten – als 27 feste Lektionen, in denen die Grammatik für sich steht.
- **Sprachkarte**: 221 Muster mit ihren Bausteinen und Verwandtschaften; wächst mit dem Lernstand.
- **Fehler-Werkstatt**: persönliches Fehlerprofil aus 56 Fehlerkarten und eine Wiederholungsqueue.
- **Klangpaare**: 47 Minimalpaare für die Laute, die im Deutschen zusammenfallen.
- **Sprachgarten**: wächst mit jeder Runde, verwelkt nie, keine Rangliste – er steht im Tab „Stand“ als große Fläche und lässt sich in voller Größe ansehen.
- **Tagesstreak statt Serie unter Druck**: Die Startseite zeigt, wie viele Tage in Folge etwas passiert ist, mit Wochenpunkten und bestem Lauf. Ein Pausentag nimmt nichts weg, die Streak mahnt nicht.
- **Gemessene Lernzeit**: Während einer Lektion läuft ein Timer; Tagesziel, Profil und Stand nutzen die wirklich gemessenen Minuten statt einer Schätzung. Der Tagesziel-Ring hört beim Ziel auf zu füllen – wer mehr übt, sieht „10 von 10“, nicht „51 von 10“.
- **Kann-Liste** statt Punkte: 164 Sätze wie „Ich kann sagen, was mir weh tut.“
- **Ruhiges Tagesende**: Ist das Ziel erreicht, sagt die App „Für heute reicht es“. An einem Pausentag wird nichts erwartet.
- **Meine Sätze**: Suche, „Alle anhören“, Aufnahmen mit Datum und einzeln löschbar.

### Stimme, Schrift und Daten

- **Wörter antippen**: In Modellsätzen sind bekannte Wörter gepunktet unterstrichen; ein Tipp zeigt die deutsche Bedeutung und spricht das Wort vor.
- **Alle Zeilen anhören**: Dialoge laufen auf Wunsch am Stück – Hände frei.
- **Zwei Darstellungen**: hell und dunkel – wahlweise wie das Handy eingestellt. Schriftgröße bis 155 %, ruhige Übergänge; bei großer Schrift stapeln sich Karten und Ringe, statt Text zu quetschen. Die Kontraste beider Darstellungen sind gemessen und als Test festgehalten (mindestens 4,5:1 für Text, 3:1 für Kanten, Symbole und Knöpfe).
- **Einstellungen in drei Teilen**: Darstellung, Stimme, Daten – dazu der zugeklappte Bereich „Dein Lernweg“. Die Stimmwahl („Langsam / Normal / Zügig“) gilt für alle Modellsätze; in Üben, Lektion und Prüfung gibt es zusätzlich einen „Langsam“-Knopf. Alles Persönliche steht gebündelt unter „Deine Angaben“ im Profil.
- **Konto optional**: Ohne Anmeldung bleibt alles auf dem Gerät. Wer möchte, verbindet ein Supabase-Konto und sichert Fortschritt, Sätze und optional die Aufnahmen in der Cloud ([docs/accounts.md](docs/accounts.md)).
- **Sicherungsdatei bleibt**: Für ein neues Handy kann man weiterhin eine Datei erstellen und dort wieder laden ([docs/backup.md](docs/backup.md)).
- **Vorher/Nachher**: eigene Aufnahmen bleiben ohne Konto lokal.

## Zahlen

| Was | Wie viel |
|---|---|
| Kapitel | 15 (14 Alltagskapitel + Werkstatt) |
| Situationen | 52 |
| Lektionen | 186 (159 gemischt, 27 Werkstatt) |
| Erzählrahmen | 115, bis zu drei je Situation |
| Bausteine | 318 |
| Muster | 221 |
| Kann-Sätze | 167 |
| Fehlerkarten | 56 |
| Minimalpaare | 47 |
| Prüfaufgaben der Standortbestimmung | 85 |
| Personen im Viertel | 7 |
| JVM-Tests | 321 in 47 Klassen |

## Wissenschaftliche Grundlage (kurz)

| Prinzip | Umsetzung |
|---|---|
| Verstehbares Input, Form-Bedeutung | Dialoge mit Bild/Situation, Deutsch als Hilfe, nicht als Umweg |
| Noticing | Die Zielform wird vor dem Üben markiert und kontrastiert |
| Abrufübung + Spacing | FSRS-inspirierter Scheduler auf gesprochene Chunks, wechselnde Kontexte |
| Prozeduralisierung | Fünfstufige Automatisierungs-Leiter: erkennen → mit Stütze → ohne → flüssig → übertragen |
| Korrekturfeedback mit Reparatur | Prompt → eigene Reparatur → Erklärung → Transfer → späterer Retest |
| Task-based Learning | Jede Lektion hat ein kommunikatives Ziel, nicht nur eine Übung |
| Aussprache | Mehrere Stimmen, Minimalpaare, ehrliches Drei-Stufen-Feedback statt Prozentzahl |

Belege (Auswahl): Li 2010 und Lyster & Saito 2010 zu Korrekturfeedback; Ellis, Loewen & Erlam 2006 zu metalinguistischer Erklärung; Nassaji 2011 zu Reparatur und Behalten; Bryfonski & McKay 2019 zu TBLT; Kang, Sok & Han 2019 zu Form-Focused Instruction; Uchihara et al. 2025 zu HVPT; Nakata 2017 zu Abrufübung; Gomez-Hernandez et al. 2023 zu Apps für Ältere.

Wichtig: Die App ersetzt kein echtes Gespräch und keinen Kurs. Spracherkennung ist ein Übungspartner, kein Prüfer.

## Projektstruktur

```
app/src/main/java/de/sprachgefuehl/app/
  MainActivity.kt              Einstieg, Theme, Motion
  AppViewModel.kt              App-Zustand, Navigation, Fortschritt
  data/                        Content-Modelle, Assets, Fortschritt (JSON), Sicherungsdatei
  data/account/                optionale Supabase-Session, Konfliktlogik, Cloud-Sync, WorkManager
  domain/                      SRS, Scorer, Fehlerdiagnose, Session, Garten, Skills, Check-up,
                               AnswerKey (welche Antwort gilt)
  speech/                      TTS, Spracherkennung, Aufnahme, Wiedergabe
  ui/navigation/               Die vier Tabs
  ui/components/               DesignSystem.kt und Components.kt: Rahmen, Karten, Knöpfe,
                               Press-Feder, Reveal, AnimatedCount, Tagesring, Tab-Leiste
  ui/screens/                  Start, Üben, Stand, Profil, Lektion, Prüfung, Werkstatt, Suche …
  ui/                          Compose-Theme, Farben, Typografie
app/src/main/assets/content/content.json    generierte Inhalte
app/src/test/                 321 JVM-Tests in 47 Klassen, inkl. Content-Validator, TaskAudit,
                              Suche, Skills, Check-up, Sicherungsformat, Aufgabenmix, Übersicht,
                              Streak, Lern-Engine, Farbkontrasten (hell/dunkel) und dem Wächter
                              für alte Speicherstände
tools/                        Inhaltskatalog und Generator
docs/                         Design, Prüfbogen, Test-Checkliste, Store-Vorbereitung, Verteilen
github-vorbereitung/          fertige Release-Pakete (APK, ZIP, Notizen, Prüfsummen)
```

## Bauen

Voraussetzungen: JDK 17, Android SDK mit Platform 35 und Build-Tools 35.

Dieses Projekt bringt eine lokale Toolchain mit (`.toolchain/`), damit nichts systemweit installiert werden muss:

```bash
export JAVA_HOME="$(pwd)/.toolchain/jdk17"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$(pwd)/.toolchain/android-sdk"
export PATH="$ANDROID_HOME/platform-tools:$PATH"
./gradlew :app:assembleDebug
```

`local.properties` verweist auf `.toolchain/android-sdk`. Wer Android Studio nutzt, kann dort seinen eigenen SDK-Pfad eintragen.

Ergebnis: `app/build/outputs/apk/debug/app-debug.apk` (ca. 22 MB, Debug-Signatur).

Für Konten zusätzlich `SUPABASE_URL` und `SUPABASE_ANON_KEY` in `local.properties` eintragen.
Ohne diese Werte bleibt der Build lokal und die Konto-Seite zeigt einen Einrichtungshinweis
([docs/accounts.md](docs/accounts.md)).

Eine signierte Release-APK entsteht, sobald `keystore.properties` im Projektwurzelverzeichnis
liegt: `./gradlew :app:assembleRelease`. Schritt für Schritt steht das in
[docs/store.md](docs/store.md).

## Version und Veröffentlichung

- **2.0** (versionCode 8) ist die erste vollständige Fassung und für den Play Store vorbereitet: Kontraste gemessen und als Test festgehalten, Schalter und Textfelder in der Garten-Palette, Sprachprofil ohne Überlappung, Standortbalken im Dunkeln sichtbar. Signierte APK (ca. 14,6 MB), App-Bundle (AAB), Release-Notizen, Anleitung und Prüfsummen liegen in [`github-vorbereitung/v2.0/`](github-vorbereitung/v2.0/). Store-Texte, Data-Safety-Antworten und die Schritte für die Play Console stehen in [docs/store.md](docs/store.md), die F-Droid-Vorbereitung in [docs/fdroid.md](docs/fdroid.md).
- **1.6** (versionCode 7) brachte zwei neue Aufgabenformen (Antwort auf eine gehörte Zeile, Satzende wählen), die Situation „Beim Friseur" und mehr Abwechslung in kurzen Runden.
- **1.5** (versionCode 6) brachte Tiefe in der Oberfläche: Kanten und Erhebung, ein antwortender Hauptknopf, Garten in Beeten, lesbare Spalte auf dem Tablet.
- Der Prüfsummenstand steht in `github-vorbereitung/v2.0/SHA256SUMS_v2.0.txt`, die Reihenfolge der Schritte in `github-vorbereitung/v2.0/ANLEITUNG-GITHUB.md`.
- Ältere Zwischenpakete werden nicht aufgehoben; `github-vorbereitung/README.md` nennt die jeweils gültige Fassung.
- Für eine erste Runde mit echten Lernenden: [docs/testleitfaden.md](docs/testleitfaden.md).

## Updates und alter Speicherstand

Jedes Update – neue Lektionen, größerer Aufgabenpool, neue Einstellung – muss mit dem vorhandenen Fortschritt zusammenpassen. Die Regeln und der Test, der sie erzwingt, stehen in [docs/kompatibilitaet.md](docs/kompatibilitaet.md): alte Dateien laden mit Defaults, verschwundene Inhalte werden beim Laden aussortiert, eine angefangene Lektion bleibt erhalten, solange ihre Sätze existieren, und neuer Inhalt gilt als neu statt als geschafft.

## Tests

```bash
./gradlew :app:testDebugUnitTest
```

321 Tests in 47 Klassen prüfen: die Kontraste beider Darstellungen (Text, Sekundärtext, Knöpfe, Pillen, Warn- und Fehlerfarbe, Kartenkanten, Garten), SRS-Intervalle und Stufen, Scoring (inkl. Kontraktionen und Zahlwörter), Fehlerdiagnose-Regeln, Lektionsreihenfolge, Review-Queue, Remediation-Sortierung, Gartenregeln ohne Verlust, die Glättung des Sprachprofils, die Check-up-Treppe inklusive Pause und Fortsetzen, die stabile Variantenwahl, den Tagesplan-Fokus, die Suche über deutsche und englische Inhalte, die Fortschrittslogik, Tagesziel und Pausentage, das Sicherungsformat (Paket mit Aufnahmen, alte JSON-Dateien, Abweisung fremder Dateien), die Sync-Entscheidungen und Aufnahme-Pfade sowie die Vollständigkeit aller Inhalte (IDs, Varianten je Situation, Prüfaufgaben je Bereich und Stufe, Sprechaufgaben, Fehlerkarten, Muster).

`ProgressUpgradeTest` ist der Wächter für Updates (siehe [docs/kompatibilitaet.md](docs/kompatibilitaet.md)): kein gespeichertes Feld ohne Default, alte Dateien laden, verschwundene Inhalte werden aussortiert, eine angefangene Lektion bleibt, und die adaptiven Teile rechnen auf einem alten Stand weiter.

Dazu kommt `TaskAuditTest`: Er prüft die 2.128 von Hand geschriebenen Schritte und 477 generierten Runden darauf, dass jede Aufgabe lösbar ist, eine eindeutige Lösung hat und die Runde abwechslungsreich bleibt. Wo der Katalog keine Lösung mitliefert (Verbinden, Klangpaar, schnelles Sprechen), leitet `AnswerKey` sie mit ausdrücklichen Regeln ab – ist keine eindeutig, gilt nichts als falsch.

## Inhalte bearbeiten

Die Inhalte sind bewusst als Datenkatalog gepflegt, nicht im Code:

```
tools/core.mjs            Pfade, Grammatik-Säulen, Konnektoren, Funktionskarten,
                          Fehlerkarten, Minimalpaare
tools/lib.mjs             Bausteine für die Katalogdateien (S, L, q, chunk)
tools/situations1.mjs     Gesundheit, Leute, Einkaufen
tools/situations2.mjs     Unterwegs, Alltag, Wenn’s hakt
tools/situations5.mjs     Behörden und Feste
tools/situations3.mjs     Restaurant und weitere Gelegenheiten (Staffel 2)
tools/situations4.mjs     Post und weitere Gelegenheiten (Staffel 2)
tools/expand-content.js  Trägt Zusatz-Situationen in content.json ein (idempotent)
tools/new-lessons.js     Arbeit & Büro, Reisen & Urlaub, Wohnen & Nachbarn
tools/new-lessons-b.js    Telefon & Digitales, Sport & Freizeit, Notfälle & Hilfe
tools/new-lessons-c.js    Beim Friseur: Termin, Wunsch, Bezahlen
tools/new-numbers.js     Zahlen, Uhrzeiten, Preise, Datum, Telefonnummern
tools/new-extras.js       Fehlerkarten, Minimalpaare, Standort-Aufgaben
tools/variants.mjs        zweiter und dritter Erzählrahmen je Situation
tools/variants2.mjs       weitere Erzählrahmen
tools/checkup.mjs         Prüfpool der Standortbestimmung
tools/generate.mjs        baut Schritte, Muster, can-do-Liste und validiert
```

```bash
node tools/generate.mjs
```

Der Generator bricht ab, wenn eine Lektion keine Sprechaufgabe hat, eine ID doppelt ist oder
eine Fehlerkarte unvollständig ist. Zusätzlich entsteht `docs/content-review.md`: ein
deutscher Prüfbogen mit allen Sätzen – zum Gegenlesen mit Muttersprachlern.

## Bewusste Abweichungen vom ursprünglichen Plan

- **Ein Modul statt fünf.** Die Trennung liegt in Paketen (`data`, `domain`, `speech`, `ui`).
  Das reduziert Build-Risiko, die Testbarkeit bleibt erhalten.
- **Fortschritt als atomar geschriebene JSON-Datei statt Room.** Der Inhalt ist statisch, der
  Fortschritt klein (Chunks, Fehlerzähler, Sitzungen). Kein Datenbankmigrationsthema, weniger
  Build-Abhängigkeiten. `ProgressStore` schreibt temporär und benennt atomar um.
- **Kein Vosk-Modell eingebaut.** Die App nutzt die On-Device-Erkennung von Android (API 31+),
  sonst die Systemerkennung, und fällt bei Fehlern auf eine ehrliche Selbsteinschätzung zurück.
  Vosk bleibt als Option offen, kostet aber ca. 40 MB und zusätzliche native Bibliotheken.
- **Kein Baseline Profile / Macrobenchmark.** Die App ist klein und textlastig; die
  Frame-Messung wurde nicht als eigener Benchmark-Modul aufgesetzt.
- **Signatur nur auf Wunsch.** Ohne `keystore.properties` bleibt es bei der debug-signierten APK
  für Sideload. Sobald die Datei da ist, baut `assembleRelease` eine signierte Version
  (`docs/store.md`).

## Datenschutz

Keine Werbung, keine Analytics. Ohne Konto verlassen Fortschritt und Aufnahmen das Gerät nicht.
Die Internet-Berechtigung existiert nur für die optionale Anmeldung und Cloud-Sicherung; ohne
Konto wird sie nicht genutzt. Mit Konto liegen die Daten in deinem Supabase-Projekt und sind
über RLS ausschließlich deiner bestätigten E-Mail-Adresse zugeordnet.

Wer das Gerät wechselt, kann in den Einstellungen eine Sicherungsdatei erstellen und sie auf dem
neuen Handy wieder laden. Was in der Datei liegt und wie der Umzug läuft:
[docs/backup.md](docs/backup.md). Die optionale Cloud-Einrichtung steht in
[docs/accounts.md](docs/accounts.md). Die vollständige Datenschutz-Erklärung, wie sie im Store
verlinkt wird, steht in [docs/datenschutz.md](docs/datenschutz.md).

## Lizenz

Der Quellcode steht unter der **Apache-Lizenz 2.0** ([LICENSE](LICENSE)). Du darfst die App
weitergeben und verändern, solange Copyright- und Lizenzhinweis erhalten bleiben. Die Inhalte
(Sätze, Erklärungen, Dialoge) stammen aus diesem Projekt; die englischen Sätze sind für den
Übungsbetrieb geschrieben, nicht aus einem Lehrwerk übernommen.

## Grenzen

- Spracherkennung hängt vom Gerät ab und versteht ältere Stimmen unterschiedlich gut. Deshalb
  nie ein blockierendes „falsch“, sondern drei Stufen und ein Selbstcheck als Rückfallebene.
- TTS-Stimmen klingen je nach Handy unterschiedlich. Fehlt eine englische Stimme, zeigt die
  App einen Einrichtungshinweis.
- Die Inhalte sind fachlich strukturiert, aber nicht von einer Lehrkraft redigiert. Der
  Prüfbogen ist dafür da.
