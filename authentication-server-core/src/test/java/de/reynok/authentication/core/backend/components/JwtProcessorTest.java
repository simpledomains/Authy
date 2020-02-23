package de.reynok.authentication.core.backend.components;

import io.virtuellewolke.authentication.core.database.entity.Identity;
import io.virtuellewolke.authentication.core.spring.components.JwtProcessor;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;


@RunWith(JUnit4.class)
@NoArgsConstructor
class JwtProcessorTest {

    private JwtProcessor jwtProcessor = new JwtProcessor();

    @Test
    public void testCreateJWTToken() {
        String jwt = jwtProcessor.getJwtTokenFor(getTestIdentity(), null);

        jwtProcessor.validateToken(jwt);
    }


    private Identity getTestIdentity() {
        Identity identity = new Identity();
        identity.setId(1);
        identity.setUsername("admin");
        identity.setAuthorities(new ArrayList<>());
        identity.setAdmin(false);
        return identity;
    }
}