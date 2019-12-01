package de.reynok.authentication.core.backend.configuration.interceptor;

import de.reynok.authentication.core.Constants;
import de.reynok.authentication.core.backend.components.JwtProcessor;
import de.reynok.authentication.core.backend.database.repository.IdentityRepository;
import de.reynok.authentication.core.shared.exceptions.SecurityTokenInvalidException;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Component
public class CookieProcessingInterceptor extends AuthyWebInterceptor {

    public CookieProcessingInterceptor(JwtProcessor jwtProcessor, IdentityRepository identityRepository) {
        super(jwtProcessor, identityRepository);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie[] cookies = request.getCookies() != null ? request.getCookies() : new Cookie[0];

        Optional<Cookie> authCookie = Stream.of(cookies).filter(cookie -> cookie.getName().equals(Constants.COOKIE_NAME)).findFirst();

        authCookie.ifPresent(cookie -> {
            String cookieData = cookie.getValue();
            try {
                Claims claims = getJwtProcessor().validateToken(cookieData);
                request.setAttribute(Constants.REQUEST_CLAIMS_FIELD, claims);
            } catch (SecurityTokenInvalidException e) {
                // do nothing here.
            }
        });

        return true;
    }
}
