package GameLogic;

public class XMLFileParsingException extends RuntimeException {

    public XMLFileParsingException() {

    }

    public XMLFileParsingException(String message) {
        super(message);

    }

    public XMLFileParsingException(Throwable cause) {
        super(cause);

    }

    public XMLFileParsingException(String message, Throwable cause) {
        super(message, cause);

    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}