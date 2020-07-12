package io.virtuellewolke.authentication.core.spring.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "cas.general")
public class CasConfiguration {
    private String   cookieDomain;
    private Integer  cookieLifeTime    = 60 * 60 * 12;
    private String   cookiePath        = "/";
    private String[] loginWhitelistIps = new String[]{"::1", "127.0.0.1/32", "172.16.0.1/16"};
    private boolean  cookieSecure      = true;

    private String totpIssuerName = "Authy Authentication Service";
}
