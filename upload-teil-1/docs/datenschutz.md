# Datenschutzerklärung – Sprachgefühl

Stand: 12. November 2026 · gilt für Version 2.0 der Android-App „Sprachgefühl"

Kurzfassung: **Ohne Konto verlässt nichts dein Handy.** Es gibt keine Werbung, keine Analyse,
keine Crash-Berichte und keine Profile. Ein Konto ist freiwillig und dient nur der Sicherung.

## 1. Wer verantwortlich ist

Sprachgefühl ist ein privates Projekt. Verantwortlich im Sinne der DSGVO ist die Person, die die
App veröffentlicht; sie ist im Store-Eintrag und im Impressum des Repositorys genannt. Kontakt
für alle Fragen zu Daten: über die Fehler-/Fragen-Seite des Repositorys
(`https://github.com/Nexus146/sprachgefuehl/issues`).

## 2. Welche Daten die App verarbeitet

| Daten | Wo sie bleiben | Wofür |
|---|---|---|
| Name (freiwillig), Lerngrund, Interessen, Tagesziel, Lerntage | nur auf dem Gerät (Datei `progress.json` im privaten App-Ordner) | Anrede und Auswahl der Inhalte |
| Lernstand: Lektionen, Bausteine, Fehlerzähler, Zeiten, Garten, Streak | nur auf dem Gerät | Fortschritt und Wiederholungen |
| Eigene Aufnahmen (Mikrofon) | nur auf dem Gerät, privater App-Ordner | Vorher/Nachher-Vergleich beim Sprechen |
| Einstellungen (Darstellung, Schriftgröße, Stimmtempo) | nur auf dem Gerät | Bedienung |

Ein Gerätekonto, eine Anmeldung oder eine E-Mail-Adresse ist für all das **nicht** nötig.

## 3. Mikrofon und Spracherkennung

- Das Mikrofon wird nur benutzt, wenn du in einer Sprechaufgabe auf „Sprechen" tippst. Android
  fragt vorher um Erlaubnis; ohne Erlaubnis funktioniert die App weiter (Selbsteinschätzung).
- Die Erkennung läuft über den Spracherkennungsdienst deines Geräts. Ist die Erkennung auf dem
  Gerät verfügbar (Android 12 und neuer mit geladenem englischem Sprachpaket), bleibt die
  Sprachaufnahme auf dem Gerät. Fehlt sie, nutzt Android den eingestellten Systemdienst – dabei
  kann der Anbieter dieses Dienstes (je nach Gerät z. B. Google) die Äußerung verarbeiten. Das
  ist eine Funktion von Android, nicht der App; die App speichert daraus nur den erkannten Text
  zur Bewertung.
- Aufnahmen für „So klingst du" werden ausschließlich lokal gespeichert und lassen sich in
  „Meine Sätze" einzeln löschen.

## 4. Sprachausgabe

Die Vorlese-Stimme kommt aus den Text-to-Speech-Diensten des Geräts. Auch hier gilt: Es wird nur
Text übergeben, den die App ohnehin anzeigt.

## 5. Freiwilliges Konto (Cloud-Sicherung)

Ohne Konto ist die App vollständig offline nutzbar. Wer ein Konto anlegt:

- Übertragen werden **E-Mail-Adresse, Passwort-Hash, Lernstand und – nur wenn eingeschaltet –
  die eigenen Aufnahmen** an das in der App eingestellte Supabase-Projekt (Serverstandort nach
  Wahl des Betreibers, in der Regel EU).
- Zweck: den Fortschritt auf ein neues Gerät bringen und gegen Verlust sichern.
- Rechtsgrundlage: deine Einwilligung (Art. 6 Abs. 1 lit. a DSGVO), jederzeit widerrufbar durch
  Löschen des Kontos.
- Übertragung ausschließlich über HTTPS. Zugriff ist über Row-Level-Security auf die eigene,
  bestätigte E-Mail-Adresse beschränkt; Aufnahmen liegen in einem privaten Speicher-Bucket.
- Es findet **kein** Verkauf, keine Weitergabe zu Werbezwecken und keine Profilbildung statt.

## 6. Sicherungsdatei

„Sicherung erstellen" schreibt eine ZIP-Datei (`sprachgefuehl-sicherung.zip`) mit Fortschritt und
Aufnahmen an einen Ort, den du selbst auswählst. Diese Datei ist **nicht verschlüsselt** – sie ist
so privat wie der Ordner, in den du sie legst.

## 7. Was die App nicht tut

- keine Werbung, keine Werbe-IDs
- keine Analyse- oder Tracking-Bibliotheken
- keine Crash-Berichte an Dritte
- keine Standortabfrage, keine Kontakte, kein Zugriff auf Fotos
- keine Datenübertragung ohne Konto – die Internet-Berechtigung wird nur für Anmeldung und
  Cloud-Sicherung genutzt

## 8. Speicherdauer und Löschung

- Lokale Daten bleiben, bis du sie löschst: **Einstellungen → Fortschritt zurücksetzen** oder
  Deinstallation der App (Android entfernt dabei den privaten App-Ordner).
- Konto-Daten bleiben, bis du sie löschst: **Einstellungen → Konto und Cloud → Konto löschen**
  entfernt Cloud-Fortschritt, Aufnahmen und das Anmeldekonto. Der lokale Fortschritt bleibt.
- Sicherungsdateien löschst du dort, wo du sie abgelegt hast.

## 9. Kinder

Die App richtet sich an Erwachsene, die Englisch lernen wollen. Sie ist kein Kinderangebot; es
werden bewusst keine Daten von Kindern erhoben.

## 10. Deine Rechte

Auskunft, Berichtigung, Löschung, Einschränkung, Datenübertragbarkeit und Widerspruch stehen dir
zu (Art. 15–21 DSGVO). Ohne Konto liegen alle Daten ohnehin bei dir; mit Konto erreichst du die
Löschung direkt in der App. Beschwerden kannst du bei einer Datenschutz-Aufsichtsbehörde deines
Bundeslandes einlegen.

## 11. Änderungen

Wird die App um Funktionen erweitert, die Daten übertragen, wird dieser Text vorher angepasst und
mit der neuen Version veröffentlicht.
