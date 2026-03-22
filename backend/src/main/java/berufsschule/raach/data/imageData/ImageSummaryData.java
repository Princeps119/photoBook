package berufsschule.raach.data.imageData;

import org.bson.Document;

/**
 * @param hexStringId the MongoDB/GridFSFile id of the image, from the backend
 * @param filename name of the image
 * @param metadata must contain a tag key with either Private or Public value
 */
public record ImageSummaryData(String hexStringId, String filename, Document metadata) {
}
