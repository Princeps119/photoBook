# photoBook

## Image Controller API Endpoints

All image-related endpoints require a valid authentication token.

### Save Image
*   **URL:** `/api/image/save`
*   **Method:** `POST`
*   **Content-Type:** `application/json`
*   **Payload:** See `ImageWithMetaData` record documentation for the JSON structure.
*   **Description:** Uploads and saves a new image for the authenticated user.

### Find Image by ID
*   **URL:** `/api/image/findid`
*   **Method:** `GET`
*   **URL Parameters:**
    *   `id=[string]`: The hexadecimal ID of the image.
    *   `tag=[string]`: The visibility tag (`Public` or `Private`).
*   **Description:** Retrieves a specific image by its ID and tag.

### Get All Images for User
*   **URL:** `/api/image/allforuser`
*   **Method:** `GET`
*   **Description:** Returns a list of all images associated with the currently authenticated user.

### Delete Image
*   **URL:** `/api/image/delete/{id}`
*   **Method:** `DELETE`
*   **Description:** Deletes the image with the specified ID. The user must be authorized to delete the image.
