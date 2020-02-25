package io.virtuellewolke.authentication.core.api.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.spring.security.annotations.AuthorizedResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * This Resource is a special endpoint to handle special cases for the frontend.
 */
@RequestMapping("/api/session")
@Tag(name = "Authy - Identity Management")
public interface IdentityManagementResource {
    @AuthorizedResource
    @GetMapping("/me")
    public ResponseEntity<Identity> myIdentity(HttpServletRequest request);
}