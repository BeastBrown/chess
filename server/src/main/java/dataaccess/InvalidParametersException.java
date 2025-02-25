package dataaccess;

/**
 * Indicates that one or more parameters of the underlying request were invalid, NOT empty
 */

public class InvalidParametersException extends DataAccessException {
    public InvalidParametersException(String message) {
        super(message);
    }
}
