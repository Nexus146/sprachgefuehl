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
  sechs Teilen statt eines roten Kreuzes. Kein Konto, keine Werbung, keine Serien, keine
  Internet-Berechtigung.
- **Kategorie:** Lernen / Bildung
- **Zielgruppe:** Erwachsene, besonders Menschen ab 60. Kein Kinderangebot, keine Inhalte für
  Kinder unter 13.
- **Screenshots:** Startseite, Lektion (Hören), Sprechaufgabe mit Rückmeldung, Fehlerkarte,
  Profil, Suche. je 1080 × 2280 px.

## 3. Data Safety (Play Console)

| Frage | Antwort |
|---|---|
| Werden Daten erhoben oder geteilt? | Nein. Kein Konto, keine Übertragung, keine Weitergabe, keine Werbung. |
| Werden Daten verschlüsselt übertragen? | Es werden keine Daten übertragen. Die Sicherungsdatei legt der Nutzer selbst ab. |
| Können Nutzer die Löschung verlangen? | Ja, jederzeit selbst: Einstellungen → Fortschritt zurücksetzen löscht alle lokalen Daten, Deinstallation löscht sie mit. |
| Wird das Mikrofon genutzt? | Ja, nur während einer Sprechaufgabe, nur on-device. Keine Aufnahmen werden gesendet. |
| Aufnahmen | Bleiben im privaten App-Verzeichnis, optional in „Meine Sätze“. |
| Kinder | Kein Angebot für Kinder; Inhalte für Erwachsene. |

Berechtigungen im Manifest: nur `RECORD_AUDIO`. Es gibt bewusst **keine** Netzwerk-Berechtigung –
damit ist im Data-Safety-Formular „keine Datenübertragung“ belegbar. TTS und Spracherkennung laufen
über die Systemdienste des Geräts.

## 4. Inhaltliche Prüfung vor dem Upload

- `docs/content-review.md` mit einer englischen Muttersprachlerin gegenlesen.
- Fehlerkarten und Grammatik-Erklärungen auf deutsche Formulierungen prüfen (Sie/Du konsistent).
- Prüfen, dass jede Lektion eine Sprechaufgabe hat (`node tools/generate.mjs` bricht sonst ab).

## 5. Was bewusst fehlt

- Kein Konto und kein Server. Gerätewechsel läuft über eine Sicherungsdatei, die der Nutzer
  selbst kopiert (Details: [backup.md](backup.md)).
- Keine Analytics und keine Crash-Berichte. Fehler werden über die Test-Checkliste gefunden.
- Kein Abo, keine Käufe.
