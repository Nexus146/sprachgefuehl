# Veröffentlichung vorbereiten

Diese Notiz sammelt alles, was für eine signierte Version und einen Store-Eintrag nötig ist.
Die App bleibt auch ohne Store nutzbar: eine signierte APK reicht zum Installieren.

## 1. Signierten Build erzeugen

Einmalig einen Schlüssel anlegen (niemals ins Repository legen):

```bash
.keytool/jdk/bin/keytool -genkeypair -v \
  -keystore release-keystore.jks -alias sprachgefuehl \
  -keyalg RSA -keysize 2048 -validity 10000
```

Danach im Projektwurzelverzeichnis `keystore.properties` anlegen:

```properties
storeFile=release-keystore.jks
storePassword=…
keyAlias=sprachgefuehl
keyPassword=…
```

```bash
./gradlew :app:assembleRelease
```

Ergebnis: `app/build/outputs/apk/release/app-release.apk`.

Fehlt `keystore.properties`, baut Gradle die Release-Variante ohne Signatur. Der Debug-Build ist
davon nicht betroffen. `release-keystore.jks` und `keystore.properties` gehören in `.gitignore`
und in ein Backup – ohne den Schlüssel sind keine Updates mehr möglich.

Vor dem Hochladen:

1. `versionCode` in `app/build.gradle.kts` erhöhen, `versionName` anpassen.
2. `./gradlew :app:testDebugUnitTest` grün.
3. `docs/test-checklist.md` einmal auf einem echten Gerät durchgehen.
4. Flugmodus-Test: Die App funktioniert vollständig offline; Vorlesen kommt von der
   System-TTS-Stimme des Geräts.

## 2. Store-Eintrag

- **Name:** Sprachgefühl – Englisch sprechen
- **Kurzbeschreibung (80 Zeichen):** Englisch verstehen und selbst sprechen – ruhig, offline, ohne Zeitdruck.
- **Volltext (Auszug):** Jede Lektion beginnt mit einem Alltagsgespräch, erklärt eine Form auf
  Deutsch, übt Bausteine und lässt dich selbst sprechen. Bei Fehlern gibt es eine Erklärung in
  sechs Teilen statt eines roten Kreuzes. Ohne Konto, ohne Werbung, ohne Serien. Ein Konto ist
  optional und nur für die Cloud-Sicherung nötig.
- **Kategorie:** Lernen / Bildung
- **Zielgruppe:** Erwachsene jeden Alters, die ohne Druck lernen wollen. Kein Kinderangebot, keine Inhalte für
  Kinder unter 13.
- **Screenshots:** Startseite, Lektion (Hören), Sprechaufgabe mit Rückmeldung, Fehlerkarte,
  Profil, Suche. je 1080 × 2280 px.

## 3. Data Safety (Play Console)

| Frage | Antwort |
|---|---|
| Werden Daten erhoben oder geteilt? | Ohne Konto: nein. Mit Konto: E-Mail, Fortschritt und optional Aufnahmen werden zum gewählten Supabase-Projekt übertragen, nicht an Dritte weitergegeben. Keine Werbung. |
| Werden Daten verschlüsselt übertragen? | Ja, die optionale Cloud-Verbindung nutzt HTTPS/TLS. Die Sicherungsdatei legt der Nutzer selbst ab und ist nicht verschlüsselt. |
| Können Nutzer die Löschung verlangen? | Ja: Einstellungen → Konto und Cloud → Konto löschen entfernt Cloud-Daten und Auth-Konto. Fortschritt zurücksetzen/Deinstallation löscht die lokalen Daten. |
| Wird das Mikrofon genutzt? | Ja, nur während einer Sprechaufgabe, nur on-device. Aufnahmen werden nur bei aktivem Cloud-Sync übertragen. |
| Aufnahmen | Bleiben ohne Konto im privaten App-Verzeichnis; mit Konto optional im privaten Storage-Bucket. |
| Kinder | Kein Angebot für Kinder; Inhalte für Erwachsene. |

Berechtigungen im Manifest: `RECORD_AUDIO` und `INTERNET`. Die Netzwerk-Berechtigung wird nur für
die freiwillige Anmeldung und Cloud-Sicherung verwendet; ohne Konto bleibt die App lokal. TTS und
Spracherkennung laufen über die Systemdienste des Geräts. Im Data-Safety-Formular muss die
optionale Konto-Nutzung deshalb als Datenübertragung angegeben werden.

## 4. Inhaltliche Prüfung vor dem Upload

- `docs/content-review.md` mit einer englischen Muttersprachlerin gegenlesen.
- Fehlerkarten und Grammatik-Erklärungen auf deutsche Formulierungen prüfen (Sie/Du konsistent).
- Prüfen, dass jede Lektion eine Sprechaufgabe hat (`node tools/generate.mjs` bricht sonst ab).

## 5. Was bewusst fehlt

- Kein verpflichtendes Konto und kein eigener Server. Gerätewechsel läuft ohne Konto über eine
  Sicherungsdatei (Details: [backup.md](backup.md)); optional über Supabase
  ([accounts.md](accounts.md)).
- Keine Analytics und keine Crash-Berichte. Fehler werden über die Test-Checkliste gefunden.
- Kein Abo, keine Käufe.

---

# Play-Store-Paket für 2.0

Alles hier ist zum Kopieren gedacht. Die Grafiken liegen in [`store/`](../store), die
Screenshots sind echte Aufnahmen aus der signierten App (1080 × 2280).

## 6. Dateien für die Play Console

| Feld in der Konsole | Datei / Wert |
|---|---|
| App-Bundle | `github-vorbereitung/v2.0/sprachgefuehl-2.0.aab` |
| App-Symbol (512 × 512) | `store/icon-512.png` |
| Feature-Grafik (1024 × 500) | `store/feature-1024x500.png` |
| Telefon-Screenshots (1080 × 2280) | `store/screenshots/1-start.png` … `6-sammlung.png` |
| Datenschutz-URL | siehe Abschnitt 9 (GitHub Pages) |

## 7. Texte zum Einfügen

**App-Name (30 Zeichen):**

```
Sprachgefühl
```

**Kurzbeschreibung (80 Zeichen):**

```
Englisch verstehen und selbst sprechen – ruhig, offline, ohne Zeitdruck.
```

**Vollständige Beschreibung (max. 4000 Zeichen):**

```
Sprachgefühl ist eine ruhige Englisch-App für Erwachsene, die im Alltag sprechen wollen – nicht
für Punktesammler.

Jede Lektion beginnt mit einem kurzen Gespräch aus dem Alltag: beim Arzt, an der Tür, im Laden,
am Telefon. Du hörst es in zwei Tempi, liest die Übersetzung, wenn du willst, und erkennst die Form,
die dahintersteckt – auf Deutsch erklärt, mit dem Kontrast zur eigenen Sprache.

Dann sprichst du selbst: nachsprechen, Lücken füllen, den Satz bauen, in eine neue Situation
übertragen. Die App hört zu und antwortet in drei Stufen statt mit rotem Kreuz. Bei einem Fehler
gibt es eine Erklärung in sechs Teilen: was passiert ist, warum es anders ist, wie es richtig geht,
einen Merkanker, eine Checkliste fürs nächste Mal und den nächsten Schritt.

Was die App besonders macht:

• Kein Zeitdruck, keine Herzen, keine Rangliste. Ein Pausentag nimmt nichts weg.
• Offline nutzbar. Ohne Konto verlässt nichts das Gerät; die Internet-Berechtigung wird nur für
die freiwillige Cloud-Sicherung gebraucht.
• Keine Werbung, keine Analytics, keine Abos.
• Fünf Bereiche statt einer Zahl: Hören, Grammatik, Bausteine, Sprechen, Verknüpfen – jeder mit
  einer Stufe in Worten und dem nächsten Schritt.
• Ein Sprachgarten, der wächst und nie verwelkt.
• Große Schrift, ruhige Farben, heller und dunkler Modus, gemessene Kontraste.
• Eigene Aufnahmen zum Vergleichen mit der Modellstimme.
• Fortschritt auf ein neues Handy: über eine Sicherungsdatei oder ein optionales Konto.

15 Kapitel, 52 Situationen, 186 Lektionen: Gesundheit, Leute und Small Talk, Einkaufen und Essen,
Unterwegs, Alltag und Zuhause, Behörden und Termine, Feste und Einladen, Arbeit und Büro, Reisen,
Wohnen und Nachbarn, Telefon und Digitales, Sport und Freizeit, Notfälle – dazu eine Werkstatt für
Grammatik, Klangpaare und Zahlen.

Spracherkennung und Stimme kommen vom Gerät; sie klingen je nach Handy unterschiedlich. Die App
ersetzt kein echtes Gespräch und keinen Kurs – sie übt den Moment davor.
```

**Was ist neu (Version 2.0):**

```
Version 2.0 macht die App fertig für den Alltag:

• Dunkelmodus überarbeitet: gemessene Kontraste (mindestens 4,5:1 für Text, 3:1 für Kanten und
  Symbole), eigene Gartenfarben für beide Darstellungen.
• Sprachprofil: Bereich und Stufe stehen jetzt untereinander und überlagern sich nicht mehr.
• Standortbestimmung: der Fortschrittsbalken ist im Dunkeln sichtbar.
• Schalter, Textfelder und Dialoge folgen der warmen Farbwelt statt der System-Violetttöne.
• 321 automatische Tests, 47 Testklassen, Kontrast-Wächter für hell und dunkel.
```

## 8. Data Safety – Antworten je Frage

| Frage | Antwort |
|---|---|
| Erhebt oder teilt die App Nutzerdaten? | Ja, aber nur bei freiwilligem Konto: E-Mail-Adresse, Lernstand, optional Aufnahmen. Ohne Konto: nein. |
| Zweck | App-Funktionalität, Kontoverwaltung, Sicherung. Keine Werbung, keine Analyse. |
| Wird die Übertragung verschlüsselt? | Ja, HTTPS/TLS. |
| Kann die Nutzerin die Löschung verlangen? | Ja, direkt in der App („Konto löschen" bzw. „Fortschritt zurücksetzen"). |
| Mikrofon | Ja, nur während einer Sprechaufgabe; Aufnahmen bleiben lokal, außer Cloud-Sync ist aktiv. |
| Standort, Kontakte, Fotos, Werbe-ID | Nein, nicht verwendet. |
| Datenverkauf | Nein. |

## 9. Datenschutz-URL einrichten (GitHub Pages)

Die Erklärung liegt als [datenschutz.md](datenschutz.md) im Repo. Für die Konsole braucht es eine
öffentliche Webseite:

1. Auf GitHub das Repo öffnen → **Settings → Pages**.
2. Unter **Source** „Deploy from a branch" wählen, Branch `main`, Ordner `/docs`, **Save**.
3. Nach ein bis zwei Minuten ist die Seite erreichbar:
   `https://Nexus146.github.io/sprachgefuehl/datenschutz`
4. Diesen Link im Play-Formular „Datenschutzrichtlinie" eintragen.

## 10. Inhaltsbewertung (Fragebogen)

| Frage | Antwort |
|---|---|
| Gewalt, Angst, Sex, Drogen, Glücksspiel | nein |
| Nutzerinteraktion, Chats, Standortfreigabe | nein |
| Käufe | nein |
| Werbung | nein |
| Datenerhebung | nur freiwilliges Konto (siehe Data Safety) |

Erwartetes Ergebnis: freigegeben ab 0/6 bzw. USK 0, „Für alle Altersgruppen".

## 11. Closed Test – was Google verlangt

Neue private Entwicklerkonten müssen vor der Produktionsfreigabe **12 Tester über 14 Tage**
begleiten. Praktisch heißt das:

1. Konto anlegen (25 $ einmalig), Identität bestätigen lassen.
2. In der Konsole: **Testing → Closed testing → Create track**, AAB hochladen.
3. 12 Personen per E-Mail-Liste einladen, sie installieren über den Test-Link.
4. 14 Tage warten, in denen die App bei mindestens einer Person installiert bleibt.
5. Danach **Apply for production** – Google prüft einige Tage.

Bis dahin bleibt der GitHub-Release der Weg, der sofort funktioniert.
