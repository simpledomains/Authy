package de.reynok.authentication.core.security;

import de.reynok.authentication.core.security.injection.RequestProcessingInterceptor;
import de.reynok.authentication.core.security.injection.SecurityInterceptor;
import de.reynok.authentication.core.util.JwtProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SecurityConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtProcessor jwtProcessor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestProcessingInterceptor(jwtProcessor));
        registry.addInterceptor(new SecurityInterceptor(jwtProcessor));
    }
}