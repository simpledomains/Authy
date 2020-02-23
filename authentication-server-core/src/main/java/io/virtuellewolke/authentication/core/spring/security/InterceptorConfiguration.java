package io.virtuellewolke.authentication.core.spring.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class InterceptorConfiguration implements WebMvcConfigurer {

    private final List<SecureContextInterceptor> secureContextInterceptors;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        secureContextInterceptors.forEach(registry::addInterceptor);
    }
}