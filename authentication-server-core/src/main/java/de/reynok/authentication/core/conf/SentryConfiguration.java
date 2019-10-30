package de.reynok.authentication.core.conf;

import io.sentry.spring.SentryExceptionResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "sentry", name = "dsn")
public class SentryConfiguration {
    public SentryConfiguration() {
        log.info("Sentry configured and enabled.");
    }

    @Bean
    public HandlerExceptionResolver sentry() {
        return new SentryExceptionResolver();
    }
}