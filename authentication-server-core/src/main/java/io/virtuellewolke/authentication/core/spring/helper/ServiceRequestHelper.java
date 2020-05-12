package io.virtuellewolke.authentication.core.spring.helper;

import io.virtuellewolke.authentication.core.database.entity.Service;
import io.virtuellewolke.authentication.core.spring.security.mods.AuthyInterceptor;

import javax.servlet.http.HttpServletRequest;

public class ServiceRequestHelper {

    public static boolean hasService(HttpServletRequest request) {
        return request.getAttribute(AuthyInterceptor.SERVICE_CONTEXT) != null;
    }

    public static void setService(Service secureContext, HttpServletRequest request) {
        request.setAttribute(AuthyInterceptor.SERVICE_CONTEXT, secureContext);
    }

    public static Service getService(HttpServletRequest request) {
        if (hasService(request)) {
            return (Service) request.getAttribute(AuthyInterceptor.SERVICE_CONTEXT);
        }
        return null;
    }
}
