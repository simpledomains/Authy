package de.reynok.authentication.core.util.validation;


import de.reynok.authentication.core.api.models.Identity;
import org.apache.commons.codec.digest.Md5Crypt;

public class Md5PasswordValidator implements Validator<String> {

    private Identity identity;

    public Md5PasswordValidator(Identity identity) {
        this.identity = identity;
    }

    @Override
    public boolean isValid(String argument) {
        String plainEncrypt = Md5Crypt.md5Crypt(argument.getBytes(), identity.getPassword());

        return identity.getPassword().equals(plainEncrypt);
    }
}