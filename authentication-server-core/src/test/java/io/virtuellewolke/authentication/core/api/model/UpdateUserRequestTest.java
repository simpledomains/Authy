package io.virtuellewolke.authentication.core.api.model;

import io.virtuellewolke.authentication.core.database.entity.Identity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.TypeMismatchException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UpdateUserRequestTest {

    private static final String TEST_USERNAME       = "SeaLife";
    private static final String TEST_API_TOKEN      = "KLjdncKdP";
    private static final String TEST_LOCKED         = "true";
    private static final String TEST_ADMIN          = "true";
    private static final String TEST_BOOLEAN_FAILED = "oof";

    private UpdateUserRequest updateUserRequest = new UpdateUserRequest();

    @Test
    public void testUpdateUsername() {
        Identity          identity = getBaseIdentity();
        UpdateUserRequest request  = getUpdateUserRequest("username", TEST_USERNAME);

        request.update(identity);

        assertEquals(TEST_USERNAME, identity.getUsername());
    }

    @Test
    public void testUpdateLocked() {
        Identity          identity = getBaseIdentity();
        UpdateUserRequest request  = getUpdateUserRequest("locked", TEST_LOCKED);

        request.update(identity);

        assertTrue(identity.getLocked());
    }

    @Test
    public void testUpdateNonExistingUpdateNothing() {
        Identity          identity = getBaseIdentity();
        UpdateUserRequest request  = getUpdateUserRequest("xxxxx", TEST_LOCKED);

        request.update(identity);

        assertFalse(identity.getLocked());
    }

    @Test
    public void testUpdateSetNull() {
        Identity          identity = getBaseIdentity();
        UpdateUserRequest request  = getUpdateUserRequest("username", null);
        request.update(identity);
        assertNull(identity.getUsername());
    }

    @Test
    public void testUpdateBooleanWithNonBooleanValue() {
        assertThrows(TypeMismatchException.class, () -> {
            Identity          identity = getBaseIdentity();
            UpdateUserRequest request  = getUpdateUserRequest("locked", TEST_BOOLEAN_FAILED);
            request.update(identity);
        });
    }

    @SuppressWarnings("SpellCheckingInspection")
    private Identity getBaseIdentity() {
        Identity identity = new Identity();
        identity.setUsername("XXXXX");
        identity.setPassword("XXXXX");
        identity.setLocked(false);
        identity.setAdmin(false);
        identity.setId(1);
        identity.setApiToken("XXXXX");
        identity.setDisplayName("XXXXX");
        return identity;
    }

    private UpdateUserRequest getUpdateUserRequest(String key, Object val) {
        Map<String, Object> data = new HashMap<>();
        data.put(key, val);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setData(data);
        return request;
    }
}