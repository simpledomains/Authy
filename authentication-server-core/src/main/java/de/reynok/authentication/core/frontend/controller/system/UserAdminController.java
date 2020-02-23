package de.reynok.authentication.core.frontend.controller.system;

import de.reynok.authentication.core.backend.configuration.WebRequiresAuthentication;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.repository.AuthorityRepository;
import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import de.reynok.authentication.core.frontend.controller.AbstractAuthyController;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class UserAdminController extends AbstractAuthyController {

    private final IdentityRepository  identityRepository;
    private final AuthorityRepository authorityRepository;

    public UserAdminController(IdentityRepository identityRepository, AuthorityRepository authorityRepository) {
        super(identityRepository);
        this.identityRepository  = identityRepository;
        this.authorityRepository = authorityRepository;
    }

    @WebRequiresAuthentication(adminOnly = true)
    @GetMapping("/api/admin/users")
    public List<Identity> identities() {
        return identityRepository.findAll();
    }

    @WebRequiresAuthentication(adminOnly = true)
    @GetMapping("/api/admin/user/{id}")
    public Identity identity(@PathVariable("id") int id) {
        return identityRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }

    @WebRequiresAuthentication(adminOnly = true)
    @PostMapping("/api/admin/users")
    public Identity createIdentity(@RequestBody Identity identity) {
        identity.setId(null);

        identityRepository.save(identity);

        return identity;
    }

    @WebRequiresAuthentication(adminOnly = true)
    @DeleteMapping("/api/admin/user/{id}")
    public void deleteIdentity(@PathVariable("id") int id) {
        identityRepository.deleteById(id);
    }

    @WebRequiresAuthentication(adminOnly = true)
    @PatchMapping("/api/admin/user/{id}")
    public Identity patchIdentity(@RequestBody Map<String, Object> update, @PathVariable("id") int id) {
        Identity identity = identity(id);

        identity.updateFrom(update, true);

        if (update.containsKey("authorities")) {
            @SuppressWarnings("unchecked") List<Integer> authorities = (List) update.get("authorities");
            identity.setAuthorities(new ArrayList<>());
            authorities.forEach(roleId -> authorityRepository.findById(roleId).ifPresent(role -> identity.getAuthorities().add(role)));
        }

        identityRepository.save(identity);

        return identity;
    }
}