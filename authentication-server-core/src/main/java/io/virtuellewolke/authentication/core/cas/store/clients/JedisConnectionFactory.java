package io.virtuellewolke.authentication.core.cas.store.clients;

import io.virtuellewolke.authentication.core.cas.store.RedisConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import redis.clients.jedis.Jedis;

@RequiredArgsConstructor
public class JedisConnectionFactory {

    private final RedisConfiguration configuration;

    private Jedis connection;

    private int  connectAttempts = 0;
    @Setter
    private int  maxAttempts     = 1;
    @Setter
    private long retryTimeout    = 10L;

    public Jedis getConnection() {
        testConnection();
        return this.connection;
    }

    @SneakyThrows
    private void testConnection() {
        if (connection == null) recreateConnection();

        try {
            connection.ping();
            connectAttempts = 0;
        } catch (Exception e) {
            recreateConnection();

            connectAttempts++;

            if (connectAttempts > maxAttempts) {
                throw new ClientConnectException("Failed to ping redis. Aborting.");
            } else {
                Thread.sleep(retryTimeout);
                testConnection();
            }
        }
    }

    private void recreateConnection() {
        if (connection != null) {
            connection.close();
            connection = null;
        }

        connection = new Jedis(
                configuration.getHost(),
                configuration.getPort(),
                configuration.isSsl()
        );

        if (configuration.getPassword() != null) {
            connection.auth(configuration.getPassword());
        }
    }
}