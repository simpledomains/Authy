package io.virtuellewolke.authentication.core.cas.store;

import io.virtuellewolke.authentication.core.cas.TicketStore;
import io.virtuellewolke.authentication.core.cas.model.Ticket;
import io.virtuellewolke.authentication.core.exceptions.SecurityTokenExpiredException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@NoArgsConstructor
public class InMemoryTicketStore implements TicketStore {

    private static final int DEFAULT_EXPIRE_TIME = 10;

    /**
     * The name was changed to "expiring" because of the explanation stated here: https://english.stackexchange.com/a/312087
     * <p>
     * Expirable can but dont need to expire, but 'expiring' will always expire someday.
     */
    @Getter
    @Setter
    private static class ExpiringTicket extends Ticket {
        private LocalDateTime expireAfter;

        public ExpiringTicket(Ticket castFrom, LocalDateTime expireAfter) {
            this.setIdentity(castFrom.getIdentity());
            this.setServiceUrl(castFrom.getServiceUrl());
            this.setToken(castFrom.getToken());
            this.setExpireAfter(expireAfter);
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expireAfter);
        }
    }

    private final List<ExpiringTicket> tickets = new CopyOnWriteArrayList<>();

    @Scheduled(fixedDelay = 10000)
    public void cleanup() {
        tickets.removeIf(ExpiringTicket::isExpired);
    }

    @Override
    public Ticket getTicket(String token) {
        ExpiringTicket ticket = getTicketFromMap(token);

        if (ticket != null && !ticket.isExpired()) { return ticket; }

        throw new SecurityTokenExpiredException("Token " + token + " expired.");
    }

    @Override
    public boolean isExpired(String token) {
        ExpiringTicket ticket = getTicketFromMap(token);

        return ticket == null || ticket.isExpired();
    }

    @Override
    public void save(Ticket ticket) {
        tickets.add(new ExpiringTicket(ticket, LocalDateTime.now().plusSeconds(DEFAULT_EXPIRE_TIME)));
    }

    @Override
    public void invalidate(String token) {
        tickets.removeIf(expiringTicket -> Objects.equals(token, expiringTicket.getToken()));
    }

    private ExpiringTicket getTicketFromMap(String token) {
        return tickets.stream().filter(expiringTicket -> Objects.equals(token, expiringTicket.getToken())).findFirst().orElse(null);
    }
}