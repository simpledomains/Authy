package de.reynok.authentication.core.frontend.controller.system;

import de.reynok.authentication.core.backend.configuration.WebRequiresAuthentication;
import de.reynok.authentication.core.backend.database.entity.Authority;
import de.reynok.authentication.core.backend.database.repository.AuthorityRepository;
import de.reynok.authentication.core.backend.database.repository.IdentityRepository;
import de.reynok.authentication.core.frontend.controller.AbstractAuthyController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Api(produces = "application/json", value = "Authorities")
public class AuthorityAdminController extends AbstractAuthyController {

    private final AuthorityRepository authorityRepository;

    public AuthorityAdminController(IdentityRepository identityRepository, AuthorityRepository authorityRepository) {
        super(identityRepository);
        this.authorityRepository = authorityRepository;
    }

    @GetMapping("/admin/authorities")
    @WebRequiresAuthentication(adminOnly = true)
    @ApiOperation(response = Authority.class, responseContainer = "List", value = "Get all authorities")
    public Iterable<Authority> identities() {
        return authorityRepository.findAll();
    }

    @GetMapping("/admin/authority/{id}")
    @WebRequiresAuthentication(adminOnly = true)
    public Authority authority(@PathVariable("id") int id) {
        return authorityRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    @WebRequiresAuthentication(adminOnly = true)
    @PostMapping("/admin/authorities")
    public Authority createAuthority(@RequestBody Authority Authority) {
        Authority.setId(null);

        authorityRepository.save(Authority);

        return Authority;
    }

    @WebRequiresAuthentication(adminOnly = true)
    @DeleteMapping("/admin/authority/{id}")
    public void deleteAuthority(@PathVariable("id") int id) {
        authorityRepository.deleteById(id);
    }

    @WebRequiresAuthentication(adminOnly = true)
    @PatchMapping("/admin/authority/{id}")
    public Authority patchAuthority(@RequestBody Map<String, Object> update, @PathVariable("id") int id) {
        Authority Authority = authority(id);

        Authority.updateFrom(update, true);

        authorityRepository.save(Authority);

        return Authority;
    }
}