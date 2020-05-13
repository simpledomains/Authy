package io.virtuellewolke.authentication.core.spring.security;

import io.virtuellewolke.authentication.core.spring.helper.SecureContextRequestHelper;
import io.virtuellewolke.authentication.core.spring.security.annotations.AdminResource;
import io.virtuellewolke.authentication.core.spring.security.annotations.AuthorizedResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
public class AuthorizationRequiredInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean isAllowed = true;

        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            AdminResource      adminOnly  = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), AdminResource.class);
            AuthorizedResource authorized = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), AuthorizedResource.class);

            boolean requiresAuth = adminOnly != null || authorized != null;

            if (requiresAuth && !SecureContextRequestHelper.hasSecureContext(request)) {
                isAllowed = false;
            } else if (requiresAuth) {
                SecureContext context = SecureContextRequestHelper.getSecureContext(request);
                assert context != null; // context cant be null here, but the IDE is thinking it can be.

                log.debug("{} {} requested by {} (admin?={}, authorized?={})", request.getMethod(), request.getRequestURI(), context, adminOnly != null, authorized != null);

                if (adminOnly != null) {
                    isAllowed = context.getIdentity() != null && context.getIdentity().getAdmin() && !context.getIdentity().getLocked();
                } else {
                    isAllowed = context.getIdentity() != null && !context.getIdentity().getLocked();
                }

                // authorized can be mixed with AdminResource to only allow certain scopes.
                if (authorized != null && authorized.allowedSources().length > 0 && Arrays.stream(authorized.allowedSources()).noneMatch(source -> Objects.equals(source, context.getSource()))) {
                    log.warn("{} {} was request by source {} but is only allowed by source {} from {}",
                            request.getMethod(), request.getRequestURI(), context.getSource(), authorized.allowedSources(), request.getRemoteAddr());
                    isAllowed = false;
                }
            }
        }

        if (!isAllowed) {
            log.warn("{} {} from {} was denied", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
            response.sendError(401);
        } else {
            log.debug("{} {} from {} accepted.", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
        }

        return isAllowed;
    }
}