package de.reynok.authentication.core.shared.util.validation;

import com.amdelamar.jotp.OTP;
import com.amdelamar.jotp.type.Type;

public class OneTimePasswordValidator implements Validator<String> {

    private String token;

    public OneTimePasswordValidator(String securityToken) {
        this.token = securityToken;
    }

    @Override
    public boolean isValid(String argument) {
        if (token == null) { return true; }

        try {
            return OTP.verify(token, OTP.timeInHex(), argument, 6, Type.TOTP);
        } catch (Throwable e) {
            return false;
        }
    }
}
