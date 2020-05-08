package io.virtuellewolke.authentication.core.spring.components;

import de.reynok.authentication.core.shared.exceptions.ServiceException;
import io.virtuellewolke.authentication.core.database.entity.ClientAuthCert;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.repository.ClientAuthCertRepository;
import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import io.virtuellewolke.authentication.core.spring.configuration.X509ManagerConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
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
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@ManagedResource
public class X509Manager {

    private X509Certificate caCache;

    private File certificateDirectory = new File("certificates/");

    private final X509ManagerConfiguration configuration;
    private final IdentityRepository       identityRepository;
    private final ClientAuthCertRepository clientAuthCertRepository;

    @ManagedOperation(description = "Cleans the revoked certificates")
    @Scheduled(fixedDelayString = "#{@x509ManagerConfiguration.cleanupTimer}")
    public void cleanupRevoked() {
        if (configuration.getEnabled()) {
            log.info("Cleaning revoked certificates ...");

            List<ClientAuthCert> certs = clientAuthCertRepository.findAllByRevokedAtLessThanAndRevokedAtIsNotNull(LocalDateTime.now().minusDays(1));

            if (log.isDebugEnabled()) {
                certs.forEach(cert -> log.debug("Deleting revoked certificate {}, revoked at {} for identity {}", cert.getSerial(), cert.getRevokedAt(), cert.getIdentity().getId()));
            }

            clientAuthCertRepository.deleteAll(certs);

            log.info("Cleaned {} revoked certificates ...", certs.size());
        }
    }

    @SneakyThrows
    public X509Certificate getCaCertificate() {
        if (caCache == null) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            try (FileInputStream fis = new FileInputStream(configuration.getCaPublicCert())) {
                caCache = (X509Certificate) factory.generateCertificate(fis);
            } catch (Throwable e) {
                log.trace("Failed importing CA certificate from {} due to {}", configuration.getCaPublicCert(), e.getMessage(), e);
            }
        }

        return caCache;
    }

    public ClientAuthCert getCertificateFor(BigInteger serial) {
        return clientAuthCertRepository.findById(serial).orElse(null);
    }

    public boolean isRevoked(ClientAuthCert cert) {
        return cert == null || cert.isRevoked();
    }

    public boolean isRevoked(BigInteger serial) {
        return isRevoked(getCertificateFor(serial));
    }

    public void markAsUsed(ClientAuthCert cert) {
        if (cert != null) {
            if (cert.getLastAccess() == null || LocalDateTime.now().isAfter(cert.getLastAccess().plusMinutes(10))) {
                cert.setLastAccess(LocalDateTime.now());
                clientAuthCertRepository.save(cert);
            }
        }
    }

    public void revoke(BigInteger serial) {
        ClientAuthCert cert = clientAuthCertRepository.findById(serial).orElse(null);

        if (cert != null) {
            cert.setRevokedAt(LocalDateTime.now());
            clientAuthCertRepository.save(cert);
        }
    }

    public boolean revoke(BigInteger serial, Identity identity) {
        ClientAuthCert cert = clientAuthCertRepository.findById(serial).orElseThrow();

        if (cert != null && (cert.getIdentity().equals(identity) || (identity != null && identity.getAdmin()))) {
            cert.setRevokedAt(LocalDateTime.now());
            log.info("Revoked {} for Identitiy {}", cert.getSerial(), identity.getId());
            clientAuthCertRepository.save(cert);
            return true;
        }
        return false;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public byte[] issuePfx(Identity identity, String certificateName) throws IOException {
        if (!configuration.getEnabled()) {
            throw new ServiceException("X509 authentication is not enabled.");
        }

        log.info("Issuing X509 mTLS certificate for Identity {} with the Name {}", identity.getId(), certificateName);

        Integer userId = identity.getId();

        BigInteger serial = new BigInteger(userId + "" + (LocalDateTime.now()).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        issueSystemCommand(new String[]{configuration.getOpensslBinary(), "genrsa", "-aes256", "-passout", "pass:test", "-out", userId + ".pass.key", "4096"});
        issueSystemCommand(new String[]{configuration.getOpensslBinary(), "rsa", "-passin", "pass:test", "-in", userId + ".pass.key", "-out", userId + ".key"});
        issueSystemCommand(new String[]{configuration.getOpensslBinary(), "req", "-subj", "/O=" + configuration.getOrganisation() + "/C=" + configuration.getCountryCode() + "/CN=" + identity.getUsername(), "-new", "-key", userId + ".key", "-out", userId + ".csr"});
        issueSystemCommand(new String[]{configuration.getOpensslBinary(), "x509", "-req", "-days", "3650", "-in", userId + ".csr", "-passin", "pass:" + configuration.getCaPrivateKeyPassphrase(), "-CA", configuration.getCaPublicCert().getPath(), "-CAkey", configuration.getCaPrivateKey().getPath(), "-set_serial", serial.toString(), "-out", userId + ".pem"});
        issueSystemCommand(new String[]{configuration.getOpensslBinary(), "pkcs12", "-passout", "pass:", "-export", "-out", userId + ".pfx", "-inkey", userId + ".key", "-in", userId + ".pem", "-certfile", configuration.getCaPublicCert().getPath()});

        ClientAuthCert clientAuthCert = new ClientAuthCert();
        clientAuthCert.setSerial(serial);
        clientAuthCert.setIdentity(identity);
        clientAuthCert.setIssuedAt(LocalDateTime.now());
        clientAuthCert.setName(certificateName);

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