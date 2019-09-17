package de.reynok.authentication.core.web.auth;

import de.reynok.authentication.core.Constants;
import de.reynok.authentication.core.database.entity.Identity;
import de.reynok.authentication.core.database.entity.Service;
import de.reynok.authentication.core.database.repository.IdentityRepository;
import de.reynok.authentication.core.cas.ServiceValidation;
import de.reynok.authentication.core.cas.ValidateResponse;
import de.reynok.authentication.core.util.JwtProcessor;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ForwardAuthController {
    private final JwtProcessor       jwtProcessor;
    private final IdentityRepository identityRepository;
    private final ServiceValidation  serviceValidation;

    @Value("${app.domain:}")
    private String baseDomain;

    @GetMapping("/auth")
    public ResponseEntity forwardAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String serviceUrl = getServiceUrlFromRequestHeaders(request);

        log.info("Forward Auth request for {}", serviceUrl);
        log.info("User has Cookie? {}", request.getAttribute(Constants.REQUEST_CLAIMS_FIELD) != null);

        Service service = serviceValidation.isAllowed(serviceUrl);

        if (service == null) {
            response.sendRedirect(baseDomain + "/#/cas/error?service=" + serviceUrl + "&code=" + ValidateResponse.StatusCode.MISSING_SERVICE);
            return ResponseEntity.status(302).build();
        }

        if (request.getAttribute(Constants.REQUEST_CLAIMS_FIELD) != null) {
            Claims claims = (Claims) request.getAttribute(Constants.REQUEST_CLAIMS_FIELD);

            Identity identity = identityRepository.findByUsername(claims.get("sub").toString()).orElseThrow(EntityNotFoundException::new);

            if (service.isIdentityAllowed(identity)) {
                return ResponseEntity.ok().build();
            } else {
                response.sendRedirect(baseDomain + "/#/cas/error?service=" + serviceUrl + "&code=" + ValidateResponse.StatusCode.DENIED);
                return ResponseEntity.status(302).build();
            }
        } else {
            response.sendRedirect(baseDomain + "/#/login?service=" + serviceUrl);
            return ResponseEntity.status(302).build();
        }
    }

    private String getServiceUrlFromRequestHeaders(HttpServletRequest request) {
        String url = "";

        url += request.getHeader("x-forwarded-proto");
        url += "://";
        url += request.getHeader("x-forwarded-host");
        url += request.getHeader("x-forwarded-uri");

        return url;
    }
}
