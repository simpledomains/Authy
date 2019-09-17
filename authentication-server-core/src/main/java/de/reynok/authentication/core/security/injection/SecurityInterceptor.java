package de.reynok.authentication.core.security.injection;


import de.reynok.authentication.core.Constants;
import de.reynok.authentication.core.security.RequiresAuthentication;
import de.reynok.authentication.core.util.JwtProcessor;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Slf4j
public class SecurityInterceptor extends HandlerInterceptorAdapter {

    private final JwtProcessor jwtProcessor;

    public SecurityInterceptor(JwtProcessor jwtProcessor) {
        this.jwtProcessor = jwtProcessor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        AtomicBoolean allowed = new AtomicBoolean(true);

        Cookie[] cookies = request.getCookies() != null ? request.getCookies() : new Cookie[0];


        if (handler instanceof HandlerMethod) {
            Optional<Cookie> authCookie = Stream.of(cookies).filter(cookie -> cookie.getName().equals(Constants.COOKIE_NAME)).findFirst();

            RequiresAuthentication annotation = ((HandlerMethod) handler).getMethod().getAnnotation(RequiresAuthentication.class);

            if (annotation != null) {
                if (authCookie.isEmpty()) {
                    allowed.set(false);
                } else {
                    Claims claims = (Claims) request.getAttribute(Constants.REQUEST_CLAIMS_FIELD);

                    if (claims == null || (annotation.adminOnly() && !Boolean.parseBoolean(claims.get("administrator", String.class)))) {
                        allowed.set(false);
                    }
                }
            }
        }

        if (!allowed.get()) {
            response.sendError(403);
        }

        return allowed.get();
    }
}