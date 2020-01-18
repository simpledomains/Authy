package de.reynok.authentication.core.backend.configuration;

import de.reynok.authentication.core.shared.exceptions.AccessDeniedException;
import io.sentry.spring.SentryExceptionResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "sentry", name = "dsn")
public class SentryConfiguration {
    public SentryConfiguration() {
        log.info("Sentry configured and enabled.");
    }

    @Bean
    public HandlerExceptionResolver sentry() {
        return new SentryExceptionResolver() {
            @Override
            public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
                if (ex instanceof AccessDeniedException) return null;
                return super.resolveException(request, response, handler, ex);
            }
        };
    }
}