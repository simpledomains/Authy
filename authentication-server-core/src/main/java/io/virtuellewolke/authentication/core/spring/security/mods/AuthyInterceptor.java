package io.virtuellewolke.authentication.core.spring.security.mods;

import org.springframework.web.servlet.HandlerInterceptor;

public interface AuthyInterceptor extends HandlerInterceptor {
    String SECURE_CONTEXT  = "AuthySecureContext";
    String SERVICE_CONTEXT = "AuthyServiceContext";
}