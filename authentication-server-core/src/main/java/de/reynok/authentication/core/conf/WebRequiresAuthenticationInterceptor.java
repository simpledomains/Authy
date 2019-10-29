package de.reynok.authentication.core.conf;


import de.reynok.authentication.core.Constants;
import de.reynok.authentication.core.annotations.WebRequiresAuthentication;
import de.reynok.authentication.core.api.exception.AccessDeniedException;
import de.reynok.authentication.core.api.models.Identity;
import de.reynok.authentication.core.logic.database.repository.IdentityRepository;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Slf4j
public class WebRequiresAuthenticationInterceptor extends HandlerInterceptorAdapter {

    private IdentityRepository identityRepository;

    public WebRequiresAuthenticationInterceptor(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        AtomicBoolean allowed = new AtomicBoolean(true);

        Cookie[] cookies = request.getCookies() != null ? request.getCookies() : new Cookie[0];
        Identity identity = null;

        String authorizationHeader = request.getHeader("Authorization");

        if (handler instanceof HandlerMethod) {
            Optional<Cookie> authCookie = Stream.of(cookies).filter(cookie -> cookie.getName().equals(Constants.COOKIE_NAME)).findFirst();

            WebRequiresAuthentication annotation = ((HandlerMethod) handler).getMethod().getAnnotation(WebRequiresAuthentication.class);

            if (authorizationHeader != null && "Bearer".equals(authorizationHeader.split(" ")[0])) {
                identity = identityRepository.findByApiToken(authorizationHeader.split(" ")[1]).orElseThrow(AccessDeniedException::new);
            }

            if (annotation != null) {
                if (authCookie.isEmpty() && identity == null) {
                    allowed.set(false);
                } else {
                    Claims claims = (Claims) request.getAttribute(Constants.REQUEST_CLAIMS_FIELD);

                    if (claims != null) {
                        if (annotation.adminOnly() && !Boolean.parseBoolean(claims.get("administrator", String.class))) {
                            allowed.set(false);
                        }
                    } else {
                        if (identity == null || (annotation.adminOnly() && !identity.getAdmin())) {
                            allowed.set(false);
                        }
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