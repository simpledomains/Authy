package de.reynok.authentication.core.backend.modules.webauthn;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import de.reynok.authentication.core.backend.configuration.ConfigurationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class WebAuthNConfiguration {
    @Bean
    public RelyingParty relyingParty(LocalCredentialRepository repository, ConfigurationContext configurationContext) {

        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(configurationContext.getCookieDomain())
                .name("Authy - CAS Service")
                .build();

        Set<String> origins = new HashSet<>();
        origins.add(configurationContext.getSystemDomain());

        return RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(repository)
                .origins(origins)
                .build();
    }
}
