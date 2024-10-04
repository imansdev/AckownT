package com.imansdev.ackownt.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NationalIdValidator.class)
public @interface ValidNationalId {
    String message() default "Invalid National ID";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
