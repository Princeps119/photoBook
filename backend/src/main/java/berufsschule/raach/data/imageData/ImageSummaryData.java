package berufsschule.raach.data.imageData;

import org.bson.Document;

public record ImageSummaryData(String hexStringId, String filename, Document metadata) {
}
