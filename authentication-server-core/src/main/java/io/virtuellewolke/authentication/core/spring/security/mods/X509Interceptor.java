package io.virtuellewolke.authentication.core.spring.security.mods;

import io.jsonwebtoken.lang.Assert;
import io.virtuellewolke.authentication.core.database.entity.ClientAuthCert;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import io.virtuellewolke.authentication.core.spring.components.ServiceValidation;
import io.virtuellewolke.authentication.core.spring.components.X509Manager;
import io.virtuellewolke.authentication.core.spring.configuration.X509ManagerConfiguration;
import io.virtuellewolke.authentication.core.spring.helper.SecureContextRequestHelper;
import io.virtuellewolke.authentication.core.spring.security.SecureContext;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import javax.persistence.EntityNotFoundException;
import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Slf4j
@Component
public class X509Interceptor extends ServiceAwareInterceptor implements AuthyInterceptor {

    private final X509ManagerConfiguration config;
    private final X509Manager              x509Manager;
    private final IdentityRepository       identityRepository;

    public X509Interceptor(ServiceValidation serviceValidation, X509Manager manager, X509ManagerConfiguration config, IdentityRepository identityRepository) {
        super(serviceValidation);
        this.x509Manager        = manager;
        this.config             = config;
        this.identityRepository = identityRepository;
    }

    @Override
    public boolean process(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String headerContent = request.getHeader(config.getHttpHeaderName());

        if (headerContent != null) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");

            String decoded = UriUtils.decode(headerContent, Charset.defaultCharset());
            decoded = decoded.replaceAll("-----(.*)-----", "");
            decoded = decoded.replaceAll("\n", "");
            decoded = decoded.replaceAll("%3D", "=");
            decoded = decoded.replaceAll("%2F", "/");

            String[] certificateList = decoded.split(",");

            if (certificateList.length > 1) {
                log.warn("The Client from {} supplied more then 1 client certificate ({})!", request.getRemoteAddr(), certificateList.length);
            }

            log.trace("X509Certificate received in {} was decoded to:\n{}", config.getHttpHeaderName(), decoded);

            for (String cert : certificateList) {
                try (InputStream bis = new ByteArrayInputStream(Base64.decode(cert))) {
                    X509Certificate certificate = (X509Certificate) factory.generateCertificate(bis);
                    handleAuthenticationWithKey(request, certificate);
                }
            }
        }

        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        if (null != certs && certs.length > 0) {
            X509Certificate cert = certs[0];
            handleAuthenticationWithKey(request, cert);
        }

        return true;
    }

    private void handleAuthenticationWithKey(HttpServletRequest request, X509Certificate certificate) {
        if (validate(certificate) && !SecureContextRequestHelper.hasSecureContext(request)) {
            X500Principal principal = certificate.getSubjectX500Principal();
            X500Name      x500Name  = new X500Name(principal.getName());
            RDN           cn        = x500Name.getRDNs(BCStyle.CN)[0];

            String username = IETFUtils.valueToString(cn.getFirst().getValue());

            log.info("Username found in certificate: {}", username);

            Identity identity = identityRepository.findByUsername(username).orElseThrow(EntityNotFoundException::new);

            SecureContext secureContext = SecureContext.builder()
                    .identity(identity)
                    .source(SecureContext.Source.X509)
                    .build();

            SecureContextRequestHelper.setSecureContext(secureContext, request);
        }
    }

    private boolean validate(X509Certificate certificate) {
        X509Certificate ca = x509Manager.getCaCertificate();

        if (!certificate.equals(ca)) {
            try {
                Assert.notNull(ca, "CA cannot be null");
                Assert.notNull(certificate, "Certificate cannot be null");

                certificate.verify(ca.getPublicKey());
                certificate.checkValidity();
                ca.checkValidity();

                ClientAuthCert dbCert = x509Manager.getCertificateFor(certificate.getSerialNumber());

                if (x509Manager.isRevoked(dbCert)) {
                    log.info("X509 Certificate {} is revoked.", certificate.getSerialNumber());
                    throw new RuntimeException("Certificate " + certificate.getSerialNumber().toString() + " revoked or unknown.");
                } else {
                    x509Manager.markAsUsed(dbCert);
                }

                return true;
            } catch (Exception e) {
                log.debug(e.getMessage(), e);
                return false;
            }
        }

        return false;
    }
}