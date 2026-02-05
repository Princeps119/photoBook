package berufsschule.raach.data;

public record ImageWithData (String hexString, String filename, org.bson.Document metadata, byte[] byteArray) {}

