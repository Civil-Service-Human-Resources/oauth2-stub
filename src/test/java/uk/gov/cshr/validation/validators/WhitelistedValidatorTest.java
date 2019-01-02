package uk.gov.cshr.validation.validators;

import org.junit.Test;

import javax.validation.ConstraintValidatorContext;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class WhitelistedValidatorTest {
    private String[] whitelistedDomains = {"example.org", "domain.org"};

    private WhitelistedValidator validator = new WhitelistedValidator(whitelistedDomains);

    private ConstraintValidatorContext constraintValidatorContext = mock(ConstraintValidatorContext.class);

    @Test
    public void shouldReturnTrueIfValueHasWhitelistedDomain() {
        assertTrue(validator.isValid("user@domain.org", constraintValidatorContext));
        assertTrue(validator.isValid("user@example.org", constraintValidatorContext));
    }

    @Test
    public void shouldReturnFalseIfValueDoesNotHaveWhitelistedDomain() {
        assertFalse(validator.isValid("user@not-in-whitelist.org", constraintValidatorContext));
    }
}