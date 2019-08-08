package uk.gov.cshr.controller.signup;

import lombok.Data;

@Data
public class SignupForm {
    private String password;
    private String confirmPassword;
    private String organisation;
    private String token;
}
