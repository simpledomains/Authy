package de.reynok.authentication.core.util.validation;

public interface Validator<T> {
    boolean isValid(T argument);
}