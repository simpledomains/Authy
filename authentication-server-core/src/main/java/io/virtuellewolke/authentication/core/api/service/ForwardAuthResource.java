package io.virtuellewolke.authentication.core.api.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ForwardAuthResource {
    @Deprecated
    @RequestMapping("/auth")
    ResponseEntity<?> forwardAuth(HttpServletRequest request, HttpServletResponse response) throws IOException;

    @RequestMapping("/forward-auth")
    default ResponseEntity<?> forwardAuthReMapped(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return forwardAuth(request, response);
    }
}
