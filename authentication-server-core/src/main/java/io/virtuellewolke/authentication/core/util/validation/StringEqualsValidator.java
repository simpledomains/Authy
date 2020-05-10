package io.virtuellewolke.authentication.core.util.validation;


import java.util.Objects;

public class StringEqualsValidator implements Validator<String> {

    private final String validateAgainst;

    public StringEqualsValidator(String validateAgainst) {
        this.validateAgainst = validateAgainst;
    }

    @Override
    public boolean isValid(String argument) {
        return Objects.equals(argument, validateAgainst);
    }
}