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
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@ManagedResource
public class X509Manager {

    private X509Certificate caCache;

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
            try (FileInputStream fis = new FileInputStream(configuration.getCaPublicKey())) {
                caCache = (X509Certificate) factory.generateCertificate(fis);
            } catch (Throwable e) {
                log.trace("Failed importing CA certificate from {} due to {}", configuration.getCaPublicKey(), e.getMessage(), e);
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

        try {
            Security.addProvider(new BouncyCastleProvider());

            KeyPair keyPair = loadKeys(configuration.getCaPrivateKey());

            X509Certificate certificate = generateV3Certificate(keyPair, serial, identity);

            byte[] pfx = generatePfxStore(keyPair, certificate, identity);

            ClientAuthCert clientAuthCert = new ClientAuthCert();
            clientAuthCert.setSerial(serial);
            clientAuthCert.setIdentity(identity);
            clientAuthCert.setIssuedAt(LocalDateTime.now());
            clientAuthCert.setName(certificateName);

            clientAuthCertRepository.save(clientAuthCert);

            return pfx;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServiceException("Failed to issue new certificate.", e);
        }
    }

    private byte[] generatePfxStore(KeyPair keyPair, X509Certificate certificate, Identity identity) throws NoSuchProviderException, KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        X509Certificate caPublicKey = getCaCertificate();

        KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
        ks.load(null, null);
        ks.setCertificateEntry(certificate.getSerialNumber().toString(), certificate);
        ks.setCertificateEntry("CA Certificate", caPublicKey);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ks.store(bos, new char[0]);
        return bos.toByteArray();
    }

    private KeyPair loadKeys(File privateKey) throws Exception {
        PEMParser          pemParser = new PEMParser(new FileReader(privateKey));
        Object             object    = pemParser.readObject();
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

        KeyPair kp;

        if (object instanceof PEMEncryptedKeyPair) {
            String password = configuration.getCaPrivateKeyPassphrase();

            PEMEncryptedKeyPair  ckp     = (PEMEncryptedKeyPair) object;
            PEMDecryptorProvider decProv = new JcePEMDecryptorProviderBuilder().build(password.toCharArray());
            kp = converter.getKeyPair(ckp.decryptKeyPair(decProv));
        } else if (object instanceof PEMKeyPair) {
            PEMKeyPair ukp = (PEMKeyPair) object;
            kp = converter.getKeyPair(ukp);
        } else if (object instanceof X509CertificateHolder) {
            throw new ServiceException("CA Private key is probably a public key.");
        } else {
            return null;
        }

        return kp;
    }

    private X509Certificate generateV3Certificate(KeyPair keyPair, BigInteger serial, Identity identity) throws SignatureException, NoSuchProviderException, InvalidKeyException {
        var builder = new X509V3CertificateGenerator();
        builder.setSerialNumber(serial);
        builder.setIssuerDN(new X500Principal("CN=" + configuration.getOrganisation()));
        builder.setNotBefore(new Date(System.currentTimeMillis() - 10000));
        builder.setNotAfter(new Date(System.currentTimeMillis() + 10000));
        builder.setSubjectDN(new X500Principal("CN=" + identity.getUsername()));
        builder.setPublicKey(keyPair.getPublic());
        builder.setSignatureAlgorithm("SHA256WithRSAEncryption");

        builder.addExtension(X509Extensions.BasicConstraints, true, new BasicConstraints(false));
        builder.addExtension(X509Extensions.KeyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
        builder.addExtension(X509Extensions.ExtendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth));

        builder.addExtension(X509Extensions.SubjectAlternativeName, false, new GeneralNames(
                new GeneralName(GeneralName.rfc822Name, identity.getEmail())
        ));

        return builder.generateX509Certificate(keyPair.getPrivate(), "BC");
    }
}