package ecsimsw.auth.exception;

public class AuthenticateFailedException extends IllegalArgumentException {

    public AuthenticateFailedException(String s) {
        super(s);
    }
}
