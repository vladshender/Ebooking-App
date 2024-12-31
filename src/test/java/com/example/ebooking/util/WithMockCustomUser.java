package com.example.ebooking.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithSecurityContext;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@WithSecurityContext(factory = WithMockUserSecurityContexFactory.class)
public @interface WithMockCustomUser {

    String value() default "user";

    String username() default "";

    String[] roles() default { "USER" };

    String[] authorities() default {};

    String password() default "password";

    String name() default "";

    String email() default "";

    @AliasFor(annotation = WithSecurityContext.class)
    TestExecutionEvent setupBefore() default TestExecutionEvent.TEST_METHOD;
}
