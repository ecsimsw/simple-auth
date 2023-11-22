package ecsimsw.auth.exception;

public class SimpleAuthException extends IllegalArgumentException {

    public SimpleAuthException(String s) {
        super(s);
    }

    public SimpleAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
