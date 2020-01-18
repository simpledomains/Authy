package de.reynok.authentication.core.backend.configuration;

import de.reynok.authentication.core.backend.configuration.interceptor.CookieProcessingInterceptor;
import de.reynok.authentication.core.backend.configuration.interceptor.WebRequiresAuthenticationInterceptor;
import de.reynok.authentication.core.backend.configuration.interceptor.X509CertificateAuthenticatorInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class AuthyInterceptorConfiguration implements WebMvcConfigurer {

    private final CookieProcessingInterceptor             cookieProcessingInterceptor;
    private final WebRequiresAuthenticationInterceptor    authenticationInterceptor;
    private final X509CertificateAuthenticatorInterceptor x509CertificateAuthenticatorInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cookieProcessingInterceptor);
        registry.addInterceptor(x509CertificateAuthenticatorInterceptor);
        registry.addInterceptor(authenticationInterceptor);
    }
}