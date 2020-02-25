package io.virtuellewolke.authentication.core.cas.store.clients;

import io.virtuellewolke.authentication.core.cas.store.RedisConfiguration;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import java.time.Duration;

@Slf4j
public class JedisConnectionFactory {

    private final RedisConfiguration configuration;

    private Jedis connection;

    private int connectAttempts = 0;

    public JedisConnectionFactory(RedisConfiguration configuration) {
        this.configuration = configuration;
    }

    public Jedis getConnection() {
        testConnection();
        return this.connection;
    }

    @SneakyThrows
    private void testConnection() {
        if (connection == null) recreateConnection();

        try {
            connection.ping();
            connection.select(configuration.getDatabase());
            connectAttempts = 0;
            log.trace("Acquired a jedis connection.");
        } catch (Exception e) {
            recreateConnection();

            connectAttempts++;

            if (connectAttempts > configuration.getMaxConnectAttempts()) {
                connectAttempts = 0;
                log.trace("Failed to acquire a jedis connection after {} attempts.", configuration.getMaxConnectAttempts());
                throw new ClientConnectException("Failed to ping redis. Aborting.");
            } else {
                log.trace("Failed to acquire a jedis connection, retrying in {}ms ({}/{})", configuration.getIfFailedRetryAfter().toMillis(), connectAttempts, configuration.getMaxConnectAttempts());
                Thread.sleep(configuration.getIfFailedRetryAfter().toMillis());
                testConnection();
            }
        }
    }

    private void recreateConnection() {
        if (connection != null) {
            connection.close();
            connection = null;
        }

        Duration timeout = configuration.getTimeout() != null ? configuration.getTimeout() : Duration.parse("PT1S");

        connection = new Jedis(
                configuration.getHost(),
                configuration.getPort(),
                ((Long) timeout.toMillis()).intValue(),
                configuration.isSsl()
        );

        if (configuration.getPassword() != null) {
            connection.auth(configuration.getPassword());
        }
    }
}