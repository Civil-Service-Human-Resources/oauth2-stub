package uk.gov.cshr.validation.validator;

import org.junit.Test;

import javax.validation.ConstraintValidatorContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class MatchesPolicyValidatorTest {
    /*
        - 8 or more characters
        - at least 1 number or symbol
        - upper and lower case letters
     */

    private final String passwordPattern = "^(?:(?=.*[a-z])(?:(?=.*[A-Z])(?=.*[\\d\\W]))).{8,}$";

    private final MatchesPolicyValidator validator = new MatchesPolicyValidator(passwordPattern);

    @Test
    public void shouldMatchPolicy() {
        String password = "Abcdefg!";
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        assertTrue(validator.isValid(password, context));
    }

    @Test
    public void shouldFailValidationIfDoesntSatisfyPolicy() {
        String password = "password";
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        assertFalse(validator.isValid(password, context));
    }

}