package de.reynok.authentication.core.conf;

import de.reynok.authentication.core.logic.database.repository.IdentityRepository;
import de.reynok.authentication.core.util.JwtProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcInterceptorConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtProcessor       jwtProcessor;
    @Autowired
    private IdentityRepository identityRepository;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CookieProcessingInterceptor(jwtProcessor));
        registry.addInterceptor(new WebRequiresAuthenticationInterceptor(identityRepository));
    }
}