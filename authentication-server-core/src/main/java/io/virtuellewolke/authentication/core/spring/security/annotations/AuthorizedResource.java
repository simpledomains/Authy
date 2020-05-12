package io.virtuellewolke.authentication.core.spring.security.annotations;

import io.virtuellewolke.authentication.core.spring.security.SecureContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthorizedResource {
    SecureContext.Source[] allowedSources() default {};
}