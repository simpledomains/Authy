package io.virtuellewolke.authentication.core.api.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.virtuellewolke.authentication.core.api.model.UpdateAuthorityRequest;
import io.virtuellewolke.authentication.core.database.entity.Authority;
import io.virtuellewolke.authentication.core.spring.security.annotations.AdminResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@RequestMapping(value = "/api", produces = "application/json")
@Tag(name = "Authy - Authority Resource")
public interface AuthorityResource {
    @AdminResource
    @RequestMapping(path = "/authorities", method = RequestMethod.GET)
    ResponseEntity<List<Authority>> listAuthority();

    @AdminResource
    @RequestMapping(path = "/authorities", method = RequestMethod.POST)
    ResponseEntity<Authority> createAuthority(@RequestBody Authority identity);

    @AdminResource
    @RequestMapping(path = "/authority/{id}", method = RequestMethod.PATCH)
    ResponseEntity<Authority> updateAuthority(@PathVariable("id") Integer id, @RequestBody UpdateAuthorityRequest request);

    @AdminResource
    @RequestMapping(path = "/authority/{id}", method = RequestMethod.DELETE)
    void deleteAuthority(@PathVariable("id") Integer id);

    @AdminResource
    @RequestMapping(path = "/authority/{id}", method = RequestMethod.GET)
    ResponseEntity<Authority> getAuthority(@PathVariable("id") Integer id);
}