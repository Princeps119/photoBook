package berufsschule.raach.exeptions;

public class EncryptionException extends RuntimeException {
    public EncryptionException(String message) {
        super(message);
    }
    public EncryptionException(String message, Exception e) {
        super(message, e);
    }

}


