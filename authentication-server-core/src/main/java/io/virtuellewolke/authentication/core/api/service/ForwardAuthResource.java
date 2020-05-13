package io.virtuellewolke.authentication.core.api.service;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequestMapping("/auth")
public interface ForwardAuthResource {
    @Hidden
    @RequestMapping
    ResponseEntity<?> forwardAuth(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
