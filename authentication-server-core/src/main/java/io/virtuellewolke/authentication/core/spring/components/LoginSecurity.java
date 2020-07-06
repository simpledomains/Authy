package io.virtuellewolke.authentication.core.spring.components;

import io.virtuellewolke.authentication.core.spring.configuration.CasConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@SuppressWarnings("FieldCanBeLocal")
@RequiredArgsConstructor
public class LoginSecurity {

    private final Integer            MAX_ATTEMPTS        = 3;
    private final Integer            RETRY_AFTER_MINUTES = 60;
    private final List<LoginAttempt> attempts            = new ArrayList<>();

    private final CasConfiguration casConfiguration;

    private static class LoginAttempt {
        private int           count = 1;
        private LocalDateTime lastFailedTry;
        private String        source;
    }


    public boolean isAllowedToTry(HttpServletRequest source) {
        LoginAttempt attempt = attempts.stream().filter(loginAttempt -> Objects.equals(loginAttempt.source, getRequestSource(source))).findFirst().orElse(null);

        return attempt == null || attempt.count < MAX_ATTEMPTS || LocalDateTime.now().isAfter(attempt.lastFailedTry.plusMinutes(RETRY_AFTER_MINUTES));
    }

    public void resetAttempts(HttpServletRequest source) {
        attempts.removeIf(loginAttempt -> Objects.equals(getRequestSource(source), loginAttempt.source));
    }

    public void recordFailedAttempt(HttpServletRequest request) {
        LoginAttempt attempt = attempts.stream().filter(loginAttempt -> Objects.equals(loginAttempt.source, getRequestSource(request))).findFirst().orElse(null);

        if (Arrays.stream(casConfiguration.getLoginWhitelistIps()).anyMatch(s -> {
            IpAddressMatcher ipAddressMatcher = new IpAddressMatcher(s);
            return ipAddressMatcher.matches(getRequestSource(request));
        })) return;

        if (attempt != null) {
            attempt.count         = attempt.count + 1;
            attempt.lastFailedTry = LocalDateTime.now();
        } else {
            LoginAttempt a = new LoginAttempt();
            a.source        = getRequestSource(request);
            a.lastFailedTry = LocalDateTime.now();

            attempts.add(a);
        }

        log.info("{} tried to login with wrong credentials.", getRequestSource(request));
    }

    private String getRequestSource(HttpServletRequest request) {
        if (request.getHeader("x-forwarded-for") != null) {
            String forwardedFor = request.getHeader("x-forwarded-for");
            if (forwardedFor.contains(",")) {
                forwardedFor = forwardedFor.split(",")[0];
            }
            return forwardedFor.trim();
        } else {
            return request.getRemoteAddr();
        }
    }
}
