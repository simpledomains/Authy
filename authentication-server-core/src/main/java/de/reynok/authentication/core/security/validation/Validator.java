package de.reynok.authentication.core.security.validation;

public interface Validator<T> {
    boolean isValid(T argument);
}