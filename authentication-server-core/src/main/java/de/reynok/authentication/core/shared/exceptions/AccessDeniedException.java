package de.reynok.authentication.core.shared.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class AccessDeniedException extends ServiceException {

    private Integer code = 403;

    public AccessDeniedException(String msg) {
        super(msg);
    }
}