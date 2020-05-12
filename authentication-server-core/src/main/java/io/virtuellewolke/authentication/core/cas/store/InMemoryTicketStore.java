package io.virtuellewolke.authentication.core.cas.store;

import io.virtuellewolke.authentication.core.exceptions.SecurityTokenExpiredException;
import io.virtuellewolke.authentication.core.cas.TicketStore;
import io.virtuellewolke.authentication.core.cas.model.Ticket;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class InMemoryTicketStore implements TicketStore {

    @Getter
    @Setter
    private static class ExpirableTicket extends Ticket {
        private LocalDateTime expireAfter;

        public ExpirableTicket(Ticket castFrom, LocalDateTime expireAfter) {
            this.setIdentity(castFrom.getIdentity());
            this.setServiceUrl(castFrom.getServiceUrl());
            this.setToken(castFrom.getToken());
            this.setExpireAfter(expireAfter);
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expireAfter);
        }
    }

    private final List<ExpirableTicket> tickets = new CopyOnWriteArrayList<>();

    @Scheduled(fixedDelay = 10000)
    public void cleanup() {
        tickets.removeIf(ExpirableTicket::isExpired);
    }

    @Override
    public Ticket getTicket(String token) {
        ExpirableTicket ticket = getTicketFromMap(token);

        if (ticket != null && !ticket.isExpired()) return ticket;

        throw new SecurityTokenExpiredException("Token " + token + " expired.");
    }

    @Override
    public boolean isExpired(String token) {
        ExpirableTicket ticket = getTicketFromMap(token);

        return ticket == null || ticket.isExpired();
    }

    @Override
    public void save(Ticket ticket) {
        tickets.add(new ExpirableTicket(ticket, LocalDateTime.now().plusSeconds(20)));
    }

    @Override
    public void invalidate(String token) {
        tickets.removeIf(expirableTicket -> Objects.equals(token, expirableTicket.getToken()));
    }

    private ExpirableTicket getTicketFromMap(String token) {
        return tickets.stream().filter(expirableTicket -> Objects.equals(token, expirableTicket.getToken())).findFirst().orElse(null);
    }
}