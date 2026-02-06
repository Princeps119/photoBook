package berufsschule.raach.data;

import com.mongodb.lang.NonNull;
import org.bson.Document;

/**
 *
 * @param hexStringId the MongoDB/GridFSFile id of the image, from the backend
 * @param filename name of the image
 * @param metadata must contain a tag key with either Private or Public value
 * @param byteArray the image itself
 *<p>
 * Mock JSON
 *<p>
        {<p>
                "hexString": "507f1f77bcf86cd799439011",<p>
                "filename": "profile_picture.png",<p>
                "metadata": {<p>
                        "userMail": "alice@example.com", -optional-
                        "contentType": "image/png", -optional-
                        "tag": "Public",
                        "uploadDate": "2026-02-05T17:40:00Z" -optional-
                        },<p>
                "byteArray": "iVBORw0KGgoAAAANSUhEUgAAAAUA"
        }
 </p>

 */
public record ImageWithMetaData(String hexStringId, String filename, Document metadata, @NonNull byte[] byteArray) {}

