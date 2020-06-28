package io.virtuellewolke.authentication.core.api.service;

import io.virtuellewolke.authentication.core.cas.StatusCode;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.entity.Service;
import io.virtuellewolke.authentication.core.spring.components.ServiceValidation;
import io.virtuellewolke.authentication.core.spring.configuration.ForwardAuthConfiguration;
import io.virtuellewolke.authentication.core.spring.helper.SecureContextRequestHelper;
import io.virtuellewolke.authentication.core.spring.security.SecureContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class ForwardAuthResourceImpl implements ForwardAuthResource {

    private final ForwardAuthConfiguration configuration;
    private final ServiceValidation        serviceValidation;

    @Override
    public ResponseEntity<?> forwardAuth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SecureContext secureContext = SecureContextRequestHelper.getSecureContext(request);

        String serviceUrl = getServiceUrlFromRequestHeaders(request);

        if (secureContext != null) {
            Service  service  = serviceValidation.getRegisteredServiceFor(serviceUrl);
            Identity identity = secureContext.getIdentity();

            if (service == null) {
                response.sendRedirect(configuration.getBaseDomain(request) + "/#/error?service=" + serviceUrl + "&code=" + StatusCode.MISSING_SERVICE);
                return ResponseEntity.status(302).build();
            }

            if (service.isIdentityAllowed(identity)) {
                return ResponseEntity
                        .ok()
                        .header("X-Auth-User", identity.getUsername())
                        .header("X-Auth-Display-Name", identity.getDisplayName())
                        .header("X-Auth-E-Mail", identity.getEmail())
                        .header("X-Auth-Admin", identity.getAdmin().toString())
                        .build();
            } else {
                response.sendRedirect(configuration.getBaseDomain(request) + "/#/error?service=" + serviceUrl + "&code=" + StatusCode.DENIED);
                return ResponseEntity.status(302).build();
            }
        } else {
            response.sendRedirect(configuration.getBaseDomain(request) + "/#/login?service=" + serviceUrl);
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
