package io.virtuellewolke.authentication.core.api.service;

import de.reynok.authentication.core.shared.exceptions.ServiceException;
import io.virtuellewolke.authentication.core.database.entity.ClientAuthCert;
import io.virtuellewolke.authentication.core.database.repository.ClientAuthCertRepository;
import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import io.virtuellewolke.authentication.core.spring.components.X509Manager;
import io.virtuellewolke.authentication.core.spring.helper.SecureContextRequestHelper;
import io.virtuellewolke.authentication.core.spring.security.SecureContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class X509ResourceImpl implements X509Resource {

    private final ClientAuthCertRepository certRepository;
    private final IdentityRepository       identityRepository;
    private final X509Manager              manager;

    @Override
    public ResponseEntity<List<ClientAuthCert>> listCertificates(HttpServletRequest request, Integer id) {
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<ByteArrayResource> issueCertificate(HttpServletRequest request, Integer id, String deviceName) {
        return null;
    }

    @Override
    public ResponseEntity<Void> revokeCertificate(HttpServletRequest request, Integer id, BigInteger serial) {
        return null;
    }

    @Override
    public ResponseEntity<List<ClientAuthCert>> listMyCertificates(HttpServletRequest request) {
        SecureContext context = SecureContextRequestHelper.getSecureContext(request);

        if (context != null && context.getIdentity() != null) {
            return ResponseEntity.ok(certRepository.findByIdentity(context.getIdentity()));
        }

        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<ByteArrayResource> issueMyCertificate(HttpServletRequest request, String deviceName) {
        SecureContext context = SecureContextRequestHelper.getSecureContext(request);

        if (context != null && context.getIdentity() != null) {
            try {
                byte[] cert = manager.issuePfx(context.getIdentity(), deviceName);

                ByteArrayResource resource = new ByteArrayResource(cert);

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + context.getIdentity().getUsername() + ".pfx")
                        .contentType(MediaType.valueOf("application/x-pkcs12"))
                        .contentLength(cert.length)
                        .body(resource);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @Override
    public ResponseEntity<Void> revokeMyCertificate(HttpServletRequest request, BigInteger serial) {
        SecureContext context = SecureContextRequestHelper.getSecureContext(request);

        if (context != null && context.getIdentity() != null) {
            manager.revoke(serial, context.getIdentity());
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Map<String, String>> handleServiceException(ServiceException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
