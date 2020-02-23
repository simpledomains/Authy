package io.virtuellewolke.authentication.core.impl.service;

import io.virtuellewolke.authentication.core.api.model.UpdateUserRequest;
import io.virtuellewolke.authentication.core.api.service.UserResource;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserResourceImpl implements UserResource {
    @Override
    public ResponseEntity<List<Identity>> listUsers() {
        return null;
    }

    @Override
    public ResponseEntity<Identity> createUser(Identity identity) {
        return null;
    }

    @Override
    public ResponseEntity<Identity> updateUser(Integer id, UpdateUserRequest request) {
        return null;
    }

    @Override
    public void deleteUser(Integer id) {

    }

    @Override
    public ResponseEntity<Identity> getUser(Integer id) {
        return null;
    }
}