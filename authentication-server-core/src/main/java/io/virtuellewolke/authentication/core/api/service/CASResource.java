package io.virtuellewolke.authentication.core.api.service;

import io.virtuellewolke.authentication.core.api.model.LoginRequest;
import io.virtuellewolke.authentication.core.api.model.LoginResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.virtuellewolke.authentication.core.api.model.cas.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

@Tag(name = "Authy - CAS Endpoint")
@RequestMapping("/cas")
public interface CASResource {

    @RequestMapping(value = {
            "/validate",
            "/p3/serviceValidate",
            "/serviceValidate",
            "/proxyValidate",
            "/p3/proxyValidate"
    }, produces = {
            "application/xml",
            "application/json"
    }, method = RequestMethod.GET)
    ResponseEntity<AuthResponse> validate(HttpServletRequest request, @RequestParam("ticket") String ticket, @RequestParam("service") String service);


    @CrossOrigin
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json")
    ResponseEntity<LoginResponse> login(HttpServletRequest req, HttpServletResponse response, @RequestBody LoginRequest body, @RequestParam("service") String serviceUrl);

    @CrossOrigin
    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = "application/json")
    default ResponseEntity<LoginResponse> loginPage(HttpServletRequest request, @RequestParam("service") String service) {
        return ResponseEntity.ok().location(URI.create("/#/login?service=" + service)).build();
    }

    @CrossOrigin
    @RequestMapping(value = "/logout", method = RequestMethod.DELETE, produces = "application/json")
    ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response);
}