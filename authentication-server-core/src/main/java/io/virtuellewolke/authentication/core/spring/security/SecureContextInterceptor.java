package io.virtuellewolke.authentication.core.spring.security;

import org.springframework.web.servlet.HandlerInterceptor;

public interface SecureContextInterceptor extends HandlerInterceptor {
    String SECURE_CONTEXT = "AuthySecureContext";
}