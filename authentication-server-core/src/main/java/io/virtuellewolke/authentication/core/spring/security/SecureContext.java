package io.virtuellewolke.authentication.core.spring.security;

import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.entity.Service;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.http.HttpServletRequest;

@Getter
@Setter
@Builder
public class SecureContext {

    enum Source {
        API_TOKEN,
        COOKIE,
        CLIENT_CERT,
        WEBAUTHN
    }

    private Source   source = Source.COOKIE;
    private Identity identity;
    private Service  service;

    public static boolean hasSecureContext(HttpServletRequest request) {
        return request.getAttribute(SecureContextInterceptor.SECURE_CONTEXT) != null;
    }

    public static void setSecureContext(SecureContext secureContext, HttpServletRequest request) {
        request.setAttribute(SecureContextInterceptor.SECURE_CONTEXT, secureContext);
    }

    public static SecureContext getSecureContext(HttpServletRequest request) {
        if (hasSecureContext(request)) {
            return (SecureContext) request.getAttribute(SecureContextInterceptor.SECURE_CONTEXT);
        }
        return null;
    }
}
