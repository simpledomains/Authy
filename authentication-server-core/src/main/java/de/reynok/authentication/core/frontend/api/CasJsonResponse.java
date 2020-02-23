package de.reynok.authentication.core.frontend.api;

import io.virtuellewolke.authentication.core.database.entity.Authority;
import io.virtuellewolke.authentication.core.database.entity.Identity;
import de.reynok.authentication.core.backend.modules.cas.CasStatusCode;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;


public class CasJsonResponse {

    private HashMap<String, Object> root            = new HashMap<>();
    private boolean                 success         = false;
    private HashMap<String, Object> serviceResponse = new HashMap<>();

    public CasJsonResponse() {
        root.put("serviceResponse", serviceResponse);
    }

    public void error(CasStatusCode code, String errorMessage) {
        HashMap<String, Object> errorObject = new HashMap<>();

        errorObject.put("code", code);
        errorObject.put("description", errorMessage);

        root.put("authenticationFailure", errorObject);
    }

    public void success(Identity identity) {
        success = true;

        HashMap<String, Object> successObject = new HashMap<>();
        HashMap<String, Object> attributes    = new HashMap<>();

        successObject.put("user", identity.getUsername());
        successObject.put("attributes", attributes);

        if (identity.getMetaData() != null && !identity.getMetaData().isEmpty()) {
            for (String metaKey : identity.getMetaData().keySet()) {
                attributes.put(metaKey, identity.getMetaData().getOrDefault(metaKey, null));
            }
        }

        for (Authority authority : identity.getAuthorities()) {
            attributes.put("groups", identity.getAuthorities().stream().map(Authority::getName).toArray());
        }

        if (identity.getEmail() != null && identity.getEmail().length() > 0) {
            attributes.put("email", identity.getEmail());
        }

        if (identity.getDisplayName() != null && identity.getDisplayName().length() > 0) {
            attributes.put("displayName", identity.getDisplayName());
        }

        if (identity.getAvatar() != null && identity.getAvatar().length() > 0) {
            attributes.put("avatar", identity.getAvatar());
        }

        serviceResponse.put("authenticationSuccess", successObject);
    }

    public ResponseEntity toResponse() {
        if (success) {
            return ResponseEntity.ok(root);
        } else {
            return ResponseEntity.status(401).body(root);
        }
    }
}