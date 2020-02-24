package io.virtuellewolke.authentication.core.cas.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.virtuellewolke.authentication.core.cas.model.Ticket;
import io.virtuellewolke.authentication.core.cas.store.clients.JedisConnectionFactory;
import org.junit.jupiter.api.*;
import redis.embedded.RedisServer;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisTicketStoreTest {

    private static final String TOKEN = "ST-123456";

    private static RedisServer      redisServer;
    private static RedisTicketStore ticketStore;

    @BeforeAll
    public static void setupRedis() {
        redisServer = new RedisServer();
        redisServer.start();
    }

    @BeforeAll
    public static void setupTicketStore() {
        ObjectMapper       objm          = new ObjectMapper();
        RedisConfiguration configuration = new RedisConfiguration();
        configuration.setHost("localhost");
        configuration.setPort(6379);
        configuration.setSsl(false);

        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(configuration);

        ticketStore = new RedisTicketStore(connectionFactory, objm);
        ticketStore.setFallbackEnabled(false);
    }

    @AfterAll
    public static void destroyRedis() {
        redisServer.stop();
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