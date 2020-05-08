package de.reynok.authentication.core.backend.configuration.interceptor;

import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import io.virtuellewolke.authentication.core.spring.components.JwtProcessor;
import io.virtuellewolke.authentication.core.spring.components.X509Manager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class X509CertificateAuthenticatorInterceptor extends AuthyWebInterceptor {

    @Autowired
    public X509CertificateAuthenticatorInterceptor(JwtProcessor jwtProcessor, IdentityRepository identityRepository, X509Manager x509Manager) {
        super(jwtProcessor, identityRepository);
    }
}
