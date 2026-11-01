# Sicherung und Gerätewechsel

Die App hat kein Konto und keinen Server. Wer das Handy wechselt, nimmt den Fortschritt als
Datei mit: **Einstellungen → Deine Daten → Sicherung erstellen**.

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
- Zwei Geräte gleichzeitig aktuell halten geht nicht: Es gibt bewusst keinen Abgleich. Wer auf
  beiden lernt, sichert vor dem Wechsel und lädt die neuere Datei.

## Warum kein Konto

Ein Konto hätte eine E-Mail-Adresse, ein Passwort, eine Bestätigungsmail und einen fremden
Server gebraucht – für eine App, die auf einem einzigen Gerät läuft, viel Aufwand und die erste
Stelle, an der Daten das Haus verlassen. Die Sicherungsdatei kann der Lernende selbst sehen,
kopieren und löschen. Die App hat deshalb auch keine Internet-Berechtigung.

## Neue Felder in `fortschritt.json`

Seit dem Sprachprofil enthält die Datei zusätzlich `skills` (fünf Bereiche mit Wert und Anzahl Belege), `checkup` (Stand und Ergebnis der Standortbestimmung), `seed` und `variantIds` (welche
Geschichte gewählt wurde). Ältere Sicherungen ohne diese Felder laden weiter: Die App ergänzt
die Standardwerte und startet mit einem ungemessenen Profil.

Seit dem Wochenrückblick kommt `skillHistory` dazu: ein Tagesstand der fünf Bereiche, höchstens
60 Einträge. Fehlt das Feld, zeigt die Woche einfach noch keine Bewegung – alles andere bleibt
gültig.
