package io.virtuellewolke.authentication.core.exceptions;

public class SecurityTokenInvalidException extends ServiceException {
    public SecurityTokenInvalidException(String s) {
        super(s);
    }

    public SecurityTokenInvalidException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SecurityTokenInvalidException(Throwable throwable) {
        super(throwable);
    }
}