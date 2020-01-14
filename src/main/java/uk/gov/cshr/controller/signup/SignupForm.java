package uk.gov.cshr.controller.signup;

import lombok.Data;
import uk.gov.cshr.validation.annotation.FieldMatch;

import javax.validation.constraints.NotBlank;

@Data
@FieldMatch(first = "password", second = "confirmPassword", message = "{validation.signup.confirmPassword.FieldMatch}")
public class SignupForm {
    @NotBlank(message = "{validation.signup.password.NotBlank}")
    private String password;
    @NotBlank(message = "{validation.signup.password.NotBlank}")
    private String confirmPassword;
}
