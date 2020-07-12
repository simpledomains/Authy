package io.virtuellewolke.authentication.core.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {
    public static final String REQUEST_CLAIMS_FIELD = "CLAIMS";
    public static final String COOKIE_NAME          = "CASTGC";
    public static final String OTP_SECRET           = "PROFILE_OTP_INIT";
}
