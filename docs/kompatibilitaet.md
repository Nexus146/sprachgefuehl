# Updates und alter Speicherstand

Der Fortschritt liegt als eine JSON-Datei auf dem Gerät und kann Jahre älter sein als die App.
Jedes Update – neue Lektionen, ein größerer Aufgabenpool, eine neue Einstellung, ein anderer
Lektionsbaum – muss mit dieser Datei zusammenarbeiten. Diese Seite sagt, welche Regeln gelten und
welcher Test sie durchsetzt.

## Die fünf Regeln

1. **Jedes gespeicherte Feld hat einen Default.** Eine Datei aus einer älteren Fassung enthält das
   Feld nicht; der Default füllt es. Wer ein Feld ohne Default ergänzt, macht jede alte Datei
   unlesbar – die App fiele auf die Sicherung zurück und danach auf einen leeren Stand.
2. **Was verschwindet, wird beim Laden aussortiert.** Entfernte Lektionen, Sätze, Fehlerkarten,
   Situationen und Muster werden aus dem Stand gestrichen. Zähler behaupten danach nichts, was es
   nicht mehr gibt (`ContentIndex.migrate`, aufgerufen beim Laden und beim Import).
3. **Eine angefangene Lektion bleibt.** Ein Update verwirft die laufende Runde nicht mehr, solange
   ihre Lektion existiert und alle Sätze darin noch da sind. Die Schritte tragen ihren Text selbst;
   eine neue Inhaltsversion stört sie nicht. Fehlt ein Satz, fällt nur die Runde weg – der
   Lektionsfortschritt und alles andere bleiben.
4. **Neuer Inhalt ist neu, nicht geschafft.** Eine Lektion, die der Stand nicht kennt, gilt als
   offen. Sie taucht als „Neu" auf und wird nicht rückwirkend als erledigt gezählt.
5. **Ein alter Stand bleibt lesbar, auch wenn er kaputt ist.** Eine unlesbare Datei fällt auf die
   letzte gute Fassung (`.bak`) zurück; eine Sicherungsdatei wird beim Laden durch dieselbe
   Migration geschickt wie der normale Start.

## Was ein Update nicht tun darf

- **Ein Feld umbenennen oder löschen.** Stattdessen: neues Feld mit Default ergänzen, das alte
  weiter lesen und beim Lesen zusammenführen (siehe `Profile.reasons()`: `reasonId` und
  `reasonIds` bestehen nebeneinander).
- **Die Bedeutung eines Feldes ändern**, ohne den alten Wert zu erkennen. Beispiel: `fontScale`
  ist ein Faktor, keine Stufe – 1.3 bleibt 1.3.
- **Einen bestehenden Aufgabentyp stillschweigend anders zählen.** `taskStats` hängt an der
  Typ-ID; eine neue ID startet bei null, eine umbenannte verliert ihre Historie.

## Welcher Test das erzwingt

`ProgressUpgradeTest` prüft bei jedem Lauf:

- **Der Wächter:** alle Felder der gespeicherten Klassen (`Progress`, `Profile`, `Settings`,
  `SkillProfile`, `CheckupState`, `LessonSession`, `Mistake`, `Memory`, `TaskStat`,
  `SkillSnapshot`, `SkillState`, `SessionLog`, `RecordingRef`) haben einen Default. Ein neues
  Pflichtfeld bricht den Test mit Klassen- und Feldnamen.
- Die älteste mögliche Datei (`{"onboarded":true}`) lädt und ergibt exakt die Defaults.
- Eine Datei ohne neue Einstellungen lädt mit deren Defaults.
- Unbekannte Felder einer neueren Fassung werden ignoriert.
- Ein voller Stand übersteht Schreiben und Lesen unverändert (Round-Trip).
- Ein Update mit neuer Inhaltsversion **behält die laufende Lektion**, wenn ihre Sätze existieren.
- **Adaptive Teile rechnen weiter:** der Mixer baut aus einem Stand ohne Sprachprofil-Historie
  eine volle Runde, Tagesziel und Garten funktionieren.
- Eine verschwundene Lektion wird verworfen, der Rest bleibt.
- Neuer Inhalt erscheint nicht als „geschafft".

Prüfen mit:

```bash
./gradlew :app:testDebugUnitTest --tests "*ProgressUpgradeTest*"
```

## Wenn ein Test rot wird

1. Fehlt der Default: `= <Wert>` an das Feld schreiben. Das ist fast immer die richtige Antwort.
2. Ist das Feld wirklich Pflicht (etwa eine ID): in `legacyRequired` eintragen – aber nur, wenn
   jede existierende Datei es bereits enthält.
3. Bricht der Round-Trip: prüfen, ob ein Typ serialisierbar bleibt (`@Serializable`, keine
   Interfaces in Feldern).
4. Bricht die laufende Lektion: prüfen, ob ein Schritt auf einen Satz zeigt, den es nicht mehr
   gibt – dann ist der Inhalt gemeint, nicht die Migration.
