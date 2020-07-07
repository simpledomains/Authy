package io.virtuellewolke.authentication.core.spring.configuration;

import io.sentry.spring.SentryExceptionResolver;
import io.virtuellewolke.authentication.core.exceptions.AccessDeniedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletContextInitializer;
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

    @Bean
    public ServletContextInitializer sentryServletContextInitializer() {
        return new io.sentry.spring.SentryServletContextInitializer();
    }
}
