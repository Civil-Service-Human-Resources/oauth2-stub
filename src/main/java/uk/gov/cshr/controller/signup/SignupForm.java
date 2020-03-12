package uk.gov.cshr.controller.signup;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import lombok.Data;
import uk.gov.cshr.validation.annotation.FieldMatch;

@Data
@FieldMatch(first = "password", second = "confirmPassword", message = "{validation.signup.password.NotMatching}")
public class SignupForm {

    @NotBlank(message = "{validation.signup.password.NotBlank}")
    @Pattern(regexp = "^(?:(?=.*[a-z])(?:(?=.*[A-Z])(?=.*[\\d\\W]))).{8,}$", message = "{validation.signup.password.MatchesPolicy}")
    private String password;

    @NotBlank(message = "{validation.signup.password.NotBlank}")
    @Pattern(regexp = "^(?:(?=.*[a-z])(?:(?=.*[A-Z])(?=.*[\\d\\W]))).{8,}$", message = "{validation.signup.password.MatchesPolicy}")
    private String confirmPassword;
}
