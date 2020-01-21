package de.reynok.authentication.core.frontend.controller.frontend;

import de.reynok.authentication.core.backend.components.X509Manager;
import de.reynok.authentication.core.backend.configuration.WebRequiresAuthentication;
import de.reynok.authentication.core.backend.database.entity.ClientAuthCert;
import de.reynok.authentication.core.backend.database.entity.Identity;
import de.reynok.authentication.core.backend.database.repository.ClientAuthCertRepository;
import de.reynok.authentication.core.backend.database.repository.IdentityRepository;
import de.reynok.authentication.core.frontend.controller.AbstractAuthyController;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

@RestController
@RequestMapping("/api/x509")
public class X509ClientAuthController extends AbstractAuthyController {

    private final ClientAuthCertRepository certRepository;
    private final X509Manager              x509Manager;

    public X509ClientAuthController(IdentityRepository identityRepository, ClientAuthCertRepository certRepository, X509Manager x509Manager) {
        super(identityRepository);
        this.certRepository = certRepository;
        this.x509Manager = x509Manager;
    }

    @WebRequiresAuthentication
    @RequestMapping(value = "/revoke/{serial}", method = {RequestMethod.POST, RequestMethod.GET})
    public boolean revoke(HttpServletRequest request, @PathVariable("serial") BigInteger serial) {
        x509Manager.revoke(serial, getIdentityFromRequest(request));
        return true;
    }

    @WebRequiresAuthentication(adminOnly = true)
    @RequestMapping(value = "/verify/{serial}", method = {RequestMethod.POST, RequestMethod.GET})
    public boolean verify(@PathVariable("serial") BigInteger serial) {
        return !x509Manager.isRevoked(serial);
    }

    @GetMapping("/my")
    public List<ClientAuthCert> getMyCerts(HttpServletRequest request) {
        Identity identity = getIdentityFromRequest(request);

        return certRepository.findByIdentity(identity);
    }

    @GetMapping("/issue")
    public ResponseEntity<ByteArrayResource> issueNew(HttpServletRequest request) throws IOException {
        Identity identity = getIdentityFromRequest(request);

        byte[] data = x509Manager.issuePfx(identity);

        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + identity.getUsername() + ".pfx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(data.length)
                .body(resource);
    }
}