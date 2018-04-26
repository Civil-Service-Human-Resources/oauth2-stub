package uk.gov.cshr.controller.signup;

import uk.gov.cshr.domain.Identity;

import javax.validation.constraints.Pattern;


public class SignupForm {

    private String password;

    private String confirmPassword;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
