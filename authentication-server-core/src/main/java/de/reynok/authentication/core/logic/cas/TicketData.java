package de.reynok.authentication.core.logic.cas;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString(exclude = "content")
public class TicketData<T> {
    private T    content;
    private String service;
    private Date expiresAt;

    public boolean isExpired() {
        return expiresAt == null || (new Date()).after(expiresAt);
    }
}
