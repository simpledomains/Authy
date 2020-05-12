package io.virtuellewolke.authentication.core.cas;

import io.virtuellewolke.authentication.core.cas.model.Ticket;

public interface TicketStore {
    Ticket getTicket(String token);

    boolean isExpired(String token);

    void save(Ticket ticket);

    void invalidate(String token);
}