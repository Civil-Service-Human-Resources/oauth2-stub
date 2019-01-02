package uk.gov.cshr.controller.signup;

import lombok.Data;

import javax.validation.constraints.Pattern;

@Data
public class SignupForm {
    private String password;
    private String confirmPassword;
}
