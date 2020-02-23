package io.virtuellewolke.authentication.core.api.service;

import de.reynok.authentication.core.frontend.api.LoginRequest;
import de.reynok.authentication.core.frontend.api.LoginResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.virtuellewolke.authentication.core.api.model.cas.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
            "application/json",
            "application/xml"
    }, method = RequestMethod.GET)
    ResponseEntity<AuthResponse> validate(HttpServletRequest request, @RequestParam("ticket") String ticket, @RequestParam("service") String service);


    @CrossOrigin
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    ResponseEntity<LoginResponse> login(HttpServletResponse response, @RequestBody LoginRequest body, @RequestParam("service") String serviceUrl);
}