package uk.gov.cshr.controller.signup;

import lombok.Data;

import javax.validation.constraints.Pattern;

@Data
public class SignupForm {
    @Pattern(regexp = "(?!([a-zA-Z]*|[a-z\\d]*|[^A-Z\\d]*|[A-Z\\d]*|[^a-z\\d]*|[^a-zA-Z]*)$).{8,}", message = "{}")
    private String password;
    private String confirmPassword;
}
