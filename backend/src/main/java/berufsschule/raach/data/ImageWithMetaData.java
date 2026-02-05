package berufsschule.raach.data;

public record ImageWithMetaData(String hexString, String filename, org.bson.Document metadata, byte[] byteArray) {}

