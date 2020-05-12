package io.virtuellewolke.authentication.core.spring.security;

import io.virtuellewolke.authentication.core.spring.components.ServiceValidation;
import io.virtuellewolke.authentication.core.spring.helper.ServiceRequestHelper;
import io.virtuellewolke.authentication.core.spring.security.mods.ServiceAwareInterceptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SpringBootTest()
@ExtendWith(SpringExtension.class)
class ServiceAwareInterceptorTest {
    @Autowired
    private ServiceValidation serviceValidation;

    @Test
    public void testGetService() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/");

        req.addParameter("service", "/");

        ServiceAwareInterceptor interceptor = new DefaultTestImplementation(serviceValidation);

        interceptor.preHandle(req, null, null);

        Assertions.assertNotNull(ServiceRequestHelper.getService(req));
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