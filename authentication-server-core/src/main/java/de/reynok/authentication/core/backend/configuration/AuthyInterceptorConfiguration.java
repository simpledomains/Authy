package de.reynok.authentication.core.backend.configuration;

import de.reynok.authentication.core.backend.configuration.interceptor.AuthyWebInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class AuthyInterceptorConfiguration implements WebMvcConfigurer {

    @Autowired
    private List<AuthyWebInterceptor> webInterceptors;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        webInterceptors.forEach(registry::addInterceptor);
    }
}