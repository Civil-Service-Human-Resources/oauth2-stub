package uk.gov.cshr.validation.annotations;

import uk.gov.cshr.validation.validators.WhitelistedValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, METHOD})
@Retention(RUNTIME)
@Constraint(validatedBy = WhitelistedValidator.class)

public @interface Whitelisted {
    String message() default "{validation.email.whitelist}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
