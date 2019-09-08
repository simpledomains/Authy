package de.reynok.authentication.core.web;

import com.amdelamar.jotp.OTP;
import com.amdelamar.jotp.type.Type;
import de.reynok.authentication.core.configuration.Constants;
import de.reynok.authentication.core.database.entity.Identity;
import de.reynok.authentication.core.database.repository.IdentityRepository;
import de.reynok.authentication.core.util.ImageHelper;
import de.reynok.authentication.core.web.api.OneTimePasswordVerificationRequest;
import de.reynok.authentication.core.web.api.RestError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
public class IdentityController extends RequestProcessedController {

    @Value("${app.security.otp.issuer:}")
    private String  issuer;
    @Value("${app.security.otp.digits:6}")
    private Integer digits;

    @Autowired
    public IdentityController(IdentityRepository identityRepository) {
        super(identityRepository);
    }

    @GetMapping("/api/me")
    public Identity me(HttpServletRequest request) {
        return getIdentityFromRequest(request);
    }

    @GetMapping("/api/profile/otp/start")
    public ResponseEntity initializeOneTimePassword(HttpSession httpSession, HttpServletRequest request) {
        Identity id = getIdentityFromRequest(request);

        httpSession.setAttribute(Constants.OTP_SECRET, OTP.randomBase32(20));

        Map<String, String> output = new HashMap<>();
        output.put("message", "Secret created, verification required.");
        output.put("qr_code_location", "/api/profile/otp/qrcode");

        return ResponseEntity.ok(output);
    }

    @GetMapping(value = "/api/profile/otp/qrcode", produces = "image/png")
    public byte[] getQrCodeImageForOneTimePassword(HttpSession httpSession, HttpServletRequest request) {
        Identity id = getIdentityFromRequest(request);

        String obj = (String) httpSession.getAttribute(Constants.OTP_SECRET);

        if (obj == null) {
            throw new IllegalArgumentException("TOTP Secret is empty, you need to start the verification process first.");
        }

        return ImageHelper.createQrCode(OTP.getURL(obj, digits, Type.TOTP, issuer, id.getUsername()));
    }

    @PostMapping(value = "/api/profile/otp/verify")
    public ResponseEntity verifyOneTimePassword(@RequestBody OneTimePasswordVerificationRequest verificationRequest, HttpServletRequest request, HttpSession httpSession) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        Identity id  = getIdentityFromRequest(request);
        String   obj = (String) httpSession.getAttribute(Constants.OTP_SECRET);

        if (obj != null) {
            if (Objects.equals(verificationRequest.getTotp(), OTP.create(obj, OTP.timeInHex(), digits, Type.TOTP))) {

                id.setOneTimePassword(obj);
                identityRepository.save(id);

                return ResponseEntity.ok().build();
            } else {
                throw new IllegalArgumentException("One-Time-Password does not match.");
            }
        } else {
            throw new IllegalArgumentException("TOTP Secret is empty, you need to start the verification process first.");
        }
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity entityNotFound(EntityNotFoundException e) {
        return new RestError(e, e.getMessage() != null ? e.getMessage() : "Entity not found.", 404).toResponse();
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity throwable(Throwable e) {
        return new RestError(e, e.getMessage() != null ? e.getMessage() : "A unknown error occurred.", 500).toResponse();
    }
}