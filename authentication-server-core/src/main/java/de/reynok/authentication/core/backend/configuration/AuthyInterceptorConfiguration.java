package de.reynok.authentication.core.backend.configuration;

import de.reynok.authentication.core.backend.configuration.interceptor.CookieProcessingInterceptor;
import de.reynok.authentication.core.backend.configuration.interceptor.WebRequiresAuthenticationInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AuthyInterceptorConfiguration implements WebMvcConfigurer {

    @Autowired
    private CookieProcessingInterceptor          cookieProcessingInterceptor;
    @Autowired
    private WebRequiresAuthenticationInterceptor authenticationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cookieProcessingInterceptor);
        registry.addInterceptor(authenticationInterceptor);
    }
}