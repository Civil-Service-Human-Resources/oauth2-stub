package uk.gov.cshr.validation.validator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.AuthenticationDetailsService;
import uk.gov.cshr.service.security.IdentityDetails;
import uk.gov.cshr.service.security.IdentityService;

import javax.validation.ConstraintValidatorContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IsCurrentPasswordValidatorTest {

    @Mock
    private IdentityService identityService;

    @Mock
    private AuthenticationDetailsService authenticationDetailsService;

    @InjectMocks
    private IsCurrentPasswordValidator validator;

    @Test
    public void shouldReturnTrueIfPasswordMatchesCurrentPassword() {
        String value = "password";
        String email = "learner@domain.com";

        Identity identity = mock(Identity.class);
        when(identity.getEmail()).thenReturn(email);
        when(authenticationDetailsService.getCurrentIdentity()).thenReturn(identity);

        ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);

        when(identityService.checkPassword(email, value)).thenReturn(true);

        assertTrue(validator.isValid(value, constraintValidatorContext));
    }

    @Test
    public void shouldReturnFalseIfPasswordDoesNotMatchCurrentPassword() {
        String value = "password";
        String email = "learner@domain.com";

        Identity identity = mock(Identity.class);
        when(identity.getEmail()).thenReturn(email);
        when(authenticationDetailsService.getCurrentIdentity()).thenReturn(identity);

        ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);

        when(identityService.checkPassword(email, value)).thenReturn(false);

        assertFalse(validator.isValid(value, constraintValidatorContext));
    }

}