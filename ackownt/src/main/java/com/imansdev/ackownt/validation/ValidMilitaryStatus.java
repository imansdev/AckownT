package com.imansdev.ackownt.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = MilitaryStatusValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMilitaryStatus {
    String message() default "Male users above 18 must not have military status of 'NONE'";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
