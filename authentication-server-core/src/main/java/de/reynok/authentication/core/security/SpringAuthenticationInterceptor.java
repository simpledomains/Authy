package de.reynok.authentication.core.security;


import de.reynok.authentication.core.configuration.Constants;
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

        boolean isAllowed  = true;
        Cookie  authCookie = null;

        if (handler instanceof HandlerMethod) {
            RequiresAuthentication annotation = ((HandlerMethod) handler).getMethod().getAnnotation(RequiresAuthentication.class);

            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (Constants.COOKIE_NAME.equals(cookie.getName())) {
                        authCookie = cookie;
                        break;
                    }
                }
            }

            if (annotation != null) {
                if (authCookie == null) {
                    isAllowed = false;
                } else {
                    Claims claims = jwtProcessor.validateToken(authCookie.getValue());

                    Boolean isAdmin = Boolean.valueOf(claims.get("administrator", String.class));

                    if (annotation.adminOnly() && !isAdmin) {
                        isAllowed = false;
                    }
                }
            }
        }

        if (!isAllowed) {
            response.sendError(403);
        }

        return isAllowed;
    }
}