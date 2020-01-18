package de.reynok.authentication.core.backend.modules.webauthn;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import de.reynok.authentication.core.backend.database.entity.Identity;
import de.reynok.authentication.core.backend.database.entity.TPMCredential;
import de.reynok.authentication.core.backend.database.repository.IdentityRepository;
import de.reynok.authentication.core.backend.database.repository.TPMCredentialRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityNotFoundException;
import java.util.*;

@Slf4j
@Component
public class LocalCredentialRepository implements CredentialRepository {

    @Autowired
    private IdentityRepository      identityRepository;
    @Autowired
    private TPMCredentialRepository credentialRepository;

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        Identity            identity    = identityRepository.findByUsername(username).orElseThrow(EntityNotFoundException::new);
        List<TPMCredential> credentials = credentialRepository.findAllByIdentity(identity);

        log.info("getCredentialIdsForUsername({})", username);

        Set<PublicKeyCredentialDescriptor> descriptors = new HashSet<>();

        for (TPMCredential credential : credentials) {
            PublicKeyCredentialDescriptor descriptor = PublicKeyCredentialDescriptor.builder()
                    .id(new ByteArray(new byte[]{credential.getId().byteValue()})).build();

            descriptors.add(descriptor);
        }

        return descriptors;
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String s) {
        log.info("getUserHandleForUsername({})", s);
        return Optional.empty();
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray byteArray) {
        log.info("getUsernameForUserHandle({})", byteArray);
        return Optional.empty();
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray byteArray, ByteArray byteArray1) {
        log.info("lookup({}, {})", byteArray, byteArray1);
        return Optional.empty();
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray byteArray) {
        log.info("lookup({})", byteArray);
        return new HashSet<>();
    }
}
