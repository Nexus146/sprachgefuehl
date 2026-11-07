# Konten und Cloud-Sicherung (optional)

Sprachgefühl bleibt ohne Konto vollständig nutzbar. Wer ein Konto verbindet, bekommt zusätzlich
eine Cloud-Sicherung für Fortschritt, Profil, Einstellungen und – wenn gewünscht – die eigenen
Aufnahmen. Lokal bleibt die primäre Kopie; die Cloud ist ein zusätzlicher Stand.

## Aktueller Stand

Das Projekt **Sprachgefühl Projekts** (`uakioxhypckbhnqciury`, Region `eu-central-1`) ist
angelegt. Bereits erledigt:

- `learning_states` mit RLS, `is_verified_user()` und `push_learning_state`,
- privater Bucket `recordings` mit 12-MB-Limit und den vier Policies,
- Redirect-URLs für `sprachgefuehl://auth/confirm`,
- Edge Function `delete-account` (Version 1, aktiv),
- `SUPABASE_URL` und `SUPABASE_ANON_KEY` in der lokalen `local.properties`.

Offen ist nur noch der eigene SMTP-Versand (Abschnitt 4). Ohne ihn verschickt Supabase
Bestätigungsmails ausschließlich an Adressen des eigenen Teams.

Die Anmeldung ist auch im Erststart erreichbar: Auf der Begrüßungsseite steht „Ich habe schon
ein Konto“, darunter bleibt der Weg über eine Sicherungsdatei. Wer sich dort anmeldet, bekommt
den Cloud-Stand geladen, ohne erst die Standortbestimmung zu durchlaufen.

## Was übertragen wird

| Daten | Ohne Konto | Mit Konto |
|---|---|---|
| Fortschritt, Profil, Einstellungen | nur lokal | lokal und als ein JSON-Dokument in `learning_states.progress` |
| Eigene Aufnahmen | nur lokal | nur bei aktivem Sync im privaten Bucket `recordings` |
| E-Mail | – | Supabase Auth, für Anmeldung und Bestätigung |
| Werbung/Analytics | keine | keine |

Das Datenmodell ist absichtlich flexibel: eine Zeile pro Nutzer, der gesamte Lernstand als
`jsonb`. Neue App-Versionen können das Dokument erweitern, ohne für jedes Feld eine neue Spalte
zu brauchen. Die Revision schützt vor stillem Überschreiben.

## 1. Supabase-Projekt

1. Kostenloses Projekt auf [supabase.com](https://supabase.com) anlegen (keine Kreditkarte).
2. Unter **Project Settings → API** die Projekt-URL und den **Anon Key** kopieren.
3. In `local.properties` eintragen – diese Datei ist nicht im Repository:

```properties
SUPABASE_URL=https://DEIN-PROJEKT.supabase.co
SUPABASE_ANON_KEY=DEIN-ANON-KEY
```

Der Anon Key ist öffentlich; der Schutz kommt aus RLS und den Datenbankfunktionen. Der
**Service-Role-Key** gehört niemals in die App, nicht in `local.properties` und nicht ins Repo.

Ohne diese zwei Werte baut die App weiter und zeigt unter **Einstellungen → Konto und Cloud**
einen Einrichtungshinweis.

## 2. Datenbank, RLS und Storage

Die Datei [supabase/migrations/0001_accounts.sql](../supabase/migrations/0001_accounts.sql) im
Supabase-SQL-Editor ausführen. Sie legt an:

- `learning_states` mit `progress jsonb`, `revision`, Zeitstempeln und Löschkaskade auf
  `auth.users`,
- die Funktion `is_verified_user()`, die `email_confirmed_at` prüft,
- RLS: Lesen nur die eigene Zeile; Schreiben nur über `push_learning_state`,
- den privaten Bucket `recordings` mit Pfad `{user_id}/{recording_id}.m4a`,
- Storage-Policies, die den Ordnernamen gegen `auth.uid()` prüfen.

`push_learning_state` ist absichtlich `security definer`: Der Client darf die Revision nicht
fälschen oder an der RLS vorbei schreiben. Bei einer veralteten Revision antwortet die Funktion
mit `conflict = true` und dem aktuellen Cloud-Stand. `p_force = true` wird nur verwendet, wenn
der Lernende im Konflikt-Dialog bewusst „Dieses Gerät hochladen“ wählt.

## 3. Auth und Deeplink

Unter **Authentication → URL Configuration**:

- **Site URL:** `sprachgefuehl://auth/confirm` – es gibt keine Web-App, der Deeplink ist das
  Ziel.
- **Redirect URLs:** `sprachgefuehl://auth/confirm`, `sprachgefuehl://auth` und
  `sprachgefuehl://*`
  (die Bestätigungs- und Reset-Links nutzen `/confirm`; das erneute Senden verwendet den
  Basis-Deeplink).

Beides ist für dieses Projekt schon gesetzt; die Passwort-Mindestlänge liegt bei 8 Zeichen.

Unter **Authentication → Providers → Email**:

- **Confirm email** einschalten.
- Die Standardvorlagen für Bestätigung und Passwort-Reset verwenden `{{ .ConfirmationURL }}`.
  Der Deeplink wird von Supabase an die erlaubte Redirect-URL angehängt.

Die Android-App verarbeitet den Rücksprung in `MainActivity` über
`supabase.handleDeeplinks(intent)`. Das Schema ist `sprachgefuehl`, der Host `auth`. Der
Passwort-Reset erkennt `type=recovery` und zeigt danach die Passwortänderung an.

## 4. Echte E-Mails: Mailjet Free

Der eingebaute Supabase-Mailer ist **kein Produktionsversand**: nur Team-Adressen und wenige
Mails pro Stunde. Für beliebige Nutzer muss ein eigener SMTP-Dienst konfiguriert werden.

Mailjet Free reicht für ein kleines Projekt:

- 6.000 Mails/Monat, 200/Tag, keine Kreditkarte,
- SMTP-Relay inklusive,
- eine verifizierte Absenderadresse; eine eigene Domain ist optional, verbessert aber die
  Zustellbarkeit.

Unter **Authentication → SMTP Settings** eintragen:

| Feld | Wert |
|---|---|
| Host | `in-v3.mailjet.com` |
| Port | `587` (STARTTLS) |
| Username | Mailjet API Key |
| Password | Mailjet Secret Key |
| Sender email | die in Mailjet verifizierte Adresse |
| Sender name | `Sprachgefühl` |

Danach unter **Authentication → Rate Limits** prüfen, dass der Mailversand nicht unnötig niedrig
gedrosselt ist. Supabase setzt nach dem Aktivieren von Custom SMTP eigene Limits; die
Free-Grenzen von Mailjet gelten zusätzlich.

Ohne diesen Schritt kann die App technisch fertig sein, aber Bestätigungsmails an fremde
Adressen kommen nicht zuverlässig an. Das ist die einzige Stelle, an der „komplett kostenlos“
eine reale Grenze hat.

## 5. Konto löschen

Die Edge Function [supabase/functions/delete-account/index.ts](../supabase/functions/delete-account/index.ts)
löscht die Aufnahmen, die `learning_states`-Zeile und den Auth-Nutzer. Sie prüft die Sitzung des
Aufrufers und verwendet den Service-Role-Key nur serverseitig.

```bash
supabase functions deploy delete-account
```

Die Funktion ist für dieses Projekt bereits deployt (`verify_jwt` aktiv, Version 1). Die
Variablen `SUPABASE_URL`, `SUPABASE_ANON_KEY` und `SUPABASE_SERVICE_ROLE_KEY` stellt die
Laufzeit selbst bereit; keine davon wird in die App geschrieben.

## 6. Sync-Verhalten

- Lokal wird bei jeder Änderung gespeichert. Der Sync vergleicht einen SHA-256-Hash des lokalen
  Dokuments mit dem zuletzt bestätigten Cloud-Hash.
- Nach dem Login wird der Cloud-Stand geladen, wenn lokal noch kein onboardingfähiger Stand
  existiert.
- Ohne Cloud-Stand wird der lokale Stand hochgeladen.
- Haben beide Seiten seit dem letzten Sync geändert, erscheint ein Konflikt-Dialog. Nichts wird
  automatisch überschrieben.
- Netzwerkfehler bleiben lokal und werden über WorkManager mit Netzwerk-Constraint erneut
  versucht.
- Aufnahmen werden nach dem Fortschritt übertragen. Fehlt eine Datei auf einem neuen Gerät, wird
  sie heruntergeladen und der lokale Pfad neu gesetzt. Schlägt eine Aufnahme fehl, bleibt der
  Fortschritt trotzdem erhalten.

### Abmelden

Abmelden ist ein echter Kontowechsel: Die Sitzung wird beendet **und der lokale Lernstand
entfernt**. Vorher versucht die App einmal zu sichern, was lokal noch offen ist; ohne Verbindung
weist der Dialog darauf hin, dass nicht Gesichertes verloren geht. Die Sitzung wird auch dann
beendet, wenn der Server nicht antwortet (`clearSession()` als Rückfall).

Mit dem Fortschritt gehen Revision, Hash und Herkunft (`owner_uid`/`owner_email`) weg. Danach
steht der Erststart wieder am Anfang; wer weiterlernen will, meldet sich an. Beim nächsten Login
ist lokal nichts onboarded, deshalb wird der Cloud-Stand geladen – ein Download, keine
Konfliktfrage. Der Dialog vor dem Abmelden sagt genau das an: lokal wird entfernt, in der Cloud
bleibt es.

Ein Fortschritt, der zu einem anderen Konto gehört, wird nie stillschweigend in das angemeldete
Konto geschoben – etwa wenn jemand die App-Daten eines Kontos mit einem anderen Konto verbindet,
ohne sich vorher abzumelden. Die Konto-Seite sagt das dann.

## 6a. Admin-Konsole

Ein Admin-Konto meldet sich über dasselbe Fenster an wie alle anderen („Ich habe schon ein
Konto“), landet aber nicht in der Lern-App, sondern in einer Konsole.

| | |
|---|---|
| Benutzername | `admin-11` (ohne `@`) |
| Passwort | beim Anlegen gesetzt, siehe unten |
| Auth-Adresse | `admin-11@sprachgefuehl.local` – die App ergänzt die Domain automatisch |

Der Eintrag in `public.admins` entscheidet über den Zugang. Er lässt sich nur in der Datenbank
ändern, nicht in der App; die App liest ihn über die eigene RLS-Policy (nur die eigene Zeile).
Ein Konto ohne diesen Eintrag ist ein normales Lernkonto.

Was die Konsole kann:

- **Überblick** über alle Konten: wie viele es gibt, wie viele einen Cloud-Stand haben, wie viele
  gesperrt sind, Lernminuten gesamt und wie viele sich in den letzten sieben Tagen angemeldet haben,
- alle Konten auflisten: Adresse, bestätigt oder nicht, Lektionen, Lernminuten, Revision, Zeitpunkt
  der letzten Sicherung und der letzten Anmeldung,
- **suchen und filtern** nach Adresse oder Name, „Mit Cloud“ und „Gesperrt“,
- **in ein Konto hineinsehen**: Name aus dem Profil, Tagesziel, Interessen, Lektionen, Minuten,
  Anzahl und Größe der Aufnahmen, Revision – aus demselben Dokument, das auch das Gerät lädt,
- ein Konto **sperren**, sodass keine Anmeldung mehr möglich ist, und wieder entsperren; der
  Lernstand bleibt dabei erhalten,
- den **Fortschritt zurücksetzen**: Lernstand und Aufnahmen weg, Konto bleibt (für „nochmal von
  vorn“),
- neue Konten anlegen (Adresse + Startpasswort) – sie gelten sofort als bestätigt und können sich
  ohne Mail anmelden, was ohne eigenen SMTP der einzige Weg ist,
- die **E-Mail-Adresse ändern** (gilt sofort als bestätigt),
- einem Konto ein neues Passwort setzen (gilt sofort, das alte nicht mehr),
- die **Zugangsdaten teilen** (Adresse und Startpasswort über das Teilen-Fenster von Android) –
  solange kein SMTP eingerichtet ist, der einzige Weg, jemandem sein Konto zu geben,
- Konten löschen, samt Cloud-Dokument und Aufnahmen,
- das eigene Admin-Konto ist gegen Löschen in der Konsole geschützt.

Alles läuft über die Edge Function `admin-users`: Sie prüft den Aufrufer gegen `public.admins` und
nutzt erst dann den Service-Role-Key, der nur in der Function-Laufzeit existiert. Die App spricht
nur mit der Function und bekommt nie einen Service-Key.

### Admin anlegen

```sql
-- 1. Auth-Konto (bestätigt) mit Passwort anlegen, 2. in public.admins eintragen.
insert into auth.users (instance_id, id, aud, role, email, encrypted_password, email_confirmed_at,
                        created_at, updated_at, raw_app_meta_data, raw_user_meta_data, is_super_admin,
                        confirmation_token, recovery_token, email_change_token_new, email_change)
values ('00000000-0000-0000-0000-000000000000', gen_random_uuid(), 'authenticated', 'authenticated',
        'admin-11@sprachgefuehl.local', extensions.crypt('DEIN-PASSWORT', extensions.gen_salt('bf')),
        now(), now(), now(), '{"provider":"email","providers":["email"]}'::jsonb,
        '{"email_verified":true}'::jsonb, false, '', '', '', '')
returning id;

insert into auth.identities (id, provider_id, user_id, identity_data, provider, last_sign_in_at, created_at, updated_at)
select gen_random_uuid(), u.id::text, u.id,
       jsonb_build_object('sub', u.id::text, 'email', u.email, 'email_verified', true),
       'email', now(), now(), now()
from auth.users u where u.email = 'admin-11@sprachgefuehl.local';

insert into public.admins (user_id, note)
select id, 'Admin-Konsole' from auth.users where email = 'admin-11@sprachgefuehl.local';
```

## 7. Grenzen des Free-Tarifs

- 500 MB PostgreSQL, 1 GB Storage, 5 GB Egress pro Monat.
- 50.000 monatlich aktive Auth-Nutzer, 2 aktive Projekte.
- Kein automatisches Backup. Für ein echtes Projekt regelmäßig exportieren.
- Ein Free-Projekt pausiert nach etwa 7 Tagen ohne Datenbankaktivität. Ein Klick im Dashboard
  reaktiviert es; die App synchronisiert danach weiter.
- Aufnahmen sind auf 12 MB pro Datei begrenzt (Bucket-Limit). Für Sprachnotizen ist das großzügig.

## 8. Prüfen

1. Ohne `SUPABASE_*`-Werte starten: Konto-Seite zeigt „Noch nicht eingerichtet“, Lernen geht.
2. Mit Werten registrieren: Bestätigungsmail kommt an, Login ist vor der Bestätigung gesperrt.
3. Nach Bestätigung anmelden: lokaler Stand wird hochgeladen, Revision erscheint.
4. Auf einem zweiten Gerät anmelden: Cloud-Stand wird geladen, Aufnahmen werden gefunden.
5. Zwei Geräte ändern: Konflikt-Dialog erscheint; beide Wege einmal ausprobieren.
6. Flugmodus: lokale Änderung bleibt, Sync zeigt offline; nach Netz kommt der Retry.
7. Konto löschen: Cloud-Zeile und Aufnahmen sind weg, lokaler Fortschritt bleibt.
8. Fremde Storage-Pfade und fremde `user_id` müssen mit 403/leerer Liste scheitern.

Die App enthält keine Supabase-Secrets und keine Service-Role-Logik. Alle Prüfungen der
Berechtigung liegen in Postgres/Storage.

## 9. Automatischer Test gegen das echte Projekt

`app/src/androidTest/java/de/sprachgefuehl/app/account/CloudAccountLiveTest.kt` prüft die ganze
Kette auf einem Emulator gegen das echte Projekt:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Abgedeckt sind Registrierung, erste Sicherung (Revision 1 samt Aufnahme), ein zweites Gerät, das
den Cloud-Stand lädt, Konflikterkennung, Konfliktlösung und die Löschung über die Edge Function
(bis hin zu `auth.users` = 0). Der Test legt ein Wegwerf-Konto mit eindeutiger Adresse an und
räumt es am Ende selbst weg.

Weil es auf dem Emulator kein Postfach gibt, muss „Confirm email" für die Dauer des Laufs aus
sein (Autoconfirm). Danach wieder einschalten. Ohne diesen Test bleibt die Registrierung per Mail
nur von Hand prüfbar – siehe `docs/test-run.md`, Abschnitt „Konto und Cloud".

## 10. Testdaten in ein vorhandenes Konto legen

`SeedCloudAccountTest.kt` schreibt einen realistischen Lernstand in ein **bestehendes** Konto:
Name, Gründe und Interessen, sieben Lektionen, 28 Sätze mit Wiederholungsdaten, sechs Lernage,
Lernzeit und ein gemessenes Sprachprofil. Der Stand entsteht aus dem echten Inhaltskatalog und
geht über `push_learning_state` hoch – also genau der Weg, den die App nimmt. Damit lässt sich
prüfen, was ein frisches Gerät nach dem Login sieht, ohne den Lernstand von Hand nachzubauen.

Die Zugangsdaten stehen nicht im Code, sondern kommen als Instrumentation-Argument:

```bash
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=de.sprachgefuehl.app.account.SeedCloudAccountTest \
  -Pandroid.testInstrumentationRunnerArguments.email=… \
  -Pandroid.testInstrumentationRunnerArguments.password=…
```

Ohne Argumente tut der Test nichts (`assumeTrue`). Er meldet sich an, lädt hoch, prüft `SYNCED`
und meldet sich am Ende wieder ab; der Sync-Zustand in `SharedPreferences` wird geleert, damit auf
dem Gerät keine fremde Sitzung zurückbleibt. Ein vorhandenes Cloud-Dokument wird dabei durch den
Teststand ersetzt – nur auf Konten anwenden, deren Inhalt entbehrlich ist.
