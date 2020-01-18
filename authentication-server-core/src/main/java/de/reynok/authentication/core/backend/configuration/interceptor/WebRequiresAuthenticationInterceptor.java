package de.reynok.authentication.core.backend.configuration.interceptor;


import de.reynok.authentication.core.Constants;
import de.reynok.authentication.core.backend.components.JwtProcessor;
import de.reynok.authentication.core.backend.configuration.WebRequiresAuthentication;
import de.reynok.authentication.core.backend.database.entity.Identity;
import de.reynok.authentication.core.backend.database.repository.IdentityRepository;
import de.reynok.authentication.core.shared.exceptions.AccessDeniedException;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class WebRequiresAuthenticationInterceptor extends AuthyWebInterceptor {

    public WebRequiresAuthenticationInterceptor(JwtProcessor jwtProcessor, IdentityRepository identityRepository) {
        super(jwtProcessor, identityRepository);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        AtomicBoolean allowed = new AtomicBoolean(true);

        Identity identity = null;

        String authorizationHeader = request.getHeader("Authorization");

        if (handler instanceof HandlerMethod) {
            WebRequiresAuthentication annotation = ((HandlerMethod) handler).getMethod().getAnnotation(WebRequiresAuthentication.class);

            if (authorizationHeader != null && "Bearer".equals(authorizationHeader.split(" ")[0])) {
                log.debug("Authorization header is present ...");
                identity = getIdentityRepository().findByApiToken(authorizationHeader.split(" ")[1]).orElseThrow(AccessDeniedException::new);
            }

            Object claimContent = request.getAttribute(Constants.REQUEST_CLAIMS_FIELD);

            if (annotation != null) {
                if (identity == null && claimContent == null) {
                    allowed.set(false);
                } else {
                    Claims claims = (Claims) claimContent;

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