package io.virtuellewolke.authentication.core.api;

import io.virtuellewolke.authentication.core.api.model.LoginResponse;
import lombok.Getter;

@Getter
public class LoginFailedException extends RuntimeException {
    private final LoginResponse.ErrorCode errorCode;

    public LoginFailedException(LoginResponse.ErrorCode code) {
        this.errorCode = code;
    }
}