package io.virtuellewolke.authentication.core.cas;

import io.virtuellewolke.authentication.core.cas.model.Ticket;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.exceptions.AccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketManager {
    private final TicketStore ticketStore;

    private final Random random = new SecureRandom();

    @Value("${app.security.service-validation:#{true}}")
    private boolean isServiceValidationEnabled;


    public Ticket issue(TicketType ticketType, String service, Identity identity) {
        String token = null;

        while (token == null) {
            String maybeToken = ticketType.toString().toUpperCase() + "-" + getRandomInteger();
            if (ticketStore.isExpired(maybeToken)) {
                token = maybeToken;
            }
        }

        if (service == null || identity == null) {
            throw new IllegalArgumentException("Neither identity or service can be null");
        }

        Ticket data = new Ticket();
        data.setIdentity(identity);
        data.setServiceUrl(service);
        data.setToken(token);

        log.info("Issued new Ticket {}", data);

        ticketStore.save(data);

        return data;
    }

    public Ticket getTicket(String token, String service) {
        if (!ticketStore.isExpired(token)) {
            Ticket ticket = ticketStore.getTicket(token);

            log.debug("Ticket {} got removed from the Ticket database.", token);

            if (Objects.equals(ticket.getServiceUrl(), service) || !isServiceValidationEnabled) {
                log.info("Ticket {} was requested by the service.", token);
                ticketStore.invalidate(token);
                return ticket;
            } else {
                log.warn("Ticket {} was requested from the wrong Service. [{}] is not equal to [{}]", token, service, ticket.getServiceUrl());
                throw new AccessDeniedException("Ticket " + token + " was requested from the wrong Service. [" + service + "] is not equal to [" + ticket.getServiceUrl() + "]");
            }
        } else {
            log.warn("Ticket {} does not exist or is expired.", token);
            throw new AccessDeniedException("Ticket " + token + " does not exist or is expired.");
        }
    }


    private Integer getRandomInteger() {
        return random.nextInt((9999999 - 1000000) + 1) + 1000000;
    }
}