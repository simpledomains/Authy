package de.reynok.authentication.core.security;


import de.reynok.authentication.core.configuration.Constants;
import de.reynok.authentication.core.exception.SecurityTokenExpiredException;
import de.reynok.authentication.core.exception.SecurityTokenInvalidException;
import de.reynok.authentication.core.util.JwtProcessor;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class SpringAuthenticationInterceptor extends HandlerInterceptorAdapter {

    private final JwtProcessor jwtProcessor;

    public SpringAuthenticationInterceptor(JwtProcessor jwtProcessor) {
        this.jwtProcessor = jwtProcessor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        boolean isAllowed = false;

        if (handler instanceof HandlerMethod) {
            RequiresAuthentication annotation = ((HandlerMethod) handler).getMethod().getAnnotation(RequiresAuthentication.class);

            if (request.getCookies() != null) {
                boolean hasCookie = false;

                for (Cookie cookie : request.getCookies()) {
                    if (Constants.COOKIE_NAME.equals(cookie.getName())) {
                        hasCookie = true;
                        try {
                            Claims result = jwtProcessor.validateToken(cookie.getValue());
                            request.setAttribute(Constants.REQUEST_CLAIMS_FIELD, result);
                            isAllowed = true;
                            log.debug("(RequestAllowed) Accessing '{}' is authenticated, proceeding...", request.getRequestURI());
                        } catch (SecurityTokenExpiredException e) {
                            hasCookie = false;
                            log.error("(RequestFailure) Accessing '{}' with a expired token.", request.getRequestURI(), e);
                        } catch (SecurityTokenInvalidException e) {
                            log.error("(RequestDenied) Accessing '{}' got denied, JWT failed to validate.", request.getRequestURI(), e);
                        }
                    }
                }

                if (annotation == null && !hasCookie) {
                    isAllowed = true;
                }
            } else {
                isAllowed = true;
            }
        } else {
            isAllowed = true;
        }

        if (!isAllowed) {
            response.sendError(403);
        }

        return isAllowed;
    }
}