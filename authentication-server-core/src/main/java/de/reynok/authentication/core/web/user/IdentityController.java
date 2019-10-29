package de.reynok.authentication.core.web.user;

import com.amdelamar.jotp.OTP;
import com.amdelamar.jotp.type.Type;
import de.reynok.authentication.core.Constants;
import de.reynok.authentication.core.annotations.WebRequiresAuthentication;
import de.reynok.authentication.core.api.models.Identity;
import de.reynok.authentication.core.api.service.LoginRequest;
import de.reynok.authentication.core.logic.database.repository.IdentityRepository;
import de.reynok.authentication.core.util.ImageHelper;
import de.reynok.authentication.core.web.RequestProcessedController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
public class IdentityController extends RequestProcessedController {

    @Value("${app.security.otp.issuer:}")
    private String issuer;

    @Autowired
    public IdentityController(IdentityRepository identityRepository) {
        super(identityRepository);
    }

    @WebRequiresAuthentication
    @GetMapping("/api/me")
    public Identity me(HttpServletRequest request) {
        return getIdentityFromRequest(request);
    }

    @WebRequiresAuthentication
    @PatchMapping("/api/me")
    public Identity patchMe(@RequestBody Map<String, Object> update, HttpServletRequest request) {
        Identity identity = getIdentityFromRequest(request);

        identity.updateFrom(update);

        identityRepository.save(identity);

        return identity;
    }

    @WebRequiresAuthentication
    @GetMapping("/api/profile/otp/start")
    public ResponseEntity initializeOneTimePassword(HttpSession httpSession, HttpServletRequest request) {
        Identity id = getIdentityFromRequest(request);

        httpSession.setAttribute(Constants.OTP_SECRET, OTP.randomBase32(20));

        Map<String, String> output = new HashMap<>();
        output.put("message", "Secret created, verification required.");
        output.put("qr_code_location", "/api/profile/otp/qrcode");

        return ResponseEntity.ok(output);
    }

    @WebRequiresAuthentication
    @GetMapping(value = "/api/profile/otp/qrcode", produces = "image/png")
    public byte[] getQrCodeImageForOneTimePassword(HttpSession httpSession, HttpServletRequest request) {
        Identity id = getIdentityFromRequest(request);

        String obj = (String) httpSession.getAttribute(Constants.OTP_SECRET);

        if (obj == null) {
            throw new IllegalArgumentException("TOTP Secret is empty, you need to start the verification process first.");
        }

        return ImageHelper.createQrCode(OTP.getURL(obj, 6, Type.TOTP, issuer, id.getUsername()));
    }

    @WebRequiresAuthentication
    @PostMapping(value = "/api/profile/otp/verify")
    public ResponseEntity verifyOneTimePassword(@RequestBody LoginRequest verificationRequest, HttpServletRequest request, HttpSession httpSession) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        Identity id = getIdentityFromRequest(request);
        String obj = (String) httpSession.getAttribute(Constants.OTP_SECRET);

        if (obj != null) {
            if (Objects.equals(verificationRequest.getSecurityPassword(), OTP.create(obj, OTP.timeInHex(), 6, Type.TOTP))) {

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

    @WebRequiresAuthentication
    @PostMapping("/api/profile/api-token")
    public ResponseEntity generateApiToken(HttpServletRequest request) {
        Identity identity = getIdentityFromRequest(request);
        String key = RandomStringUtils.randomAlphanumeric(32);

        identity.setApiToken(key);

        identityRepository.save(identity);

        return ResponseEntity.status(202).body(key);
    }

    @WebRequiresAuthentication
    @DeleteMapping("/api/profile/api-token")
    public ResponseEntity deleteApiToken(HttpServletRequest request) {
        Identity identity = getIdentityFromRequest(request);

        identity.setApiToken(null);

        identityRepository.save(identity);

        return ResponseEntity.status(204).build();
    }
}