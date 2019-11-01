package de.reynok.authentication.core.shared.exceptions;

public class SecurityTokenExpiredException extends SecurityTokenInvalidException {
    public SecurityTokenExpiredException(String s) {
        super(s);
    }

    public SecurityTokenExpiredException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SecurityTokenExpiredException(Throwable throwable) {
        super(throwable);
    }
}