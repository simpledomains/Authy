package io.virtuellewolke.authentication.core.spring.security;

import io.virtuellewolke.authentication.core.database.entity.Service;
import io.virtuellewolke.authentication.core.spring.components.ServiceValidation;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public abstract class ServiceAwareInterceptor implements SecureContextInterceptor {

    private static final String SERVICE_REQUEST_NAME = "AuthyRequestedService";
    private static final String SERVICE_PARAMETER    = "service";

    private ServiceValidation serviceValidation;

    public ServiceAwareInterceptor(ServiceValidation serviceValidation) {
        this.serviceValidation = serviceValidation;
    }

    public abstract boolean process(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String parameter = getServiceUrlFromRequest(request);

        if (parameter != null) {
            Service service = serviceValidation.isAllowed(parameter);

            log.debug("Detected Service {} for url {}", service, parameter);

            if (service != null) {
                request.setAttribute(SERVICE_REQUEST_NAME, service);
            }
        }

        return process(request, response, handler);
    }

    public Service getService(HttpServletRequest request) {
        if (request.getAttribute(SERVICE_REQUEST_NAME) != null) {
            return (Service) request.getAttribute(SERVICE_REQUEST_NAME);
        }
        return null;
    }

    private String getServiceUrlFromRequest(HttpServletRequest request) {
        if (request.getParameter(SERVICE_PARAMETER) != null) {
            return request.getParameter(SERVICE_PARAMETER);
        } else if (request.getHeader("x-forwarded-proto") != null) {
            String url = "";

            url += request.getHeader("x-forwarded-proto");
            url += "://";
            url += request.getHeader("x-forwarded-host");
            url += request.getHeader("x-forwarded-uri");

            return url;
        }
        return null;
    }
}