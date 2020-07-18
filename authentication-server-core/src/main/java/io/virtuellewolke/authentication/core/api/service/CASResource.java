package io.virtuellewolke.authentication.core.api.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.virtuellewolke.authentication.core.api.model.LoginRequest;
import io.virtuellewolke.authentication.core.api.model.LoginResponse;
import io.virtuellewolke.authentication.core.api.model.cas.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

@Tag(name = "Authy - CAS Endpoint")
@RequestMapping("/")
public interface CASResource {

    @RequestMapping(value = {
            "/cas/validate",
            "/cas/p3/serviceValidate",
            "/cas/serviceValidate",
            "/cas/proxyValidate",
            "/cas/p3/proxyValidate"
    }, produces = {
            "application/xml",
            "application/json"
    }, method = RequestMethod.GET)
    ResponseEntity<AuthResponse> validate(HttpServletRequest request, @RequestParam("ticket") String ticket, @RequestParam("service") String service);


    @CrossOrigin
    @RequestMapping(value = {"/cas/login", "/authenticate"}, method = RequestMethod.POST, produces = "application/json")
    ResponseEntity<LoginResponse> login(HttpServletRequest req, HttpServletResponse response, @RequestBody LoginRequest body, @RequestParam("service") String serviceUrl);

    @CrossOrigin
    @RequestMapping(value = "/cas/login", method = RequestMethod.GET, produces = "application/json")
    default ResponseEntity<LoginResponse> loginPage(HttpServletRequest request, @RequestParam("service") String service) {
        return ResponseEntity.ok().location(URI.create("/#/login?service=" + service)).build();
    }

    @CrossOrigin
    @RequestMapping(value = "/cas/logout", method = {RequestMethod.DELETE}, produces = "application/json")
    ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) throws IOException;

    @CrossOrigin
    @RequestMapping(value = "/cas/logout", method = {RequestMethod.GET}, produces = "application/json")
    ResponseEntity<Void> getLogoutPage(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "service", required = false) String service) throws IOException;
}