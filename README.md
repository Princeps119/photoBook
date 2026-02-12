# photoBook

## Frontend (Maximilian Wagner)
Das Frontend dieser Anwendung ermöglicht es Benutzern, Bilder hochzuladen, zu betrachten und zu verwalten. Eine detaillierte Dokumentation der Frontend-Komponenten folgt durch Maximilian Wagner.

## Backend (Robin Mössinger)
Das Backend ist in Java implementiert, wird mit Maven gebaut und bietet REST-Endpunkte für das Usermanagement sowie das Imagemanagement.

### Docker 
Das Backend läuft als zwei Dockercontainer (siehe Directory `docker`). Über Docker-Compose werden die beiden Container gestartet und gestoppt.
Der Container für die Datenbank wird mit dem Namen `photoBook_db` gestartet und gestoppt und basiert auf einem MongoDB Image.
Der Container für die Backend-Logik wird mit dem Namen `photoBook_backend` gestartet und gestoppt und basiert auf dem Javacode im Directory `backend`.

### Usermanagement (Hierfür wurde Logik, welche ich das Projekt bei Herrn Mezger geschrieben hatte, ergänzt/angepasst)
Alle Endpunkte für das Usermanagement beginnen mit `/api/`.

*   **Login**
    *   **URL:** `/api/login`
    *   **Methode:** `POST`
    *   **Beschreibung:** Authentifiziert einen Benutzer und gibt ein Login-Token zurück, was später der Authentifizierung des Users bei anderen Requests dient.
    *   **Beispiel Login-Token (JSON):**
        ```json
        {
          "username": "Max Mustermann",
          "encryptedMail": "a1b2c3d4...",
          "timestamp": "2026-02-12T14:03:00Z",
          "version": "550e8400-e29b-11d4-a716-446655440000"
        }
        ```
*   **Logout**
    *   **URL:** `/api/logout`
    *   **Methode:** `POST`
    *   **Beschreibung:** Meldet den Benutzer ab und entwertet das aktuelle Token.
*   **Registrierung**
    *   **URL:** `/api/register`
    *   **Methode:** `POST`
    *   **Beschreibung:** Erstellt ein neues Benutzerkonto.
*   **Benutzer löschen**
    *   **URL:** `/api/delete`
    *   **Methode:** `DELETE`
    *   **Beschreibung:** Löscht das Konto des aktuell authentifizierten Benutzers, damit werden auch alle Token des Users entwertet.
*   **Backend-Check**
    *   **URL:** `/api/checkBackend`
    *   **Methode:** `GET`
    *   **Beschreibung:** Einfacher Integritätstest, um sicherzustellen, dass das Backend erreichbar ist.

### Imagemanagement (Im Zuge dieses Projektes komplett neu geschrieben)
Alle bildbezogenen Endpunkte erfordern ein gültiges Authentifizierungs-Token und beginnen mit `/api/image/`.

*   **Bild speichern**
    *   **URL:** `/api/image/save`
    *   **Methode:** `POST`
    *   **Beschreibung:** Lädt ein neues Bild für den authentifizierten Benutzer hoch.
    *   **Datenstruktur (ImageUploadData):**
        ```json
        {
          "filename": "beispiel.png",
          "metadata": {
            "tag": "Public",
            "contentType": "image/png"
          },
          "image": {
            "base64": "iVBORw0KGgoAAAANSUhEUgAAAAUA..."
          }
        }
        ```
*   **Bild nach ID finden**
    *   **URL:** `/api/image/findid`
    *   **Methode:** `GET`
    *   **Parameter:** `id=[string]`, `tag=[string]` (z.B. `Public` oder `Private`).
    *   **Beschreibung:** Ruft ein bestimmtes Bild anhand seiner ID und seines Sichtbarkeits-Tags ab.
    *   **Rückgabestruktur (ImageWithIDData):**
        ```json
        {
          "hexStringId": "507f1f77bcf86cd799439011",
          "filename": "beispiel.png",
          "metadata": {
            "tag": "Public",
            "contentType": "image/png"
          },
          "base64": "iVBORw0KGgoAAAANSUhEUgAAAAUA..."
        }
        ```
*   **Alle Bilder des Benutzers abrufen**
    *   **URL:** `/api/image/allforuser`
    *   **Methode:** `GET`
    *   **Beschreibung:** Gibt eine Liste aller Bilder zurück, die dem aktuell authentifizierten Benutzer gehören.
    *   **Rückgabestruktur (Liste von ImageSummaryData):**
        ```json
        [
          {
            "hexStringId": "507f1f77bcf86cd799439011",
            "filename": "beispiel.png",
            "metadata": {
              "tag": "Private"
            }
          }
        ]
        ```
*   **Alle öffentlichen Bilder abrufen**
    *   **URL:** `/api/image/allpublic`
    *   **Methode:** `GET`
    *   **Beschreibung:** Gibt eine Liste aller als "Public" markierten Bilder zurück. Ist der einzige Imageendpunkt, der ohne Authentifizierung aufgerufen werden kann.
*   **Bild löschen**
    *   **URL:** `/api/image/delete/{id}`
    *   **Methode:** `DELETE`
    *   **Beschreibung:** Löscht das Bild mit der angegebenen ID. Der Benutzer muss berechtigt sein, dieses Bild zu löschen.




