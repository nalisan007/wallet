package io.wallet.entity;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = DifferentWalletsValidator.class)
public @interface DifferentWallets {

    String message() default "From wallet and to wallet must be different";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
