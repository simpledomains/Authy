package de.reynok.authentication.core.util.validation;

import com.amdelamar.jotp.OTP;
import com.amdelamar.jotp.type.Type;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class OneTimePasswordValidator implements Validator<String> {

    private String token;

    public OneTimePasswordValidator(String securityToken) {
        this.token = securityToken;
    }

    @Override
    public boolean isValid(String argument) {
        if (token == null) return true;

        try {
            return OTP.verify(token, OTP.timeInHex(), argument, 6, Type.TOTP);
        } catch (InvalidKeyException | NoSuchAlgorithmException | IOException e) {
            return false;
        }
    }
}
