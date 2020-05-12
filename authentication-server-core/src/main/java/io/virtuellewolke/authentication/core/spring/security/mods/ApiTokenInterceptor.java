package io.virtuellewolke.authentication.core.spring.security.mods;

import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import io.virtuellewolke.authentication.core.spring.components.LoginSecurity;
import io.virtuellewolke.authentication.core.spring.components.ServiceValidation;
import io.virtuellewolke.authentication.core.spring.helper.SecureContextRequestHelper;
import io.virtuellewolke.authentication.core.spring.helper.ServiceRequestHelper;
import io.virtuellewolke.authentication.core.spring.security.SecureContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Slf4j
@Component
public class ApiTokenInterceptor extends ServiceAwareInterceptor implements AuthyInterceptor {

    private final IdentityRepository identityRepository;
    private final LoginSecurity      loginSecurity;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String API_TOKEN_HEADER     = "X-Api-Token";

    @Autowired
    public ApiTokenInterceptor(IdentityRepository identityRepository, ServiceValidation serviceValidation, LoginSecurity loginSecurity) {
        super(serviceValidation);
        this.identityRepository = identityRepository;
        this.loginSecurity      = loginSecurity;
    }

    @Override
    public boolean process(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!SecureContextRequestHelper.hasSecureContext(request) && loginSecurity.isAllowedToTry(request.getRemoteAddr())) {
            String token = getApiTokenFromHeaders(request);

            if (token != null) {
                Optional<Identity> identity = identityRepository.findByApiToken(token);

                log.trace("Access Requested by Api Token '{}' resulted in User {}", token, identity);

                if (identity.isPresent()) {
                    SecureContext context = SecureContext.builder()
                            .identity(identity.get())
                            .service(ServiceRequestHelper.getService(request))
                            .source(SecureContext.Source.API_TOKEN)
                            .build();

                    SecureContextRequestHelper.setSecureContext(context, request);

                    loginSecurity.resetAttempts(request.getRemoteAddr());
                } else {
                    loginSecurity.recordFailedAttempt(request.getRemoteAddr());
                }
            }
        }

        return true;
    }

    private String getApiTokenFromHeaders(HttpServletRequest request) {
        String token = null;
        if (request.getHeader(AUTHORIZATION_HEADER) != null) {
            token = request.getHeader(AUTHORIZATION_HEADER);

            if (token.startsWith("Bearer ")) {
                token = token.substring("Bearer ".length());
            }
        } else if (request.getHeader(API_TOKEN_HEADER) != null) {
            token = request.getHeader(API_TOKEN_HEADER);
        }

        return token != null ? token.trim() : null;
    }
}