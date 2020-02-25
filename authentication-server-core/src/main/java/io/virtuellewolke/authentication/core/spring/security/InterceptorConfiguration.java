package io.virtuellewolke.authentication.core.spring.security;

import io.virtuellewolke.authentication.core.spring.security.mods.AuthyInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class InterceptorConfiguration implements WebMvcConfigurer {

    private final List<AuthyInterceptor> authyInterceptors;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        authyInterceptors.forEach(registry::addInterceptor);
        registry.addInterceptor(new AuthorizationRequiredInterceptor());
    }
}