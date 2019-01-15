package uk.gov.cshr.validation.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.cshr.validation.annotation.MatchesPolicy;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class MatchesPolicyValidator implements ConstraintValidator<MatchesPolicy, String> {
   private final String passwordPattern;

   public MatchesPolicyValidator(@Value("${accountValidation.passwordPattern}") String passwordPattern) {
      this.passwordPattern = passwordPattern;
   }

   public void initialize(MatchesPolicy constraint) {
   }

   public boolean isValid(String value, ConstraintValidatorContext context) {
      return value.matches(passwordPattern);
   }
}
