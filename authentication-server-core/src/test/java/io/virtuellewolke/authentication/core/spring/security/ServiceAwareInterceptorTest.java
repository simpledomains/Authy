package io.virtuellewolke.authentication.core.spring.security;

import io.virtuellewolke.authentication.core.AuthenticationApplication;
import io.virtuellewolke.authentication.core.spring.components.ServiceValidation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SpringBootTest()
@ExtendWith(SpringExtension.class)
class ServiceAwareInterceptorTest {

    @Test
    public void testAuthentication() throws Exception {
        ServiceValidation  serviceValidation = Mockito.mock(ServiceValidation.class);
        HttpServletRequest req               = new MockHttpServletRequest("GET", "/?service=/");

        ServiceAwareInterceptor interceptor = new DefaultTestImplementation(serviceValidation);

        interceptor.preHandle(req, null, null);

        Assertions.assertNotNull(SecureContext.getSecureContext(req));
    }

    private static class DefaultTestImplementation extends ServiceAwareInterceptor {
        public DefaultTestImplementation(ServiceValidation serviceValidation) {
            super(serviceValidation);
        }

        @Override
        public boolean process(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            return true;
        }
    }
}