package de.reynok.authentication.core.web.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginFailedResponse {
    private String message;
}