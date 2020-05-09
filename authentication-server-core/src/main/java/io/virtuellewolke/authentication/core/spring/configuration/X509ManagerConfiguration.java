package io.virtuellewolke.authentication.core.spring.configuration;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Getter
@Configuration
@ConfigurationProperties(prefix = "cas.x509")
public class X509ManagerConfiguration {
    private File    caPublicCert           = new File("ca.pub.pem");
    private File    caPrivateKey           = new File("ca.pem");
    private String  caPrivateKeyPassphrase = "schnitzel";
    private String  opensslBinary          = "openssl";
    private Long    cleanupTimer           = 1800000L;
    private Boolean enabled                = true;

    private String httpHeaderName = "X-SSL-Cert";

    private String organisation = "Authentication Server";
    private String countryCode  = "DE";
}