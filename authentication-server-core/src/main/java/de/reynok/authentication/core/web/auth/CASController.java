package de.reynok.authentication.core.web.auth;

import de.reynok.authentication.core.Constants;
import de.reynok.authentication.core.database.entity.Identity;
import de.reynok.authentication.core.database.entity.Service;
import de.reynok.authentication.core.database.repository.IdentityRepository;
import de.reynok.authentication.core.cas.ServiceValidation;
import de.reynok.authentication.core.cas.TicketHandler;
import de.reynok.authentication.core.cas.TicketType;
import de.reynok.authentication.core.cas.ValidateResponse;
import de.reynok.authentication.core.util.JwtProcessor;
import de.reynok.authentication.core.web.api.LoginFailedResponse;
import de.reynok.authentication.core.web.api.LoginRequest;
import de.reynok.authentication.core.web.api.LoginResponse;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CASController {
    private final TicketHandler ticketHandler;
    private final JwtProcessor jwtProcessor;
    private final IdentityRepository identityRepository;
    private final ServiceValidation serviceValidation;

    @Value("${app.domain:}")
    private String baseDomain;
    @Value("${app.cas.cookie.domain:#{null}}")
    private String cookieDomain;

    @GetMapping("/cas/logout")
    public ResponseEntity logout(HttpServletResponse response) throws IOException {
        Cookie cookie = new Cookie("CASTGC", "");
        cookie.setMaxAge(1);

        response.addCookie(cookie);
        response.sendRedirect(baseDomain + "/");
        return ResponseEntity.status(302).build();
    }

    @GetMapping("/cas/login")
    public ResponseEntity requestLogin(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "service") String serviceUrl) throws IOException {
        Service service = serviceValidation.isAllowed(serviceUrl);

        if (service == null) {
            response.sendRedirect(baseDomain + "/#/cas/error?service=" + request.getParameter("service") + "&code=" + ValidateResponse.StatusCode.MISSING_SERVICE);
            return ResponseEntity.status(302).build();
        }

        if (request.getAttribute(Constants.REQUEST_CLAIMS_FIELD) != null) {
            Claims claims = (Claims) request.getAttribute(Constants.REQUEST_CLAIMS_FIELD);

            Identity identity = identityRepository.findByUsername(claims.get("sub").toString()).orElseThrow(EntityNotFoundException::new);

            if (service.isIdentityAllowed(identity)) {
                String redirectUrl = getRedirectLogin(serviceUrl, identity);

                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setLocation(redirectUrl);

                response.sendRedirect(redirectUrl);
                return ResponseEntity.status(302).build();
            } else {
                response.sendRedirect(baseDomain + "/#/cas/error?service=" + request.getParameter("service") + "&code=" + ValidateResponse.StatusCode.DENIED);
                return ResponseEntity.status(302).build();
            }
        } else {
            response.sendRedirect(baseDomain + "/#/cas/login?service=" + request.getParameter("service"));
            return ResponseEntity.status(302).build();
        }
    }

    @PostMapping("/cas/login")
    public ResponseEntity processAuthentication(HttpServletResponse response, @RequestBody LoginRequest body, @RequestParam("service") String serviceUrl) {
        Service service = serviceValidation.isAllowed(serviceUrl);

        if (service == null) {
            return ResponseEntity.status(400).body(new LoginFailedResponse("No Service found for " + serviceUrl));
        }

        Optional<Identity> optionalIdentity = identityRepository.findByUsername(body.getUsername());

        if (optionalIdentity.isPresent()) {
            Identity identity = optionalIdentity.get();

            if (identity.checkPassword(body.getPassword(), body.getSecurityPassword())) {
                Cookie cookie = new Cookie(Constants.COOKIE_NAME, jwtProcessor.getJwtTokenFor(identity));
                cookie.setMaxAge(60 * 60 * 12);
                cookie.setPath("/");
                cookie.setComment("CAS Auth Token");

                if (cookieDomain != null) {
                    cookie.setDomain(cookieDomain);
                }

                response.addCookie(cookie);

                LoginResponse loginResponse = new LoginResponse();

                if (body.getCas()) {
                    loginResponse.setLocation(serviceUrl);
                } else {
                    loginResponse.setLocation(getRedirectLogin(serviceUrl, identity)); // issues a new ticket
                }

                return ResponseEntity.ok(loginResponse);
            }
        }

        return ResponseEntity.status(403).build();
    }

    @GetMapping(value = {"/cas/validate", "/cas/p3/serviceValidate", "/cas/serviceValidate"})
    public ResponseEntity validate(HttpServletResponse response, @RequestParam("ticket") String ticket, @RequestParam("service") String service) {
        ValidateResponse validateResponse = new ValidateResponse();

        Identity identity = ticketHandler.getTicketData(ticket, service);

        if(identity != null) {
            validateResponse.isSuccess(identity);
        } else {
            validateResponse.isFailure(ValidateResponse.StatusCode.INVALID_TICKET, "Ticket " + ticket + " not recognized.");
        }

        response.setContentType("application/xml");
        return ResponseEntity.ok(validateResponse.toString());
    }

    private String getRedirectLogin(String service, Identity identity) {
        String serviceUrl = service;

        if(service.contains("?")) service += "&"; else service += "?";
        service += "ticket=" + ticketHandler.generateTicketFor(TicketType.ST, serviceUrl, identity);

        return service;
    }
}