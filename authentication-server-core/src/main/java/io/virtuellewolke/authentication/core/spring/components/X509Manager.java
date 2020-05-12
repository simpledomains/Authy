package io.virtuellewolke.authentication.core.spring.components;

import io.virtuellewolke.authentication.core.database.entity.ClientAuthCert;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.repository.ClientAuthCertRepository;
import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import io.virtuellewolke.authentication.core.exceptions.ServiceException;
import io.virtuellewolke.authentication.core.spring.configuration.X509ManagerConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v1CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.security.auth.x500.X500Principal;
import java.io.*;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

    public void revoke(BigInteger serial, Identity identity) {
        ClientAuthCert cert = clientAuthCertRepository.findById(serial).orElseThrow();

        if (cert != null && (cert.getIdentity().equals(identity) || (identity != null && identity.getAdmin()))) {
            cert.setRevokedAt(LocalDateTime.now());
            log.info("Revoked {} for Identity {}", cert.getSerial(), identity.getId());
            clientAuthCertRepository.save(cert);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    public byte[] issuePfx(Identity identity, String certificateName) {
        if (!configuration.getEnabled()) {
            throw new ServiceException("X509 authentication is not enabled.");
        }

        log.info("Issuing X509 mTLS certificate for Identity {} with the Name {}", identity.getId(), certificateName);

        Integer userId = identity.getId();

        BigInteger serial = new BigInteger(userId + "" + (LocalDateTime.now()).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        try {
            Security.addProvider(new BouncyCastleProvider());

            KeyPair caKeyPair = loadKeys(configuration.getCaPrivateKey());

            // STEP 1: GENERATE KEY
            KeyPair userKeyPair = generateRSAKeyPair();
            // STEP 2: GENERATE CSR
            PKCS10CertificationRequest csr = createCertRequest(userKeyPair, identity);
            // STEP 3: SIGN CERTIFICATE
            X509Certificate cert = signCertificateRequestWithCA(csr, serial, caKeyPair.getPrivate(), getCaCertificate(), identity);
            // STEP 4: BUILD PFX FILE
            byte[] pfx = generatePfxStore(userKeyPair, cert, identity);

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

    /**
     * Signs a certificate request with a provide CA certificate.
     *
     * @implNote for some reason, a mTLS certificate cant be a V3 certificate and must be a V1 certificate. idk why.
     */
    private X509Certificate signCertificateRequestWithCA(PKCS10CertificationRequest request, BigInteger serial, PrivateKey caPrivateKey, X509Certificate caPublicKey, Identity identity) throws Exception {
        LocalDateTime localDateTime = LocalDateTime.now().plusYears(5);
        Date          notAfter      = Date.from(localDateTime.toInstant(ZoneOffset.UTC));

        var certGen = new X509v1CertificateBuilder(
                new X500Name(PrincipalUtil.getIssuerX509Principal(caPublicKey).getName()),
                serial,
                new Date(),
                notAfter,
                request.getSubject(),
                request.getSubjectPublicKeyInfo()
        );

        var contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption").setProvider("BC").build(caPrivateKey);
        return (new JcaX509CertificateConverter()).setProvider("BC").getCertificate(certGen.build(contentSigner));
    }

    /**
     * Creates a CSR.
     */
    private PKCS10CertificationRequest createCertRequest(KeyPair keyPair, Identity identity) throws OperatorCreationException {
        X500Principal subject = new X500Principal("O=Authentication Service, C=DE, CN=" + identity.getUsername());

        ContentSigner                     contentSigner = new JcaContentSignerBuilder("SHA1withRSA").build(keyPair.getPrivate());
        PKCS10CertificationRequestBuilder builder       = new JcaPKCS10CertificationRequestBuilder(subject, keyPair.getPublic());

        return builder.build(contentSigner);
    }

    /**
     * Creates a PFX keystore for the user to download.
     */
    private byte[] generatePfxStore(KeyPair keyPair, X509Certificate certificate, Identity identity) throws NoSuchProviderException, KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        X509Certificate caPublicKey = getCaCertificate();

        KeyStore ks = KeyStore.getInstance("PKCS12", "BC");
        ks.load(null, null);
        ks.setCertificateEntry(certificate.getSerialNumber().toString(), certificate);
        ks.setCertificateEntry("CA Certificate", caPublicKey);
        ks.setKeyEntry(certificate.getSerialNumber().toString(), keyPair.getPrivate(), new char[0], new Certificate[]{certificate, certificate});

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ks.store(bos, new char[0]);
        return bos.toByteArray();
    }

    /**
     * Generates a new RSA keypair for the user to be downloaded.
     */
    public static KeyPair generateRSAKeyPair() throws Exception {
        KeyPairGenerator kpGen = KeyPairGenerator.getInstance("RSA", "BC");
        kpGen.initialize(2048, new SecureRandom());
        return kpGen.generateKeyPair();
    }

    /**
     * Loads the CA private key as keypair from file.
     */
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
            throw new ServiceException("CA Private key is not readable.");
        }

        if (kp == null) {
            throw new ServiceException("CA Private key is not readable.");
        }

        return kp;
    }
}