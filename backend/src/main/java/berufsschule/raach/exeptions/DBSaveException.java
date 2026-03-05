package berufsschule.raach.exeptions;

public class DBSaveException extends RuntimeException {
    public DBSaveException(String message, Exception e) {
        super(message, e);
    }

}
