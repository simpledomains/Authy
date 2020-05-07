package de.reynok.authentication.core.frontend.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Builder
@Accessors(chain = true)
public class LoginResponse {

    public enum ErrorCode {
        CREDENTIAL_ERROR,
        SERVICE_NOT_ALLOWED,
        OTP_REQUIRED, USER_ACCOUNT_DENIED, USER_ACCOUNT_BLOCKED
    }

    private ErrorCode errorCode;
    private String    message;
    private String    location;
    private String    token;
}
