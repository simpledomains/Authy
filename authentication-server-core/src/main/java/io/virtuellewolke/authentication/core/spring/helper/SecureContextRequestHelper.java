package io.virtuellewolke.authentication.core.spring.helper;

import io.virtuellewolke.authentication.core.spring.security.SecureContext;
import io.virtuellewolke.authentication.core.spring.security.mods.AuthyInterceptor;

import javax.servlet.http.HttpServletRequest;

public class SecureContextRequestHelper {
    public static boolean hasSecureContext(HttpServletRequest request) {
        return request.getAttribute(AuthyInterceptor.SECURE_CONTEXT) != null;
    }

    public static void setSecureContext(SecureContext secureContext, HttpServletRequest request) {
        request.setAttribute(AuthyInterceptor.SECURE_CONTEXT, secureContext);
    }

    public static SecureContext getSecureContext(HttpServletRequest request) {
        if (hasSecureContext(request)) {
            return (SecureContext) request.getAttribute(AuthyInterceptor.SECURE_CONTEXT);
        }
        return null;
    }
}
