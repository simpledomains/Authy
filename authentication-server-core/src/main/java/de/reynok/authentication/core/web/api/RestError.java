package de.reynok.authentication.core.web.api;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
public class RestError {
    private String  exception;
    private String  message;
    private Integer status;

    public RestError(Throwable e, String message, Integer status) {
        this.exception = e.getClass().getSimpleName();
        this.message   = message;
        this.status    = status;
    }

    public ResponseEntity<RestError> toResponse() {
        return ResponseEntity.status(this.status).body(this);
    }
}
