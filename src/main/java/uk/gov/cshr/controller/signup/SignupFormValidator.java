package uk.gov.cshr.controller.signup;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class SignupFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return SignupForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignupForm form = (SignupForm) target;
        if (form.getConfirmPassword() == null || !form.getConfirmPassword().equals(form.getPassword())) {
            errors.rejectValue("confirmPassword", "{validation.confirmPassword}");
        }
    }
}