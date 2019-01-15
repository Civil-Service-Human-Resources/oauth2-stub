package uk.gov.cshr.controller.form;

import lombok.Data;
import uk.gov.cshr.validation.annotation.FieldMatch;
import uk.gov.cshr.validation.annotation.IsCurrentPassword;
import uk.gov.cshr.validation.annotation.MatchesPolicy;

import javax.validation.constraints.NotBlank;

@FieldMatch(first = "newPassword", second = "confirm", message = "{validation.updatePassword.newPassword.FieldMatch}")
@Data
public class UpdatePasswordForm {
    @NotBlank(message = "{validation.updatePassword.password.NotBlank}")
    @IsCurrentPassword(message = "{validation.updatePassword.password.IsCurrentPassword}")
    private String password;

    @NotBlank(message = "{validation.updatePassword.newPassword.NotBlank}")
    @MatchesPolicy(message = "{validation.updatePassword.newPassword.MatchesPolicy}")
    private String newPassword;
    private String confirm;
}
