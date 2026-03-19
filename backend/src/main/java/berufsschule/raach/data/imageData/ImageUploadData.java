package berufsschule.raach.data.imageData;

import com.mongodb.lang.NonNull;
import org.bson.Document;

/**
 *
 * @param filename name of the image
 * @param metadata must contain a tag key with either Private or Public value
 * @param image the image itself
 *<p>
 * Mock JSON
 *<p>
        {<p>
                "filename": "profile_picture.png",<p>
                "metadata": {<p>
                        "userMail": "alice@example.com", -optional-
                        "contentType": "image/png", -optional-
                        "tag": "Public", -required-
                        "uploadDate": "2026-02-05T17:40:00Z" -optional-
                        },<p>
                "image": {
                              "base64":"iVBORw0KGgoAAAANSUhEUgAAAAUA"
                }
        }
 </p>

 */
public record ImageUploadData(String filename, @NonNull Document metadata, ImageArrayData image) {}

