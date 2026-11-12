# F-Droid – was für eine Aufnahme nötig ist

Stand: 12. November 2026. Die App ist **noch nicht** eingereicht; hier steht, was dafür fehlt und
was schon vorbereitet ist. F-Droid prüft jede App von Hand, mit Wochen Vorlauf.

## Schon erledigt (in diesem Repo)

| Voraussetzung | Stand |
|---|---|
| Freie Lizenz | `LICENSE` (Apache-2.0) im Wurzelverzeichnis |
| Bau aus dem Quellcode | `./gradlew assembleRelease` baut ohne vorbereitete Binärdateien |
| Keine Binärdateien im Repo | `github-vorbereitung/` und `screenshots/` stehen in `.gitignore` |
| Store-Metadaten | `fastlane/metadata/android/de-DE/` mit Titel, Kurz-/Langtext, Changelog 8, Icon und Screenshots |
| Keine proprietären Abhängigkeiten | Jetpack Compose, Kotlin, kotlinx, Supabase-Kotlin und Ktor – alle Apache-2.0 |
| Optionaler Cloud-Dienst | nur mit eigener Supabase-Konfiguration; ohne sie bleibt die App vollständig lokal |

## Was noch fehlt (deine Schritte)

1. **Quellcode öffentlich und vollständig.** Der Branch `main` auf GitHub muss Version 2.0
   enthalten (versionCode 8). F-Droid baut genau diesen Stand.
2. **Konto bei GitLab.com** (kostenlos) für den Merge-Request.
3. **Merge-Request im Repo `fdroiddata`** anlegen: Datei `metadata/de.sprachgefuehl.app.yml` nach
   der Vorlage unten, dazu ein Commit mit „New app: Sprachgefühl".
4. **Warten:** Erste Reviews dauern erfahrungsgemäß mehrere Wochen bis Monate. Nachfragen im
   Merge-Request sind willkommen, Beschleunigung gibt es nicht.

## Vorlage für `metadata/de.sprachgefuehl.app.yml`

```yaml
Categories:
  - Science & Education
License: Apache-2.0
AuthorName: Sprachgefühl
SourceCode: https://github.com/Nexus146/sprachgefuehl
IssueTracker: https://github.com/Nexus146/sprachgefuehl/issues
Changelog: https://github.com/Nexus146/sprachgefuehl/releases

AutoName: Sprachgefühl

RepoType: git
Repo: https://github.com/Nexus146/sprachgefuehl.git

Builds:
  - versionName: '2.0'
    versionCode: 8
    commit: v2.0
    subdir: app
    gradle:
      - yes

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: '2.0'
CurrentVersionCode: 8

AntiFeatures:
  - NonFreeNet
```

`NonFreeNet` ist ehrlich: Die App kann optional ein Supabase-Projekt ansprechen. Ohne
Konfiguration (`SUPABASE_URL` leer) bleibt sie rein lokal – so baut F-Droid sie auch.

## Zwei Eigenheiten, die man vorher wissen sollte

- **Spracherkennung** nutzt den Systemdienst des Geräts. Auf Geräten ohne Offline-Paket kann das
  den Google-Dienst bedeuten; F-Droid verbietet das nicht, weist aber darauf hin.
- **Signatur:** F-Droid signiert mit eigenem Schlüssel. Wer von der GitHub-APK kommt, muss einmal
  deinstallieren (Sicherung vorher erstellen!). Deshalb lohnt F-Droid nur als *zusätzlicher* Weg,
  nicht als Ersatz für den Obtainium-Weg.
