package de.reynok.authentication.core.backend.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Slf4j
@Configuration
public class RequestLoggerConfiguration {

    @Bean
    public CommonsRequestLoggingFilter commonsRequestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeHeaders(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setAfterMessagePrefix("REQUEST DATA:");
        return loggingFilter;
    }
}