# Sicherung und Gerätewechsel

Die App funktioniert ohne Konto und ohne Server. Wer das Handy wechselt, nimmt den Fortschritt
als Datei mit: **Einstellungen → Deine Daten → Sicherung erstellen**. Optional kann zusätzlich
ein Konto für die Cloud-Sicherung verbunden werden ([accounts.md](accounts.md)).

## Was in der Datei liegt

`sprachgefuehl-sicherung.zip` ist ein gewöhnliches ZIP-Archiv:

| Eintrag | Inhalt |
|---|---|
| `fortschritt.json` | Profil, Einstellungen, Lektionen, Sätze, Fehlerzähler, Garten, Meilensteine, Sitzungen |
| `aufnahmen/*.m4a` | die eigenen Aufnahmen aus „Meine Sätze“, falls vorhanden |

Die Aufnahmen behalten ihren Dateinamen, deshalb findet die App sie nach dem Laden ohne Umweg
wieder. Eine Datei mit nur `fortschritt.json` – also eine Sicherung aus einer früheren Version –
wird ebenfalls geladen.

## Umzug in fünf Schritten

1. Auf dem alten Handy: Einstellungen → Deine Daten → **Sicherung erstellen**, Speicherort wählen
   (zum Beispiel Downloads).
2. Die Datei auf das neue Handy bringen: Kabel, USB-Stick oder ein Cloud-Ordner.
3. Die App auf dem neuen Handy installieren und einmal starten (das Onboarding lässt sich
   überspringen).
4. Einstellungen → Deine Daten → **Sicherung laden**, die Datei auswählen und „Laden“ bestätigen.
5. Prüfen: Die Startseite zeigt den Lernstand, das Profil den Namen, „Meine Sätze“ die Aufnahmen.

## Regeln

- **Laden ersetzt** den Stand auf dem Gerät. Die App fragt vorher; ohne Bestätigung passiert
  nichts.
- Eine fremde oder beschädigte Datei wird abgelehnt; der bisherige Fortschritt bleibt unverändert.
- Die Datei ist **nicht verschlüsselt**. Sie gehört an einen Ort, den nur der Lernende kennt.
- Ohne Konto gibt es keinen automatischen Abgleich zwischen zwei Geräten. Wer auf beiden
  lernt, sichert vor dem Wechsel und lädt die neuere Datei. Mit Konto übernimmt der optionale
  Cloud-Sync diese Aufgabe (siehe [accounts.md](accounts.md)).

## Warum die Datei bleibt

Die Sicherungsdatei ist der Weg, der ohne E-Mail, Passwort und fremden Server funktioniert. Sie
kann der Lernende selbst sehen, kopieren und löschen. Ein Konto ist eine zusätzliche, freiwillige
Bequemlichkeit – keine Voraussetzung. Ohne Konto wird die Internet-Berechtigung nicht genutzt.

## Neue Felder in `fortschritt.json`

Seit dem Sprachprofil enthält die Datei zusätzlich `skills` (fünf Bereiche mit Wert und Anzahl Belege), `checkup` (Stand und Ergebnis der Standortbestimmung), `seed` und `variantIds` (welche
Geschichte gewählt wurde). Ältere Sicherungen ohne diese Felder laden weiter: Die App ergänzt
die Standardwerte und startet mit einem ungemessenen Profil.

Seit dem Wochenrückblick kommt `skillHistory` dazu: ein Tagesstand der fünf Bereiche, höchstens
60 Einträge. Fehlt das Feld, zeigt die Woche einfach noch keine Bewegung – alles andere bleibt
gültig.
