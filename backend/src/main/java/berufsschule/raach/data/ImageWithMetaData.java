package berufsschule.raach.data;

import org.bson.Document;

/**
 *
 * @param hexStringId the MongoDB/GridFSFile id of the image
 * @param filename name of the image
 * @param metadata must contain a tag key with either Private or Public value
 * @param byteArray the image itself
 *
 * Mock JSON
 * <p>
        {
        "hexString": "507f1f77bcf86cd799439011",
        "filename": "profile_picture.png",
        "metadata": {
        "userMail": "alice@example.com",
        "contentType": "image/png",
        "tag": "Public",
        "uploadDate": "2026-02-05T17:40:00Z"
        },
        "byteArray": "iVBORw0KGgoAAAANSUhEUgAAAAUA"
        }
 </p>

 */
public record ImageWithMetaData(String hexStringId, String filename, Document metadata, byte[] byteArray) {}

