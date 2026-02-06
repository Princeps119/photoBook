package berufsschule.raach.data;

import org.bson.Document;

public record ImageSummaryData(String hexStringId, String filename, Document metadata) {
}
