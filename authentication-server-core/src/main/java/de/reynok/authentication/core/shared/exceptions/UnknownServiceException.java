package de.reynok.authentication.core.shared.exceptions;

public class UnknownServiceException extends ServiceException {
    public UnknownServiceException(String message) {
        super(message);
    }

    public UnknownServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}