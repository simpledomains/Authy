package io.virtuellewolke.authentication.core.exceptions;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@NoArgsConstructor
public class AccessDeniedException extends ServiceException {
    public AccessDeniedException(String msg) {
        super(msg);
    }
}