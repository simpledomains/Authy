package de.reynok.authentication.core.backend.configuration.interceptor;


import de.reynok.authentication.core.Constants;
import de.reynok.authentication.core.backend.configuration.WebRequiresAuthentication;
import de.reynok.authentication.core.backend.components.JwtProcessor;
import de.reynok.authentication.core.shared.exceptions.AccessDeniedException;
import de.reynok.authentication.core.backend.database.entity.Identity;
import de.reynok.authentication.core.backend.database.repository.IdentityRepository;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Slf4j
@Component
public class WebRequiresAuthenticationInterceptor extends AuthyWebInterceptor {

    public WebRequiresAuthenticationInterceptor(JwtProcessor jwtProcessor, IdentityRepository identityRepository) {
        super(jwtProcessor, identityRepository);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(request.getSession().getAttribute("customerId") == null) {
            response.sendError(403);
            return false;
        }

        AtomicBoolean allowed = new AtomicBoolean(true);

        Cookie[] cookies  = request.getCookies() != null ? request.getCookies() : new Cookie[0];
        Identity identity = null;

        String authorizationHeader = request.getHeader("Authorization");

        if (handler instanceof HandlerMethod) {
            Optional<Cookie> authCookie = Stream.of(cookies).filter(cookie -> cookie.getName().equals(Constants.COOKIE_NAME)).findFirst();

            log.debug("Is cookie there? {}", authCookie.isPresent());

            WebRequiresAuthentication annotation = ((HandlerMethod) handler).getMethod().getAnnotation(WebRequiresAuthentication.class);

            if (authorizationHeader != null && "Bearer".equals(authorizationHeader.split(" ")[0])) {
                log.debug("Authorization header is present ...");
                identity = getIdentityRepository().findByApiToken(authorizationHeader.split(" ")[1]).orElseThrow(AccessDeniedException::new);
            }

            if (annotation != null) {
                if (authCookie.isEmpty() && identity == null) {
                    allowed.set(false);
                } else {
                    Claims claims = (Claims) request.getAttribute(Constants.REQUEST_CLAIMS_FIELD);

                    log.debug("Claims used: {}", claims);

                    if (claims != null) {
                        if (annotation.adminOnly() && !Boolean.parseBoolean(claims.get("administrator", String.class))) {
                            log.debug("Access denied, resource is admin only but user is not admin");
                            allowed.set(false);
                        }
                    } else {
                        if (identity == null || (annotation.adminOnly() && !identity.getAdmin())) {
                            log.debug("Access denied, identity is null or resource was admin only and found identity is no admin (api token)");
                            allowed.set(false);
                        }
                    }
                }
            }
        }

        if (!allowed.get()) {
            log.debug("Access Denied for Request {} {} from {}", request.getMethod(), request.getRequestURI(), request.getRemoteAddr());
            response.sendError(403);
        }

        return allowed.get();
    }
}