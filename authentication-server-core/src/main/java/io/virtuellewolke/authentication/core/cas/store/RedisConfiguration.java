package io.virtuellewolke.authentication.core.cas.store;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Setter
@Getter
@Configuration
@ConditionalOnProperty(prefix = "spring.redis", name = "enabled", havingValue = "true")
@ConfigurationProperties(prefix = "spring.redis")
public class RedisConfiguration extends RedisProperties {
    private Duration ifFailedRetryAfter = Duration.parse("PT0.010S");
    private Integer  maxConnectAttempts = 3;
}