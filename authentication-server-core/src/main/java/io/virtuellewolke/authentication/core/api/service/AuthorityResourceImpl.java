package io.virtuellewolke.authentication.core.api.service;

import io.virtuellewolke.authentication.core.api.model.UpdateAuthorityRequest;
import io.virtuellewolke.authentication.core.database.entity.Authority;
import io.virtuellewolke.authentication.core.database.entity.Service;
import io.virtuellewolke.authentication.core.database.repository.AuthorityRepository;
import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import io.virtuellewolke.authentication.core.database.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AuthorityResourceImpl implements AuthorityResource {

    private final AuthorityRepository authorityRepository;
    private final IdentityRepository  identityRepository;
    private final ServiceRepository   serviceRepository;

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
        Authority authority = authorityRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        identityRepository.findAllByAuthoritiesId(id).forEach(identity -> {
            identity.getAuthorities().removeIf(a -> a.getId().equals(id));
            identityRepository.save(identity);
        });

        serviceRepository.findAll().forEach(service -> {
            if (service.getRequiredRoles().contains(authority.getName())) {
                service.getRequiredRoles().removeIf(s -> s.equalsIgnoreCase(authority.getName()));
                if (service.getRequiredRoles().size() == 0 && service.getMode() == Service.ServiceMode.AUTHORIZED) {
                    service.setMode(Service.ServiceMode.PUBLIC);
                }
                serviceRepository.save(service);
            }
        });

        authorityRepository.deleteById(id);
    }

    @Override
    public ResponseEntity<Authority> getAuthority(Integer id) {
        Authority authority = authorityRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return ResponseEntity.ok(authority);
    }
}
