package de.reynok.authentication.core.conf;

import de.reynok.authentication.core.Constants;
import de.reynok.authentication.core.api.exception.SecurityTokenInvalidException;
import de.reynok.authentication.core.util.JwtProcessor;
import io.jsonwebtoken.Claims;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.stream.Stream;

public class CookieProcessingInterceptor extends HandlerInterceptorAdapter {

    private final JwtProcessor jwtProcessor;

    public CookieProcessingInterceptor(JwtProcessor jwtProcessor) {
        this.jwtProcessor = jwtProcessor;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies() != null ? request.getCookies() : new Cookie[0];

        Optional<Cookie> authCookie = Stream.of(cookies).filter(cookie -> cookie.getName().equals(Constants.COOKIE_NAME)).findFirst();

        authCookie.ifPresent(cookie -> {
            String cookieData = cookie.getValue();
            try {
                Claims claims = jwtProcessor.validateToken(cookieData);
                request.setAttribute(Constants.REQUEST_CLAIMS_FIELD, claims);
            } catch (SecurityTokenInvalidException e) {
                // do nothing here.
            }
        });

        return true;
    }
}
