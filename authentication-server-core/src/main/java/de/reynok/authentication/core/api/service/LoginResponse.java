package de.reynok.authentication.core.api.service;

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
