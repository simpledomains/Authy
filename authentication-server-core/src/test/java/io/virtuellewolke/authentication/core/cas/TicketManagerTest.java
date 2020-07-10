package io.virtuellewolke.authentication.core.cas;

import io.virtuellewolke.authentication.core.cas.model.Ticket;
import io.virtuellewolke.authentication.core.cas.store.InMemoryTicketStore;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.exceptions.AccessDeniedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class TicketManagerTest {

    private final TicketStore   ticketStore   = new InMemoryTicketStore();
    private final TicketManager ticketManager = new TicketManager(ticketStore);

    @Test
    public void testTicketIssueNewWithNullServiceAndNullIdentity() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ticketManager.issue(
                    TicketType.ST,
                    null,
                    null
            );
        });
    }

    @Test
    public void testTicketIssueNewWithNullIdentity() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            ticketManager.issue(TicketType.ST, "/", null);
        });
    }

    @Test
    public void testTicketIssueNew() {
        Identity identity = new Identity();
        identity.setUsername("admin");
        identity.setAuthorities(new ArrayList<>());
        identity.setAdmin(true);

        Ticket ticket = ticketManager.issue(TicketType.ST, "/", identity);

        Assertions.assertNotNull(ticket);
        Assertions.assertNotNull(ticket.getIdentity());
        Assertions.assertNotNull(ticket.getServiceUrl());
        Assertions.assertNotNull(ticket.getToken());
    }

    @Test
    public void testTicketIssueNewAndVerify() {
        Identity identity = new Identity();
        identity.setUsername("admin");
        identity.setAuthorities(new ArrayList<>());
        identity.setAdmin(true);

        Ticket ticket = ticketManager.issue(TicketType.ST, "/", identity);

        Assertions.assertNotNull(ticket);
        Assertions.assertNotNull(ticket.getIdentity());
        Assertions.assertNotNull(ticket.getServiceUrl());
        Assertions.assertNotNull(ticket.getToken());

        Ticket result = ticketManager.getTicket(ticket.getToken(), ticket.getServiceUrl());

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getIdentity());
        Assertions.assertNotNull(result.getServiceUrl());
        Assertions.assertNotNull(result.getToken());

        Assertions.assertEquals(ticket.getIdentity().getUsername(), result.getIdentity().getUsername());
        Assertions.assertEquals(ticket.getServiceUrl(), result.getServiceUrl());
        Assertions.assertEquals(ticket.getToken(), result.getToken());
    }

    //@Test
    public void testTicketIssueNewAndVerifyExpired() throws InterruptedException {
        Identity identity = new Identity();
        identity.setUsername("admin");
        identity.setAuthorities(new ArrayList<>());
        identity.setAdmin(true);

        Ticket ticket = ticketManager.issue(TicketType.ST, "/", identity);

        Assertions.assertNotNull(ticket);
        Assertions.assertNotNull(ticket.getIdentity());
        Assertions.assertNotNull(ticket.getServiceUrl());
        Assertions.assertNotNull(ticket.getToken());

        Thread.sleep(10100L);

        Assertions.assertThrows(AccessDeniedException.class, () -> ticketManager.getTicket(ticket.getToken(), ticket.getServiceUrl()));
    }
}