package uk.gov.cshr.validation.annotation;

import uk.gov.cshr.validation.validator.MatchesPolicyValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD, METHOD})
@Retention(RUNTIME)
@Constraint(validatedBy = MatchesPolicyValidator.class)
public @interface MatchesPolicy {
    String message() default "{validation.password.matchesPolicy}";
    Class<?>[] groups() default { };
    Class<? extends Payload>[] payload() default { };
}
