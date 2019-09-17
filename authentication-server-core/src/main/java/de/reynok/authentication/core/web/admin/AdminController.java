package de.reynok.authentication.core.web.admin;

import de.reynok.authentication.core.database.entity.Identity;
import de.reynok.authentication.core.database.repository.IdentityRepository;
import de.reynok.authentication.core.security.RequiresAuthentication;
import de.reynok.authentication.core.web.RequestProcessedController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController extends RequestProcessedController {
    @Autowired
    public AdminController(IdentityRepository identityRepository) {
        super(identityRepository);
    }


    @GetMapping("/api/admin/users")
    @RequiresAuthentication(adminOnly = true)
    public Iterable<Identity> findAllIdentities() {
        return identityRepository.findAll();
    }
}