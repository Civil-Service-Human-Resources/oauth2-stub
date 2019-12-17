package uk.gov.cshr.controller.signup;

import lombok.Data;
import uk.gov.cshr.validation.annotations.FieldMatch;
import uk.gov.cshr.validation.annotations.Whitelisted;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


// Check if email address is in a valid format and not blank

@FieldMatch(first = "email", second = "confirmEmail", message = "{validation.confirmEmail.match}")
@Data
public class RequestInviteForm {
    @Email(message = "{validation.email.invalid}")
    @Whitelisted
    private String email;

    @NotBlank(message = "{validation.confirmEmail.blank}")
    private String confirmEmail;
}
