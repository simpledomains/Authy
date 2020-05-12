package io.virtuellewolke.authentication.core.cas.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.virtuellewolke.authentication.core.cas.TicketStore;
import io.virtuellewolke.authentication.core.cas.store.clients.JedisConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Slf4j
@Configuration
public class StoreConfiguration {

    @Bean
    @Order
    @ConditionalOnProperty(prefix = "spring.redis", name = "enabled", havingValue = "true")
    public TicketStore redisTicketStore(RedisConfiguration config, ObjectMapper objectMapper) {
        log.info("Ticket-Store enabled: Redis");
        return new RedisTicketStore(new JedisConnectionFactory(config), objectMapper);
    }

    @Bean
    @Order
    @ConditionalOnMissingBean(TicketStore.class)
    public TicketStore inMemoryTicketStore() {
        log.info("Ticket-Store enabled: In-Memory");
        return new InMemoryTicketStore();
    }
}
