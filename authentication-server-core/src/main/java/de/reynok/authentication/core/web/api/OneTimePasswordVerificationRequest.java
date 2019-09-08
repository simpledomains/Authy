package de.reynok.authentication.core.web.api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OneTimePasswordVerificationRequest {
    private String totp;
}
