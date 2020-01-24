package de.reynok.authentication.core.backend.components;

import de.reynok.authentication.core.backend.database.entity.ClientAuthCert;
import de.reynok.authentication.core.backend.database.entity.Identity;
import de.reynok.authentication.core.backend.database.repository.ClientAuthCertRepository;
import de.reynok.authentication.core.backend.database.repository.IdentityRepository;
import de.reynok.authentication.core.frontend.configuration.FrontendConfiguration;
import de.reynok.authentication.core.shared.exceptions.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class X509Manager {
    @Value("${server.ssl.client-auth-config.public-key:ca.pub.pem}")
    private File   caPublicCert;
    @Value("${server.ssl.client-auth-config.private-key:ca.pem}")
    private File   caPrivateKey;
    @Value("${server.ssl.client-auth-config.private-key-password:schnitzel}")
    private String caCertPassword;
    @Value("${openssl.binary:openssl}")
    private String opensslExecutable;

    private X509Certificate caCache;
    private File            certificateDirectory = new File("certificates/");

    private final IdentityRepository       identityRepository;
    private final ClientAuthCertRepository clientAuthCertRepository;
    private final FrontendConfiguration    frontendConfiguration;

    @SneakyThrows
    public X509Certificate getCaCertificate() {
        if (caCache == null) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            try (FileInputStream fis = new FileInputStream(caPublicCert)) {
                caCache = (X509Certificate) factory.generateCertificate(fis);
            } catch (Throwable e) {
                log.trace("Failed importing CA certificate from {} due to {}", caPublicCert, e.getMessage(), e);
            }
        }

        return caCache;
    }

    public boolean isRevoked(BigInteger serial) {
        ClientAuthCert cert = clientAuthCertRepository.findById(serial).orElse(null);

        return cert == null || cert.isRevoked();
    }

    public void revoke(BigInteger serial) {
        ClientAuthCert cert = clientAuthCertRepository.findById(serial).orElse(null);

        if (cert != null) {
            cert.setRevokedAt(LocalDateTime.now());
            clientAuthCertRepository.save(cert);
        }
    }

    public void revoke(BigInteger serial, Identity isAllowedBy) {
        ClientAuthCert cert = clientAuthCertRepository.findById(serial).orElse(null);

        if (cert != null && cert.getIdentity().equals(isAllowedBy)) {
            cert.setRevokedAt(LocalDateTime.now());
            clientAuthCertRepository.save(cert);
        }
    }

    public void markLastAccess(ClientAuthCert cert) {
        if (cert.getLastAccess() == null || LocalDateTime.now().isBefore(LocalDateTime.now().minusMinutes(10))) {
            cert.setLastAccess(LocalDateTime.now());
            clientAuthCertRepository.save(cert);
        }
    }

    public byte[] issuePfx(Identity identity) throws IOException {
        if (!frontendConfiguration.getClientCertAuth()) {
            throw new ServiceException("X509 authentication is not enabled.");
        }

        Integer userId = identity.getId();

        BigInteger serial = new BigInteger(userId + "" + (LocalDateTime.now()).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        issueSystemCommand(new String[]{opensslExecutable, "genrsa", "-aes256", "-passout", "pass:test", "-out", userId + ".pass.key", "4096"});
        issueSystemCommand(new String[]{opensslExecutable, "rsa", "-passin", "pass:test", "-in", userId + ".pass.key", "-out", userId + ".key"});
        issueSystemCommand(new String[]{opensslExecutable, "req", "-subj", "/O=Authentication Service/C=DE/CN=" + identity.getUsername(), "-new", "-key", userId + ".key", "-out", userId + ".csr"});
        issueSystemCommand(new String[]{opensslExecutable, "x509", "-req", "-days", "3650", "-in", userId + ".csr", "-passin", "pass:" + caCertPassword, "-CA", caPublicCert.getPath(), "-CAkey", caPrivateKey.getPath(), "-set_serial", serial.toString(), "-out", userId + ".pem"});
        issueSystemCommand(new String[]{opensslExecutable, "pkcs12", "-passout", "pass:", "-export", "-out", userId + ".pfx", "-inkey", userId + ".key", "-in", userId + ".pem", "-certfile", caPublicCert.getPath()});

        ClientAuthCert clientAuthCert = new ClientAuthCert();
        clientAuthCert.setSerial(serial);
        clientAuthCert.setIdentity(identity);
        clientAuthCert.setIssuedAt(LocalDateTime.now());

        clientAuthCertRepository.save(clientAuthCert);

        return FileUtils.readFileToByteArray(new File(certificateDirectory.getPath() + "/" + userId + ".pfx"));
    }

    private void issueSystemCommand(String[] cmd) {
        if (!certificateDirectory.exists() && !certificateDirectory.isDirectory()) {
            certificateDirectory.mkdirs();
        }

        try {
            Process process = Runtime.getRuntime().exec(cmd, new String[0], certificateDirectory);
            log.debug("Command {} printed:\n{}", Arrays.toString(cmd), IOUtils.toString(process.getInputStream(), Charset.defaultCharset()));
            int code = process.waitFor();

            if (code != 0) {

                throw new IOException("Process " + Arrays.toString(cmd) + " returned non-zero status " + code);
            }
        } catch (IOException | InterruptedException e) {
            throw new ServiceException("Error while processing X509 request.", e);
        }
    }
}