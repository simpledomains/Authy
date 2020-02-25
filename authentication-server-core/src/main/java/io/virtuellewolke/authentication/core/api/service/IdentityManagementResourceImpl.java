package io.virtuellewolke.authentication.core.api.service;

import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.repository.ClientAuthCertRepository;
import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import io.virtuellewolke.authentication.core.spring.helper.SecureContextRequestHelper;
import io.virtuellewolke.authentication.core.spring.security.SecureContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequiredArgsConstructor
public class IdentityManagementResourceImpl implements IdentityManagementResource {

    private final IdentityRepository       identityRepository;
    private final ClientAuthCertRepository certRepository;

    @Override
    public ResponseEntity<Identity> myIdentity(HttpServletRequest request) {
        SecureContext context = SecureContextRequestHelper.getSecureContext(request);
        if (context != null && context.getIdentity() != null) {
            return ResponseEntity.ok(context.getIdentity());
        }
        return ResponseEntity.notFound().build();
    }
}