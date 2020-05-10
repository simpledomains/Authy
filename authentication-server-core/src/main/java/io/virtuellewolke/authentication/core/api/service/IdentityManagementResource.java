package io.virtuellewolke.authentication.core.api.service;

import io.virtuellewolke.authentication.core.api.model.LoginRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.spring.security.annotations.AuthorizedResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * This Resource is a special endpoint to handle special cases for the frontend.
 */
@RequestMapping(value = "/api/session", produces = "application/json")
@Tag(name = "Authy - Identity Management")
public interface IdentityManagementResource {
    @AuthorizedResource
    @GetMapping("/me")
    ResponseEntity<Identity> myIdentity(HttpServletRequest request);

    @AuthorizedResource
    @PatchMapping("/me")
    ResponseEntity<Identity> patchIdentity(HttpServletRequest request, @RequestBody Map<String, Object> updateData);

    @AuthorizedResource
    @PostMapping("/me/otp")
    public ResponseEntity<Map<?, ?>> initializeOneTimePassword(HttpSession httpSession, HttpServletRequest request);

    @AuthorizedResource
    @DeleteMapping("/me/otp")
    public ResponseEntity<Identity> revokeTwoFactor(HttpSession httpSession, HttpServletRequest request);

    @AuthorizedResource
    @GetMapping(value = "/me/otp/image", produces = "image/png")
    public byte[] getQrCodeImageForOneTimePassword(HttpSession httpSession, HttpServletRequest request);

    @AuthorizedResource
    @PostMapping(value = "/me/otp/verify")
    public ResponseEntity<?> verifyOneTimePassword(@RequestBody LoginRequest verificationRequest, HttpServletRequest request, HttpSession httpSession) throws IOException, NoSuchAlgorithmException, InvalidKeyException;

    @AuthorizedResource
    @PostMapping("/me/api-token")
    public ResponseEntity<String> generateApiToken(HttpServletRequest request);

    @AuthorizedResource
    @DeleteMapping("/me/api-token")
    public ResponseEntity<?> deleteApiToken(HttpServletRequest request);
}