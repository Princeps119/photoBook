package berufsschule.raach.data.imageData;

import com.mongodb.lang.NonNull;
import org.bson.Document;

/**
 *
 * @param hexStringId the MongoDB/GridFSFile id of the image, from the backend
 * @param filename name of the image
 * @param metadata must contain a tag key with either Private or Public value
 * @param base64 the image itself in a base64 string format
 *<p>
 * Mock JSON
 * <p>
 * <pre>{@code
{
"hexString": "507f1f77bcf86cd799439011",
"filename": "profile_picture.png",
"metadata": {
"userMail": "alice@example.com", // optional
"contentType": "image/png",      // optional
"tag": "Public",
"uploadDate": "2026-02-05T17:40:00Z" // optional
},
"image": {
"base64": "iVBORw0KGgoAAAANSUhEUgAAAAUA"
}
}
 * }</pre>
 */

public record ImageWithIDData(String hexStringId, String filename, @NonNull Document metadata, @NonNull ImageArrayData base64) {}

