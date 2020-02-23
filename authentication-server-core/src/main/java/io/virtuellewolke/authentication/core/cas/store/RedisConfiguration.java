package io.virtuellewolke.authentication.core.cas.store;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "spring.redis", name = "enabled", havingValue = "true")
@ConfigurationProperties(prefix = "spring.redis")
public class RedisConfiguration extends RedisProperties {
}