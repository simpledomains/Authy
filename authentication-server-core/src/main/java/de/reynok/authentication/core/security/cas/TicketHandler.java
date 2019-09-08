package de.reynok.authentication.core.security.cas;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CAS Ticket handler to create, invalidate and check CAS tickets.
 *
 * Service-Ticket Format: ST-123456
 * Ticket Granting Ticket format: TGT-123456
 */
@Component
@Slf4j(topic = "CASTicketHandling")
public class TicketHandler {
    private Random               random        = new Random();
    private Map<String, TicketData<?>> ticketMapping = new ConcurrentHashMap<>();

    private Integer getRandomInteger() {
        return random.nextInt((9999999 - 1000000) + 1) + 1000000;
    }

    @Scheduled(fixedDelay = 1000 * 7)
    public void clearExpiredTokens() {
        for (String key: ticketMapping.keySet()) {
            TicketData<?> data = ticketMapping.get(key);

            if (data.isExpired()) {
                log.warn("Ticket {} expired.", key);
                ticketMapping.remove(key);
            }
        }
    }

    public <T> String generateTicketFor(TicketType ticketType, String service, T obj) {
        String ticket = null;

        while(ticket == null) {
            String maybeTicket = ticketType.toString().toUpperCase() + "-" + getRandomInteger();

            if (!ticketMapping.containsKey(maybeTicket)) {
                TicketData<T> data = new TicketData<>();
                data.setContent(obj);
                data.setService(service);
                data.setExpiresAt(new Date(System.currentTimeMillis() + (1000 * 15)));

                log.info("Issued new Ticket for {} with Ticket ID {}", data, maybeTicket);

                ticketMapping.put(maybeTicket, data);

                ticket = maybeTicket;
            }
        }

        return ticket;
    }

    @SuppressWarnings("unchecked")
    public <T> T getTicketData(String ticket, String service) {
        if (ticketMapping.containsKey(ticket)) {
            TicketData<T> data = (TicketData<T>) ticketMapping.get(ticket);
            ticketMapping.remove(ticket);

            log.debug("Ticket {} got removed from the Ticket database.", ticket);

            if (!data.isExpired() && data.getService().equals(service)) {
                log.info("Ticket {} was requested by the service.", ticket);
                return data.getContent();
            } else {
                log.warn("Ticket {} was not request in time or from the wrong service, rejecting.", ticket);
            }
        }

        return null;
    }
}