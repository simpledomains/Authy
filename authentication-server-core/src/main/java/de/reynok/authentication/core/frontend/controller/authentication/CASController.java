package de.reynok.authentication.core.frontend.controller.authentication;

import de.reynok.authentication.core.Constants;
import de.reynok.authentication.core.backend.configuration.ConfigurationContext;
import de.reynok.authentication.core.backend.modules.cas.CasStatusCode;
import de.reynok.authentication.core.backend.modules.cas.TicketHandler;
import de.reynok.authentication.core.backend.modules.cas.TicketType;
import de.reynok.authentication.core.frontend.api.CasJsonResponse;
import de.reynok.authentication.core.frontend.api.CasXmlResponse;
import de.reynok.authentication.core.frontend.api.LoginRequest;
import de.reynok.authentication.core.frontend.api.LoginResponse;
import de.reynok.authentication.core.frontend.controller.AbstractAuthyController;
import de.reynok.authentication.core.shared.exceptions.ServiceException;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import io.virtuellewolke.authentication.core.spring.components.JwtProcessor;
import io.virtuellewolke.authentication.core.spring.components.ServiceValidation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RestController
public class CASController extends AbstractAuthyController {
    private final TicketHandler        ticketHandler;
    private final JwtProcessor         jwtProcessor;
    private final IdentityRepository   identityRepository;
    private final ServiceValidation    serviceValidation;
    private final ConfigurationContext configurationContext;

    @Autowired
    public CASController(TicketHandler ticketHandler, JwtProcessor jwtProcessor, IdentityRepository identityRepository, ServiceValidation serviceValidation, ConfigurationContext configuration) {
        super(identityRepository);
        this.jwtProcessor         = jwtProcessor;
        this.identityRepository   = identityRepository;
        this.serviceValidation    = serviceValidation;
        this.ticketHandler        = ticketHandler;
        this.configurationContext = configuration;
    }

    @GetMapping("/cas/logout")
    public void logout(HttpServletResponse response, @RequestParam(required = false, value = "service") String serviceUrl) throws IOException {
        Cookie cookie = new Cookie(Constants.COOKIE_NAME, "");
        cookie.setMaxAge(1);
        cookie.setPath(configurationContext.getCookiePath());
        cookie.setComment(configurationContext.getCookieComment());
        cookie.setDomain(configurationContext.getCookieDomain());

        response.addCookie(cookie);

        if (serviceUrl == null || StringUtils.isBlank(serviceUrl)) {
            response.sendRedirect(configurationContext.getSystemDomain() + "/");
        } else {
            response.sendRedirect(serviceUrl);
        }

        response.setStatus(302);
    }

    @CrossOrigin
    @GetMapping("/cas/login")
    public void requestLogin(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "service") String serviceUrl) throws IOException {
    }

    @CrossOrigin
    @PostMapping("/cas/login")
    public ResponseEntity<LoginResponse> processAuthentication(HttpServletResponse response, @RequestBody LoginRequest body, @RequestParam("service") String serviceUrl) throws ServiceException {
        return null;
    }

    @GetMapping(value = {"/cas/validate", "/cas/p3/serviceValidate", "/cas/serviceValidate", "/cas/proxyValidate", "/cas/p3/proxyValidate"}, produces = "application/json")
    public ResponseEntity validateAsJson(HttpServletResponse response, @RequestParam("ticket") String ticket, @RequestParam("service") String service) {
        CasJsonResponse jsonResponse = new CasJsonResponse();

        Identity identity = ticketHandler.getTicketData(ticket, service);

        if (identity != null) {
            jsonResponse.success(identity);
        } else {
            jsonResponse.error(CasStatusCode.INVALID_TICKET, "Ticket " + ticket + " not recognized.");
        }

        return jsonResponse.toResponse();
    }

    /**
     * ATTENTION: /proxyValidate is ONLY validating the normal service tickets, proxyTickets are NOT yet supported!
     */
    @GetMapping(value = {"/cas/validate", "/cas/p3/serviceValidate", "/cas/serviceValidate", "/cas/proxyValidate", "/cas/p3/proxyValidate"})
    public ResponseEntity validate(HttpServletResponse response, @RequestParam("ticket") String ticket, @RequestParam("service") String service) {
        CasXmlResponse xmlResponse = new CasXmlResponse();

        Identity identity = ticketHandler.getTicketData(ticket, service);

        if (identity != null) {
            xmlResponse.isSuccess(identity);
        } else {
            xmlResponse.isFailure(CasStatusCode.INVALID_TICKET, "Ticket " + ticket + " not recognized.");
        }

        response.setContentType("application/xml");
        return ResponseEntity.ok(xmlResponse.toString());
    }


    private String getRDUrl(String type, HttpServletRequest request, CasStatusCode statusCode) {
        return getRDUrl(type, request) + "&code=" + statusCode;
    }

    private String getRDUrl(String type, HttpServletRequest request) {
        return configurationContext.getSystemDomain() + "/#/cas/" + type + "?service=" + request.getParameter("service");
    }

    private String getRedirectLogin(String service, Identity identity) {
        String serviceUrl = service;

        if (service.contains("?")) {
            service += "&";
        } else {
            service += "?";
        }
        service += "ticket=" + ticketHandler.generateTicketFor(TicketType.ST, serviceUrl, identity);

        return service;
    }
}