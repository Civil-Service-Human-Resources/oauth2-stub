package uk.gov.cshr.controller.signup;

import lombok.Data;
import uk.gov.cshr.validation.annotations.FieldMatch;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@FieldMatch(first = "email", second = "confirmEmail", message = "{validation.confirmEmail.match}")
@Data
public class RequestInviteForm {
    @Email(message = "{validation.email.invalid}")
    private String email;

    @NotBlank(message = "{validation.confirmEmail.blank}")
    private String confirmEmail;
}
