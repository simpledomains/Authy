package de.reynok.authentication.core.frontend.api;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class LoginResponse {
    private String message;
    private String location;
}
