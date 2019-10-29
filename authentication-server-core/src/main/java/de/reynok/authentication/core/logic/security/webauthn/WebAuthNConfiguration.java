package de.reynok.authentication.core.logic.security.webauthn;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import de.reynok.authentication.core.conf.CASConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class WebAuthNConfiguration {
    @Bean
    public RelyingParty relyingParty(LocalCredentialRepository repository, CASConfiguration casConfiguration) {

        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(casConfiguration.getCookieDomain())
                .name("Authy - CAS Service")
                .build();

        Set<String> origins = new HashSet<>();
        origins.add(casConfiguration.getSystemDomain());

        return RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(repository)
                .origins(origins)
                .build();
    }
}
