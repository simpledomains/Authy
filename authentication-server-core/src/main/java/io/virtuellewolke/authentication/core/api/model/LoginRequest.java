package io.virtuellewolke.authentication.core.api.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LoginRequest {
    private String  username;
    private String  password;
    private String  securityPassword;
    @JsonAlias("cas")
    private Boolean cas = true;
}