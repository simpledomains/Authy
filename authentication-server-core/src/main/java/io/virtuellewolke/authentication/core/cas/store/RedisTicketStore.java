package io.virtuellewolke.authentication.core.cas.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.virtuellewolke.authentication.core.exceptions.SecurityTokenExpiredException;
import io.virtuellewolke.authentication.core.exceptions.UnknownServiceException;
import io.virtuellewolke.authentication.core.cas.TicketStore;
import io.virtuellewolke.authentication.core.cas.model.Ticket;
import io.virtuellewolke.authentication.core.cas.store.clients.JedisConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import redis.clients.jedis.Jedis;

import java.util.function.Supplier;

@Slf4j
@RequiredArgsConstructor
public class RedisTicketStore implements TicketStore {

    private static final String REDIS_KEY = "cas.ticket.%s";

    private final JedisConnectionFactory jedisConnectionFactory;
    private final ObjectMapper           objectMapper;

    private final InMemoryTicketStore fallback = new InMemoryTicketStore();

    @Setter
    private boolean fallbackEnabled = true;

    @Scheduled(fixedDelay = 10000)
    public void cleanupFallback() {
        fallback.cleanup();
    }

    @Override
    public Ticket getTicket(String token) {
        return resilienceCall(
                () -> {
                    String json = getRedisClient().get(formatRedisKey(token));

                    if (json != null) {
                        try {
                            return objectMapper.readValue(json, Ticket.class);
                        } catch (JsonProcessingException e) {
                            throw new UnknownServiceException("Failed to read JSON from redis database", e);
                        }
                    } else {
                        throw new SecurityTokenExpiredException("Token " + token + " expired.");
                    }
                },
                () -> fallback.getTicket(token)
        );
    }

    @Override
    public boolean isExpired(String token) {
        return resilienceCall(
                () -> !getRedisClient().exists(formatRedisKey(token)),
                () -> fallback.isExpired(token)
        );
    }

    @Override
    public void save(Ticket ticket) {
        resilienceCall(
                () -> {
                    Jedis jedis = getRedisClient();

                    jedis.set(formatRedisKey(ticket.getToken()), objectMapper.writeValueAsString(ticket));
                    jedis.expire(formatRedisKey(ticket.getToken()), 15);
                },
                () -> fallback.save(ticket)
        );
    }

    @Override
    public void invalidate(String token) {
        resilienceCall(
                () -> getRedisClient().del(formatRedisKey(token)),
                () -> fallback.invalidate(token)
        );
    }

    private String formatRedisKey(String token) {
        return String.format(REDIS_KEY, token);
    }

    private <T> T resilienceCall(Supplier<T> callback, Supplier<T> resilienceCallback) {
        try {
            return callback.get();
        } catch (Exception e) {
            if (fallbackEnabled) {
                log.warn("There is a problem with Redis, using Fallback.");
                return resilienceCallback.get();
            } else {
                throw e;
            }
        }
    }

    @SneakyThrows
    private void resilienceCall(Callback callback, Callback resilienceCallback) {
        try {
            callback.exec();
        } catch (Exception e) {
            if (fallbackEnabled) {
                log.warn("There is a problem with Redis, using Fallback.");
                resilienceCallback.exec();
            } else {
                throw e;
            }
        }
    }

    public Jedis getRedisClient() {
        return jedisConnectionFactory.getConnection();
    }

    @FunctionalInterface
    private interface Callback {
        void exec() throws Exception;
    }
}