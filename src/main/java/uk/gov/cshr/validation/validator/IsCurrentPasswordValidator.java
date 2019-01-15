package uk.gov.cshr.validation.validator;

import org.springframework.stereotype.Component;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.AuthenticationDetailsService;
import uk.gov.cshr.service.security.IdentityService;
import uk.gov.cshr.validation.annotation.IsCurrentPassword;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class IsCurrentPasswordValidator implements ConstraintValidator<IsCurrentPassword, String> {
   private final IdentityService identityService;
   private final AuthenticationDetailsService authenticationDetailsService;

   public IsCurrentPasswordValidator(IdentityService identityService, AuthenticationDetailsService authenticationDetailsService) {
      this.identityService = identityService;
      this.authenticationDetailsService = authenticationDetailsService;
   }

   public void initialize(IsCurrentPassword constraint) {
   }

   public boolean isValid(String value, ConstraintValidatorContext context) {
      Identity identity = authenticationDetailsService.getCurrentIdentity();

      return identityService.checkPassword(identity.getEmail(), value);
   }
}
