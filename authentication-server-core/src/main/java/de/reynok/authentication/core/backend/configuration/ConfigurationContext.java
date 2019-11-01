package de.reynok.authentication.core.backend.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "cas")
public class ConfigurationContext {
    private String cookieDomain;
    private String systemDomain;

    private Integer cookieMaxAge = 60 * 60 * 12;
    private String cookiePath = "/";
    private String cookieComment = "Authy CAS Token";
    private String tokenSecret;
}