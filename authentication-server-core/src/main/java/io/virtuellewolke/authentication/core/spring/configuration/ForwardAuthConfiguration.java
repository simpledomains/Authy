package io.virtuellewolke.authentication.core.spring.configuration;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;

@Setter
@Configuration
@ConfigurationProperties(prefix = "forward-auth")
public class ForwardAuthConfiguration {
    private String baseDomain;

    public String getBaseDomain(HttpServletRequest request) {
        if (this.baseDomain == null) {
            return String.format("%s://%s", request.getScheme(), request.getServerName());
        }
        return this.baseDomain;
    }
}
