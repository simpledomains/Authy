package de.reynok.authentication.core.exception;

import io.jsonwebtoken.SignatureException;

public class SecurityTokenInvalidException extends Exception {
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