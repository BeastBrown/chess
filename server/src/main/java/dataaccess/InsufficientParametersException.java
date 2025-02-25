package dataaccess;

/**
 * Indicates that a parameter was missing or empty in the request
 */

public class InsufficientParametersException extends DataAccessException {
    public InsufficientParametersException(String message) {
        super(message);
    }
}
