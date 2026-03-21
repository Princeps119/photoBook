# photoBook

Ein Fullstack-Webprojekt zum Hochladen, Verwalten und Teilen von Bildern.

## Projektübersicht

Dieses Projekt besteht aus einem Java-Backend und einem modernen Frontend. Es ermöglicht Benutzern, sich zu registrieren, Bilder hochzuladen, diese als "Public" oder "Private" zu markieren und öffentliche Bilder anderer Benutzer anzusehen.

### Architektur & Technologien

*   **Backend:** Java 17+, Maven, `com.sun.net.httpserver` (leichtgewichtiger HTTP-Server), MongoDB.
*   **Frontend:** Dokumentation durch Maximilian Wagner (siehe `frontend` Verzeichnis).
*   **Datenbank:** MongoDB (NoSQL) zur Speicherung von Benutzerdaten und Bild-Metadaten inkl. Base64-kodierter Bilder.
*   **Infrastruktur:** Docker & Docker-Compose für Containerisierung.
*   **Sicherheit:** Passwort-Hashing (SHA-256), Token-basierte Authentifizierung (Bearer Token) mit verschlüsselten Inhalten (AES).

## Backend (Robin Mössinger)

Das Backend bietet eine REST-API für das Benutzer- und Bildmanagement.

### Verzeichnisstruktur (Backend)
*   `src/main/java/berufsschule/raach/controllers`: API-Endpunkt-Logik und Routing.
*   `src/main/java/berufsschule/raach/services`: Geschäftslogik (Bildverarbeitung, Authentifizierung, Hilfsfunktionen).
*   `src/main/java/berufsschule/raach/repo`: Datenbankanbindung an MongoDB (Singleton-Pattern).
*   `src/main/java/berufsschule/raach/data`: Datenmodelle (POJOs/Records).

### Authentifizierung
Die meisten Endpunkte erfordern einen `Authorization` Header:
`Authorization: Bearer <Login-Token>`

Das Login-Token ist ein JSON-Objekt, das verschlüsselte Informationen (E-Mail, Zeitstempel) enthält und vom `/api/login` Endpunkt zurückgegeben wird.
Die Email wird genutzt um den User zu identifizieren, währen Versionsnummer und Zeitstempel zur Validierung des Tokens selbst genutzt werden.

**Beispiel Login-Token (JSON):**
```json
{
  "username": "Max Mustermann",
  "encryptedMail": "a1b2c3d4...",
  "timestamp": "2026-02-12T14:03:00Z",
  "version": "550e8400-e29b-11d4-a716-446655440000"
}
```

### Bildspeicherung & GridFS
Bilder werden nicht direkt als Dokumente in einer Standard-MongoDB-Collection gespeichert, sondern über **GridFS**.

*   **GridFS:** Ein Spezifikationssystem von MongoDB zum Speichern und Abrufen von Dateien, die die BSON-Dokumentgrößenbeschränkung von 16 MB überschreiten.
*   **Speicherstruktur:** GridFS teilt die Bilder in zwei Collections auf:
    1.  `fs.files`: Speichert die Metadaten der Datei (Dateiname, Upload-Datum, Content-Type, Tags).
    2.  `fs.chunks`: Speichert die eigentlichen binären Datenpakete des Bildes.
*   **Ablauf:** Beim Hochladen (`/api/image/save`) wird der Base64-String aus dem Payload in ein Byte-Array dekodiert und via `GridFSBucket.uploadFromStream` gespeichert. Die generierte `ObjectId` wird zusätzlich im Benutzer-Dokument (`photobook_users.users`) im Feld `imageIds` hinterlegt, um die Verknüpfung zwischen User und Bild herzustellen.

### API Endpunkte

#### Usermanagement
Alle Endpunkte beginnen mit `/api/`.

*   **Login** (`POST /api/login`)
    *   Authentifiziert einen Benutzer und gibt ein Login-Token zurück.
*   **Logout** (`POST /api/logout`)
    *   Meldet den Benutzer ab und entwertet das aktuelle Token.
*   **Registrierung** (`POST /api/register`)
    *   Erstellt ein neues Benutzerkonto.
*   **Benutzer löschen** (`DELETE /api/delete`)
    *   Löscht das Konto des aktuell authentifizierten Benutzers inkl. aller Daten.
*   **Backend-Check** (`GET /api/checkBackend`)
    *   Prüft die Erreichbarkeit des Backends.

#### Imagemanagement
Alle bildbezogenen Endpunkte erfordern Authentifizierung (außer `allpublic`) und beginnen mit `/api/image/`.

*   **Bild speichern** (`POST /api/image/save`)
    *   Lädt ein neues Bild hoch.
    *   **Payload:** `ImageUploadData` (filename, metadata, base64 image). Der `tag` im `metadata`-Objekt bestimmt die Sichtbarkeit des Bildes (`Public` oder `Private`).
    *   **Beispiel Payload:**
        ```json
        {
          "filename": "meingesicht.png",
          "metadata": {
            "tag": "Public" // Sichtbarkeit: Public oder Private
          },
          "image": {
            "base64": "iVBORw0KGgoAAAANSUhEUgAAAAUA..."
          }
        }
        ```
*   **Bild mit ID finden** (`GET /api/image/findid?id={id}&tag={Public|Private}`)
    *   Ruft ein Bild anhand seiner ID und Sichtbarkeit ab.
*   **Eigene Bilder abrufen** (`GET /api/image/allforuser`)
    *   Gibt eine Liste aller Bilder des authentifizierten Benutzers zurück.
*   **Öffentliche Bilder abrufen** (`GET /api/image/allpublic`)
    *   Gibt alle als "Public" markierten Bilder zurück (keine Auth erforderlich).
*   **Bild löschen** (`DELETE /api/image/delete/{id}`)
    *   Löscht das Bild mit der angegebenen ID.

### Docker Setup
Das Projekt wird über Docker-Compose im Verzeichnis `docker` verwaltet.

1.  `photoBook_db`: MongoDB Container.
2.  `photoBook_backend`: Java Backend Container (Port 8080).

Zum Starten:
```bash
cd docker
docker-compose -p photobook -f docker/docker-compose.yml up --build -d
```

### Entwicklung & Build
Das Backend kann mit Maven gebaut werden:
```bash
cd backend
mvn clean install
```
Die MongoDB-Verbindungs-URI wird entweder über ein Docker-Secret (`/run/secrets/MONGO_URI_FILE`) oder eine lokale Datei `backend/src/main/resources/uri.txt` konfiguriert.

## Frontend (Maximilian Wagner)

### Techstack
Als Framework für das Frontend wurde Svelte und Sveltekit verwendet.

Dieses funktioniert mit HTML, CSS und Typescript.

### Start

Um das Frontend zu starten:

cd frontend

pnpm install oder npm install jenachdem welcher Package Manager installiert ist.

pnpm run dev oder npm run dev

Frontend läuft dann unter localhost:5173

### Svelte Backend

Svelte besitzt mit Sveltekit ein eigenes Backend und ist somit ein Fullstack Framework.

Hierbei wird Sveltekit verwendet, um die Logik für das verarbeiten des Auth Tokens, sowie um eigene API Routen zu definieren, welche mit dem Java Backend kommunizieren und für das Svelte Frontend zur Verfügung stehen.
Somit ist eine Verarbeitung der Daten möglich ohne dass vertrauliche Daten in das Frontend gelangen.

Als eigene API Routen wurden definiert:

- api/auth -> login, register und logout.
- api/photos -> delete, load.
- api/upload.



