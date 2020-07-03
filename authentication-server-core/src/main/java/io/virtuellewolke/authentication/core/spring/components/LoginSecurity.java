package io.virtuellewolke.authentication.core.spring.components;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@SuppressWarnings("FieldCanBeLocal")
public class LoginSecurity {

    private final Integer            MAX_ATTEMPTS        = 3;
    private final Integer            RETRY_AFTER_MINUTES = 60;
    private final List<LoginAttempt> attempts            = new ArrayList<>();

    @Value("${cas.security.error-whitelist}")
    private String[] WHITELIST;

    private static class LoginAttempt {
        private int           count = 1;
        private LocalDateTime lastFailedTry;
        private String        source;
    }


    public boolean isAllowedToTry(String source) {
        LoginAttempt attempt = attempts.stream().filter(loginAttempt -> Objects.equals(loginAttempt.source, source)).findFirst().orElse(null);

        return attempt == null || attempt.count < MAX_ATTEMPTS || LocalDateTime.now().isAfter(attempt.lastFailedTry.plusMinutes(RETRY_AFTER_MINUTES));
    }

    public void resetAttempts(String source) {
        attempts.removeIf(loginAttempt -> Objects.equals(source, loginAttempt.source));
    }

    public void recordFailedAttempt(String source) {
        LoginAttempt attempt = attempts.stream().filter(loginAttempt -> Objects.equals(loginAttempt.source, source)).findFirst().orElse(null);

        if (Arrays.stream(WHITELIST).anyMatch(s -> {
            IpAddressMatcher ipAddressMatcher = new IpAddressMatcher(s);
            return ipAddressMatcher.matches(source);
        })) return;

        if (attempt != null) {
            attempt.count         = attempt.count + 1;
            attempt.lastFailedTry = LocalDateTime.now();
        } else {
            LoginAttempt a = new LoginAttempt();
            a.source        = source;
            a.lastFailedTry = LocalDateTime.now();

            attempts.add(a);
        }

        log.info("{} tried to login with wrong credentials.", source);
    }

}
