package uk.gov.cshr.controller.form;

import lombok.Data;
import uk.gov.cshr.validation.annotation.FieldMatch;
import uk.gov.cshr.validation.annotations.Whitelisted;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@FieldMatch(first = "email", second = "confirm", message = "{validation.updateEmail.email.FieldMatch}")
public class UpdateEmailForm {
    @NotBlank(message = "{validation.updateEmail.email.NotBlank}")
    @Email(message = "{validation.updateEmail.email.Email}")
    private String email;

    @NotBlank(message = "{validation.updateEmail.confirm.NotBlank}")
    private String confirm;
}