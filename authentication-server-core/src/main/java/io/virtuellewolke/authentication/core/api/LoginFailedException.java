package io.virtuellewolke.authentication.core.api;

import de.reynok.authentication.core.frontend.api.LoginResponse;
import lombok.Getter;

@Getter
public class LoginFailedException extends RuntimeException {
    private LoginResponse.ErrorCode errorCode;

    public LoginFailedException(LoginResponse.ErrorCode code) {
        this.errorCode = code;
    }
}