package io.virtuellewolke.authentication.core.spring.security.mods;

import io.virtuellewolke.authentication.core.api.Constants;
import io.virtuellewolke.authentication.core.exceptions.SecurityTokenInvalidException;
import io.jsonwebtoken.Claims;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import io.virtuellewolke.authentication.core.spring.components.JwtProcessor;
import io.virtuellewolke.authentication.core.spring.components.ServiceValidation;
import io.virtuellewolke.authentication.core.spring.helper.SecureContextRequestHelper;
import io.virtuellewolke.authentication.core.spring.security.SecureContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@Component
public class CookieAuthenticationInterceptor extends ServiceAwareInterceptor implements AuthyInterceptor {

    private final IdentityRepository identityRepository;
    private final JwtProcessor       jwtProcessor;


    public CookieAuthenticationInterceptor(IdentityRepository identityRepository, ServiceValidation serviceValidation, JwtProcessor jwtProcessor) {
        super(serviceValidation);
        this.identityRepository = identityRepository;
        this.jwtProcessor       = jwtProcessor;
    }

    @Override
    public boolean process(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!SecureContextRequestHelper.hasSecureContext(request)) {
            Cookie[] cookies = request.getCookies() != null ? request.getCookies() : new Cookie[0];

            Optional<Cookie> authCookie = Stream.of(cookies).filter(cookie -> cookie.getName().equals(Constants.COOKIE_NAME)).findFirst();

            authCookie.ifPresent(cookie -> {
                String cookieData = cookie.getValue();
                try {
                    Claims claims = jwtProcessor.validateToken(cookieData);

                    Object uidStr = claims.get("uid");

                    if (uidStr != null) {
                        Integer uid = Integer.parseInt(uidStr.toString());

                        Optional<Identity> identity = identityRepository.findById(uid);

                        if (identity.isPresent()) {
                            SecureContext ctx = SecureContext.builder()
                                    .identity(identity.get())
                                    .source(SecureContext.Source.COOKIE)
                                    .build();

                            SecureContextRequestHelper.setSecureContext(ctx, request);
                        }
                    }
                } catch (NullPointerException | SecurityTokenInvalidException ignored) {
                }
            });
        }

        return true;
    }
}