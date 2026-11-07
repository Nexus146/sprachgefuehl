# Verteilen und aktualisieren

Die App wird ohne Store verteilt: **GitHub Releases** ist das Lager für die APK,
**Obtainium** auf dem Handy des Lernenden prüft und installiert Updates. Das kostet nichts und
braucht kein Konto bei Google.

Der wichtigste Punkt zuerst: **Ein Update behält den Fortschritt von selbst.** Export und Import
der Sicherungsdatei braucht man nur beim Handywechsel oder nach einer Deinstallation – nicht bei
jedem Update. Das funktioniert, solange drei Dinge gleich bleiben:

1. der Paketname `de.sprachgefuehl.app`,
2. **derselbe Signaturschlüssel**,
3. drüberinstallieren statt vorher deinstallieren.

Punkt 2 ist die einzige Stelle, die man nicht reparieren kann. Deshalb Schritt 1 vor allem anderen.

> **Welche Konsole?** Die Befehle stehen in zwei Fassungen. PowerShell gehört zu Windows und ist
> der Weg des geringsten Widerstands. Wichtig: **kein `\` am Zeilenende** – PowerShell kennt diese
> Fortsetzung nicht und führt jede Zeile einzeln aus. Entweder alles in einer Zeile schreiben oder
> die Bash-Fassung in der **Git Bash** verwenden (die kommt mit Git für Windows).

---

## Einmalige Einrichtung

### 1. Signaturschlüssel anlegen

Warum jetzt und nicht später: Die bisherige APK ist mit dem automatischen Debug-Schlüssel
signiert. Den erzeugt Android Studio pro Rechner neu, ohne Passwort. Ändert er sich, verweigert
Android das Update („App nicht installiert“), und der einzige Ausweg ist Deinstallieren – dabei
geht der Lernstand verloren. Mit einem eigenen Schlüssel gehört er dir, dauerhaft.

Im Projektordner ausführen – **eine Zeile**, kopierbar:

PowerShell:

```powershell
.\\.toolchain\\jdk17\\bin\\keytool -genkeypair -v -keystore release-keystore.jks -alias sprachgefuehl -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Sprachgefuehl, O=Privat, C=DE"
```

Git Bash:

```bash
./.toolchain/jdk17/bin/keytool -genkeypair -v \
  -keystore release-keystore.jks \
  -alias sprachgefuehl \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -dname "CN=Sprachgefuehl, O=Privat, C=DE"
```

Das Werkzeug fragt nur nach dem Passwort (zweimal zur Bestätigung). Die `-dname`-Angaben sind
Beigaben fürs Zertifikat, sie müssen nichts bedeuten. Wichtig:

- **Ein starkes Passwort, das du behältst.** Bei einem PKCS12-Schlüssel (Standard) gilt dasselbe
  Passwort für Schlüssel und Speicher.
- Keine Sonderzeichen wie `#`, `=` oder `:` und keine Leerzeichen am Anfang oder Ende – sonst
  muss man sie in der Konfigurationsdatei maskieren.

Danach im Projektordner die Datei `keystore.properties` anlegen (sie ist in `.gitignore`, landet
also nie im Repo):

```properties
storeFile=release-keystore.jks
storePassword=DEIN_PASSWORT
keyAlias=sprachgefuehl
keyPassword=DEIN_PASSWORT
```

**Jetzt sichern, bevor irgendetwas anderes passiert:** `release-keystore.jks` **und** das Passwort
an zwei getrennte Orte legen (zwei USB-Sticks, oder Passwortmanager + Cloud-Ordner). Geht der
Schlüssel verloren, sind für niemanden mehr Updates möglich – nur noch Neuinstallationen mit
Datenverlust.

### 2. Signierte Release-APK bauen

PowerShell:

```powershell
$env:JAVA_HOME = (Resolve-Path .\.toolchain\jdk17).Path
$env:PATH = "$($env:JAVA_HOME)\bin;" + $env:PATH
.\gradlew.bat :app:assembleRelease
```

Git Bash:

```bash
export JAVA_HOME="$(cygpath -w "$(pwd)/.toolchain/jdk17")"
export PATH="$(pwd)/.toolchain/jdk17/bin:$PATH"
./gradlew :app:assembleRelease
```

Ergebnis: `app/build/outputs/apk/release/app-release.apk` (ca. 11–12 MB, deutlich kleiner als die
Debug-Version mit 17 MB).

Prüfen, dass wirklich der eigene Schlüssel drinsteckt:

```powershell
.\\.toolchain\\android-sdk\\build-tools\\35.0.0\\apksigner.bat verify --print-certs app\\build\\outputs\\apk\\release\\app-release.apk
```

In der Ausgabe muss der Fingerabdruck deines Schlüssels stehen (nicht `CN=Android Debug`). Diesen
SHA-256-Fingerabdruck notierst du dir – daran erkennst du später, ob eine APK von dir stammt.

### 3. Git-Repo anlegen und hochladen

```bash
git init
git add .
git status          # kontrollieren, was mitkommt
git commit -m "Sprachgefühl 1.0"
```

> **Bereits erledigt.** Das lokale Repo existiert, der erste Commit `Sprachgefühl 1.0` liegt
drin, unterschrieben mit `Sprachgefuehl <sprachgefuehl@users.noreply.github.com>` (nur lokal
gesetzt, nicht global). Es fehlt nur noch das Hochladen zu GitHub.

Vor dem Hochladen prüfen, dass **nicht** dabei ist (steht alles in `.gitignore`):

| Nicht ins öffentliche Repo | Warum |
|---|---|
| `release-keystore.jks`, `keystore.properties` | damit könnte jeder gefälschte Updates bauen |
| `.cloud-attachments/` | enthält Screenshots mit deiner E-Mail-Adresse und eine alte Chat-Sicherung mit Zugangsdaten aus dem verworfenen Konto-Versuch |
| `local.properties`, `.toolchain/`, `app/build/` | Maschinenpfade und Build-Ausgabe |
| `screenshots/` (43 MB) | reines Arbeitsmaterial |

Dann auf github.com ein neues Repository anlegen:

- Name z. B. `sprachgefuehl`
- Sichtbarkeit **Public** – Anhänge eines privaten Repos sind nur nach Anmeldung
  herunterladbar, und genau das soll deine Oma nicht brauchen
- **ohne** README, ohne .gitignore, ohne Lizenz (das Repo ist leer)

```bash
git remote add origin https://github.com/DEIN_NAME/sprachgefuehl.git
git branch -M main
git push -u origin main
```

Wer den Quellcode nicht öffentlich haben will, legt stattdessen ein leeres öffentliches Repo
`sprachgefuehl-releases` an und lädt dort nur die APK hoch. Alles andere bleibt gleich.

### 4. Erstes Release anlegen

Auf der GitHub-Seite des Repos: **Releases → Draft a new release**.

| Feld | Eintrag |
|---|---|
| Tag | `v1.0` |
| Titel | `Version 1.0` |
| Beschreibung | ein bis drei Sätze, was drin ist |
| Anhänge | die Datei `app-release.apk` |

**„Publish release“** drücken. Der Anhang ist danach ohne Anmeldung herunterladbar.

Der Link, den du weitergibst, ist immer derselbe und zeigt immer auf die neueste Version:

```
https://github.com/DEIN_NAME/sprachgefuehl/releases/latest
```

### 5. Obtainium auf dem Handy einrichten

Obtainium ist der Updater. Es braucht kein Konto. Sprachgefühl hat Internet nur für die optionale
Anmeldung und Cloud-Sicherung; ohne Konto wird nichts übertragen. Obtainium braucht Netz für
Updates.

1. Obtainium installieren: [github.com/ImranR98/Obtainium/releases](https://github.com/ImranR98/Obtainium/releases),
   die Datei `Obtainium-….apk` herunterladen und installieren (gleiche Rückfragen wie unten).
2. Obtainium öffnen, oben auf **+** tippen.
3. Die Repo-Adresse eintragen: `https://github.com/DEIN_NAME/sprachgefuehl` → **Add**.
4. Android fragt einmal nach der Erlaubnis „Apps aus dieser Quelle installieren“ – erlauben.
5. In Obtainium bei Sprachgefühl „Update automatically“ einschalten, damit es im Hintergrund
   nachsieht. Ohne diese Einstellung meldet es nur.

### 6. Was beim ersten Installieren angetippt werden muss

Das ist der einzige unangenehme Moment, und er wiederholt sich nicht:

| Bildschirm | Was sie tippt |
|---|---|
| Chrome: „Diese Datei kann dein Gerät schädigen“ | **Trotzdem herunterladen** |
| Download fertig | **Öffnen** |
| „Installation aus unbekannter Quelle“ | **Einstellungen** → Schalter für Chrome/Dateien **erlauben** → zurück |
| Play Protect: „App wurde nicht geprüft“ | **Mehr Details** → **Trotzdem installieren** |
| Installiert | **Öffnen** |

Der Play-Protect-Hinweis ist normal bei Apps außerhalb des Play Stores. Er erscheint bei jedem
neuen Update wieder, lässt sich aber nicht abkürzen.

---

## Bei jedem Update

1. **Version hochzählen** in [app/build.gradle.kts](../app/build.gradle.kts):
   `versionCode = 2`, `versionName = "1.1"`. Ohne neue `versionCode` erkennt Obtainium kein
   Update – Android lehnt es sonst irgendwann ab.
2. Release bauen wie in Schritt 2.
3. GitHub → **Releases → Draft a new release**: Tag `v1.1`, Titel `Version 1.1`, ein Satz dazu,
   neue APK anhängen, **publish**.
4. Auf dem Handy: Obtainium meldet „Update verfügbar“ oder installiert selbst. Tipp auf
   **Aktualisieren** – **nicht** deinstallieren.

Kontrolle, welche Version installiert ist: in der App **Einstellungen → Über** steht
„Sprachgefühl 1.1 · offline nutzbar …“. Diese Zeile liest sich am Telefon vor, wenn du wissen
willst, ob das Update angekommen ist.

---

## Was der Fortschritt aushält

| Vorgang | Lernstand, Sätze, Aufnahmen |
|---|---|
| Update über die bestehende App (gleicher Schlüssel) | **bleiben erhalten** |
| Deinstallation, dann Neuinstallation | verloren – vorher Sicherung erstellen |
| Neues Handy | über Sicherungsdatei (Einstellungen → Deine Daten) |
| Wechsel des Signaturschlüssels | verloren – Android verlangt Deinstallation |

Deshalb: einmal gemeinsam „Sicherung erstellen“ zeigen, bevor das erste Update kommt. Die Datei
ist die Versicherung für alles außer dem normalen Update.

---

## Wenn etwas schiefgeht

| Meldung | Ursache | Was tun |
|---|---|---|
| „App nicht installiert“ | andere Signatur als die installierte, oder `versionCode` kleiner als die vorhandene | richtige `keystore.properties` prüfen. Wenn die Signatur wirklich gewechselt hat: **in der alten App erst Sicherung erstellen**, dann deinstallieren, neu installieren, Sicherung laden |
| Obtainium findet nichts | APK nicht am Release **anghängt**, oder Repo privat | Release bearbeiten, APK anhängen; Repo auf public stellen |
| Obtainium meldet kein Update | `versionCode` nicht erhöht | hochzählen, neu bauen, neues Release |
| Play Protect blockt dauerhaft | App außerhalb des Stores | „Trotzdem installieren“; wenn das Gerät es gar nicht zulässt, ist der Play Store der einzige Weg |
| Download bricht bei 11 MB ab | wackliges Netz | noch einmal; die Datei ist vollständig, wenn sie sich installieren lässt |

---

## Wenn es später größer werden soll

Der Weg oben kostet dauerhaft nichts, hat aber zwei Eigenheiten: die Erstinstallation ist
frickelig, und echte stille Updates erlaubt Android nur eingeschränkt. Sobald die App mehr als
eine Handvoll Menschen erreichen soll, ist der Play Store die bequemere Lösung – 25 $ einmalig,
danach automatische Updates ohne Zutun. Für neue private Konten verlangt Google vor der
Produktionsfreigabe einen geschlossenen Test mit 12 Testern über 14 Tage
([Google-Support](https://support.google.com/googleplay/android-developer/answer/14151465)).

Wichtig für diesen Umstieg: dabei bleibt dein Signaturschlüssel entweder derselbe (Play Signing
mit eigenem Schlüssel hochladen) oder die bestehenden Installationen müssen einmal deinstalliert
werden. Deshalb lohnt es sich, den Schlüssel aus Schritt 1 sorgfältig aufzubewahren, auch wenn
die Verteilung später wechselt.
