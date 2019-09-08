package de.reynok.authentication.core.web.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoginRequest {
    private String username;
    private String password;
    private String totp;
    @JsonAlias("no-cas")
    private Boolean noCas;
}
