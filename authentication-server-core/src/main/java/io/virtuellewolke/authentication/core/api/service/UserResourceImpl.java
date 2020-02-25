package io.virtuellewolke.authentication.core.api.service;

import io.virtuellewolke.authentication.core.api.model.UpdateUserRequest;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserResourceImpl implements UserResource {

    private final IdentityRepository identityRepository;

    @Override
    public ResponseEntity<List<Identity>> listUsers() {
        return ResponseEntity.ok(identityRepository.findAll());
    }

    @Override
    public ResponseEntity<Identity> createUser(Identity identity) {
        identity.setId(null);
        return ResponseEntity.ok(identityRepository.save(identity));
    }

    @Override
    public ResponseEntity<Identity> updateUser(Integer id, UpdateUserRequest request) {
        Identity identity = identityRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        identityRepository.save(request.update(identity));
        return ResponseEntity.ok(identity);
    }

    @Override
    public void deleteUser(Integer id) {
        identityRepository.deleteById(id);
    }

    @Override
    public ResponseEntity<Identity> getUser(Integer id) {
        return ResponseEntity.ok(identityRepository.findById(id).orElseThrow(EntityNotFoundException::new));
    }
}