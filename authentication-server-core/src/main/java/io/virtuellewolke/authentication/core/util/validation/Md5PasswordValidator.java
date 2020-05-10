package io.virtuellewolke.authentication.core.util.validation;


import io.virtuellewolke.authentication.core.database.entity.Identity;
import org.apache.commons.codec.digest.Md5Crypt;

public class Md5PasswordValidator implements Validator<String> {

    private final Identity identity;

    public Md5PasswordValidator(Identity identity) {
        this.identity = identity;
    }

    @Override
    public boolean isValid(String argument) {
        if (argument == null || identity == null) return false;

        String plainEncrypt = Md5Crypt.md5Crypt(argument.getBytes(), identity.getPassword());

        return identity.getPassword().equals(plainEncrypt);
    }
}