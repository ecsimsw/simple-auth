package ecsimsw.auth.exception;

public class InvalidAccessTokenException extends IllegalArgumentException {

    public InvalidAccessTokenException(String s) {
        super(s);
    }
}
