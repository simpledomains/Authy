package io.virtuellewolke.authentication.core.api.service;

import io.virtuellewolke.authentication.core.api.model.UpdateAuthorityRequest;
import io.virtuellewolke.authentication.core.database.entity.Authority;
import io.virtuellewolke.authentication.core.database.repository.AuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AuthorityResourceImpl implements AuthorityResource {

    private final AuthorityRepository authorityRepository;

    @Override
    public ResponseEntity<List<Authority>> listAuthority() {
        return ResponseEntity.ok(authorityRepository.findAll());
    }

    @Override
    public ResponseEntity<Authority> createAuthority(Authority authority) {
        authority.setId(null);
        authorityRepository.save(authority);
        return ResponseEntity.ok(authority);
    }

    @Override
    public ResponseEntity<Authority> updateAuthority(Integer id, UpdateAuthorityRequest request) {
        Authority authority = authorityRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        authorityRepository.save(request.update(authority));
        return ResponseEntity.ok(authority);
    }

    @Override
    public void deleteAuthority(Integer id) {
        authorityRepository.deleteById(id);
    }

    @Override
    public ResponseEntity<Authority> getAuthority(Integer id) {
        Authority authority = authorityRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return ResponseEntity.ok(authority);
    }
}
