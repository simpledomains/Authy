package io.virtuellewolke.authentication.core.api.service;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.virtuellewolke.authentication.core.api.model.UpdateUserRequest;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.spring.security.annotations.AdminResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@RequestMapping(value = "/api", produces = "application/json")
@Tag(name = "Authy - User Resource")
public interface UserResource {
    @AdminResource
    @RequestMapping(path = "/users", method = RequestMethod.GET)
    ResponseEntity<List<Identity>> listUsers();

    @AdminResource
    @RequestMapping(path = "/users", method = RequestMethod.POST)
    ResponseEntity<Identity> createUser(@RequestBody Identity identity);

    @AdminResource
    @RequestMapping(path = "/user/{id}", method = RequestMethod.PATCH)
    ResponseEntity<Identity> updateUser(@PathVariable("id") Integer id, @RequestBody UpdateUserRequest request);

    @AdminResource
    @RequestMapping(path = "/user/{id}", method = RequestMethod.DELETE)
    void deleteUser(@PathVariable("id") Integer id);

    @AdminResource
    @RequestMapping(path = "/user/{id}", method = RequestMethod.GET)
    ResponseEntity<Identity> getUser(@PathVariable("id") Integer id);
}