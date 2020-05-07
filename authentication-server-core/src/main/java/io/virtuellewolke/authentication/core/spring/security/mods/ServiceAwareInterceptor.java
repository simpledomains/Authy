package io.virtuellewolke.authentication.core.spring.security.mods;

import io.virtuellewolke.authentication.core.database.entity.Service;
import io.virtuellewolke.authentication.core.spring.components.ServiceValidation;
import io.virtuellewolke.authentication.core.spring.helper.ServiceRequestHelper;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public abstract class ServiceAwareInterceptor implements AuthyInterceptor {

    private static final String SERVICE_PARAMETER = "service";

    private ServiceValidation serviceValidation;

    public ServiceAwareInterceptor(ServiceValidation serviceValidation) {
        this.serviceValidation = serviceValidation;
    }

    public abstract boolean process(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String parameter = getServiceUrlFromRequest(request);

        if (parameter != null) {
            Service service = serviceValidation.getRegisteredServiceFor(parameter);

            log.debug("Detected Service {} for url {}", service, parameter);

            ServiceRequestHelper.setService(service, request);
        }

        return process(request, response, handler);
    }

    private String getServiceUrlFromRequest(HttpServletRequest request) {
        if (request.getParameter(SERVICE_PARAMETER) != null) {
            return request.getParameter(SERVICE_PARAMETER);
        } else if (request.getHeader("x-forwarded-proto") != null) {
            String url = "";

            url += getRequestHeader(request, "x-forwarded-proto");
            url += "://";
            url += getRequestHeader(request, "x-forwarded-host");
            url += getRequestHeader(request, "x-forwarded-uri");

            return url;
        }
        return null;
    }

    private String getRequestHeader(HttpServletRequest request, String parameter) {
        return request.getHeader(parameter) == null ? "" : request.getHeader(parameter);
    }
}