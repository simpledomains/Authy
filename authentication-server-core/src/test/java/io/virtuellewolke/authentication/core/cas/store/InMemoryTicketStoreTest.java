package io.virtuellewolke.authentication.core.cas.store;

import io.virtuellewolke.authentication.core.cas.model.Ticket;
import org.junit.jupiter.api.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InMemoryTicketStoreTest {

    private static final String TOKEN = "ST-123456";

    private static InMemoryTicketStore ticketStore;


    @BeforeAll
    public static void setupTicketStore() {
        ticketStore = new InMemoryTicketStore();
    }

    @Test
    @Order(1)
    public void testIssueToken() {
        ticketStore.save(getTicket());
    }

    @Test
    @Order(2)
    public void testIsNotExpired() {
        boolean expired = ticketStore.isExpired(TOKEN);
        Assertions.assertFalse(expired);
    }

    @Test
    @Order(3)
    public void testGetTicket() {
        Ticket ticket = ticketStore.getTicket(TOKEN);
        ticketStore.invalidate(TOKEN);
        Assertions.assertNotNull(ticket);
    }

    @Test
    @Order(4)
    public void testIsExpired() {
        boolean expired = ticketStore.isExpired(TOKEN);
        Assertions.assertTrue(expired);
    }

    private Ticket getTicket() {
        Ticket ticket = new Ticket();

        ticket.setToken(TOKEN);
        ticket.setServiceUrl("/");
        ticket.setIdentity(null);

        return ticket;
    }
}