package de.reynok.authentication.core.backend.configuration.interceptor;

import de.reynok.authentication.core.backend.database.repository.IdentityRepository;
import de.reynok.authentication.core.backend.components.JwtProcessor;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Getter(AccessLevel.PROTECTED)
public abstract class AuthyWebInterceptor extends HandlerInterceptorAdapter {

    private JwtProcessor jwtProcessor;
    private IdentityRepository identityRepository;

    public AuthyWebInterceptor(JwtProcessor jwtProcessor, IdentityRepository identityRepository) {
        this.jwtProcessor = jwtProcessor;
        this.identityRepository = identityRepository;
    }
}