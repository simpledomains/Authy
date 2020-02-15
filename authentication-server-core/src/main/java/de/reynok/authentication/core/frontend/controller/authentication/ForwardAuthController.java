package de.reynok.authentication.core.frontend.controller.authentication;

import de.reynok.authentication.core.Constants;
import de.reynok.authentication.core.backend.database.entity.Identity;
import de.reynok.authentication.core.backend.database.entity.Service;
import de.reynok.authentication.core.backend.modules.cas.CasStatusCode;
import de.reynok.authentication.core.backend.database.repository.IdentityRepository;
import de.reynok.authentication.core.backend.modules.cas.ServiceValidation;
import de.reynok.authentication.core.backend.components.JwtProcessor;
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
            response.sendRedirect(baseDomain + "/#/cas/error?service=" + serviceUrl + "&code=" + CasStatusCode.MISSING_SERVICE);
            return ResponseEntity.status(302).build();
        }

        if (service.getMode() == Service.ServiceMode.ANONYMOUS) {
            return ResponseEntity.ok().build(); // indicates a anonymous route, no auth is required.
        }

        if (request.getAttribute(Constants.REQUEST_CLAIMS_FIELD) != null) {
            Claims claims = (Claims) request.getAttribute(Constants.REQUEST_CLAIMS_FIELD);

            Identity identity = identityRepository.findByUsername(claims.get("sub").toString()).orElse(null);

            if (service.isIdentityAllowed(identity)) {
                return ResponseEntity.ok().build();
            } else {
                response.sendRedirect(baseDomain + "/#/cas/error?service=" + serviceUrl + "&code=" + CasStatusCode.DENIED);
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
