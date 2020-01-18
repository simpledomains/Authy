package de.reynok.authentication.core.backend.configuration.interceptor;

import de.reynok.authentication.core.Constants;
import de.reynok.authentication.core.backend.components.JwtProcessor;
import de.reynok.authentication.core.backend.database.repository.IdentityRepository;
import de.reynok.authentication.core.shared.exceptions.SecurityTokenInvalidException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriUtils;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Slf4j
@Component
public class X509CertificateAuthenticatorInterceptor extends AuthyWebInterceptor {

    @Value("${server.ssl.client-auth-ca-cert:ca.pem}")
    private File caCertLocation;

    public X509CertificateAuthenticatorInterceptor(JwtProcessor jwtProcessor, IdentityRepository identityRepository) {
        super(jwtProcessor, identityRepository);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String certHeaderFromProxy = request.getHeader("X-SSL-Cert");

        if (certHeaderFromProxy != null) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");

            String decoded = UriUtils.decode(certHeaderFromProxy, Charset.defaultCharset());
            decoded = decoded.replaceAll("-----(.*)-----", "");
            decoded = decoded.replaceAll("\n", "");

            try (InputStream bis = new ByteArrayInputStream(Base64.decode(decoded))) {
                X509Certificate certificate = (X509Certificate) factory.generateCertificate(bis);
                handleAuthenticationWithKey(request, certificate);
            }
        }

        X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
        if (null != certs && certs.length > 0) {
            X509Certificate cert = certs[0];

            handleAuthenticationWithKey(request, cert);
        }

        return true;
    }

    private void handleAuthenticationWithKey(HttpServletRequest request, X509Certificate key) throws SecurityTokenInvalidException {
        if (validate(key)) {
            X500Principal principal = key.getSubjectX500Principal();
            X500Name      x500Name  = new X500Name(principal.getName());
            RDN           cn        = x500Name.getRDNs(BCStyle.CN)[0];

            String username = IETFUtils.valueToString(cn.getFirst().getValue());

            log.info("Username found in certificate: {}", username);

            String token = getJwtProcessor().getJwtTokenFor(getIdentityRepository().findByUsername(username).orElseThrow(), null);
            request.setAttribute(Constants.REQUEST_CLAIMS_FIELD, getJwtProcessor().validateToken(token));

            log.info("Created new JWT Token for current certificate authenticated request for user {}", username);
        }
    }

    @SneakyThrows
    private X509Certificate loadCA() {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        try (FileInputStream fis = new FileInputStream(caCertLocation)) {
            return (X509Certificate) factory.generateCertificate(fis);
        }
    }

    private boolean validate(X509Certificate certificate) {
        X509Certificate ca = loadCA();

        if (!certificate.equals(ca)) {
            try {
                certificate.verify(ca.getPublicKey());
                certificate.checkValidity();
                ca.checkValidity();
                // TODO: implement revocation by serial id
                //log.info("Certificate Serial: {}", certificate.getSerialNumber());
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }
}
