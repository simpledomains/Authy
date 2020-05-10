package io.virtuellewolke.authentication.core.api.service;

import com.amdelamar.jotp.OTP;
import com.amdelamar.jotp.type.Type;
import io.virtuellewolke.authentication.core.api.Constants;
import io.virtuellewolke.authentication.core.api.model.LoginRequest;
import io.virtuellewolke.authentication.core.util.ImageHelper;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.database.repository.IdentityRepository;
import io.virtuellewolke.authentication.core.spring.configuration.CasConfiguration;
import io.virtuellewolke.authentication.core.spring.helper.SecureContextRequestHelper;
import io.virtuellewolke.authentication.core.spring.security.SecureContext;
import io.virtuellewolke.authentication.core.spring.security.annotations.AuthorizedResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


@Slf4j
@RestController
@RequiredArgsConstructor
public class IdentityManagementResourceImpl implements IdentityManagementResource {

    private final IdentityRepository identityRepository;
    private final CasConfiguration   casConfiguration;

    @Override
    @AuthorizedResource
    public ResponseEntity<Identity> myIdentity(HttpServletRequest request) {
        return ResponseEntity.ok(getIdentityFromRequest(request));
    }

    @Override
    @AuthorizedResource
    public ResponseEntity<Identity> patchIdentity(HttpServletRequest request, Map<String, Object> updateData) {
        SecureContext context = SecureContextRequestHelper.getSecureContext(request);
        if (context != null && context.getIdentity() != null) {
            Identity identity = identityRepository.findById(context.getIdentity().getId()).orElseThrow(EntityNotFoundException::new);
            identity.updateFrom(updateData);
            identityRepository.save(identity);

            return ResponseEntity.ok(identity);
        }
        return ResponseEntity.notFound().build();
    }

    @Override
    @AuthorizedResource
    public ResponseEntity<Map<?, ?>> initializeOneTimePassword(HttpSession httpSession, HttpServletRequest request) {
        Map<String, String> output = new HashMap<>();

        if (httpSession.getAttribute(Constants.OTP_SECRET) == null) {
            httpSession.setAttribute(Constants.OTP_SECRET, OTP.randomBase32(20));
            output.put("info", "Retry old code.");
        }

        output.put("message", "Secret created, verification required.");
        output.put("qr_code_location", "/api/session/me/otp/image");

        return ResponseEntity.ok(output);
    }

    @Override
    public ResponseEntity<Identity> revokeTwoFactor(HttpSession httpSession, HttpServletRequest request) {
        Identity identity = identityRepository.findById(getIdentityFromRequest(request).getId()).orElseThrow(EntityNotFoundException::new);
        identity.setOtpSecret(null);
        identityRepository.save(identity);
        return ResponseEntity.ok(identity);
    }

    @Override
    @AuthorizedResource
    public byte[] getQrCodeImageForOneTimePassword(HttpSession httpSession, HttpServletRequest request) {
        Identity id = getIdentityFromRequest(request);

        String obj = (String) httpSession.getAttribute(Constants.OTP_SECRET);

        if (obj == null) {
            throw new IllegalArgumentException("TOTP Secret is empty, you need to start the verification process first.");
        }

        return ImageHelper.createQrCode(OTP.getURL(obj, 6, Type.TOTP, casConfiguration.getTotpIssuerName(), id.getUsername()));
    }

    @Override
    @AuthorizedResource
    public ResponseEntity<?> verifyOneTimePassword(@RequestBody LoginRequest verificationRequest, HttpServletRequest request, HttpSession httpSession) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        Identity id  = getIdentityFromRequest(request);
        String   obj = (String) httpSession.getAttribute(Constants.OTP_SECRET);

        if (obj != null) {
            if (Objects.equals(verificationRequest.getSecurityPassword(), OTP.create(obj, OTP.timeInHex(), 6, Type.TOTP))) {

                httpSession.removeAttribute(Constants.OTP_SECRET);

                id.setOtpSecret(obj);
                identityRepository.save(id);

                return ResponseEntity.ok().build();
            } else {
                throw new IllegalArgumentException("One-Time-Password does not match.");
            }
        } else {
            throw new IllegalArgumentException("TOTP Secret is empty, you need to start the verification process first.");
        }
    }

    @Override
    @AuthorizedResource
    public ResponseEntity<String> generateApiToken(HttpServletRequest request) {
        Identity identity = getIdentityFromRequest(request);
        String   key      = RandomStringUtils.randomAlphanumeric(32);

        identity.setApiToken(key);

        log.info("Identity {} requested a new API token, old token was revoked immediately", identity.getId());

        identityRepository.save(identity);

        return ResponseEntity.status(202).body(key);
    }

    @Override
    @AuthorizedResource
    public ResponseEntity<?> deleteApiToken(HttpServletRequest request) {
        Identity identity = getIdentityFromRequest(request);

        identity.setApiToken(null);

        log.info("Identity {} revoked his API token completely", identity.getId());

        identityRepository.save(identity);

        return ResponseEntity.status(204).build();
    }

    private Identity getIdentityFromRequest(HttpServletRequest request) {
        SecureContext context = SecureContextRequestHelper.getSecureContext(request);

        if (context != null) return context.getIdentity();
        throw new IllegalArgumentException("Identity is null but should not be.");
    }
}