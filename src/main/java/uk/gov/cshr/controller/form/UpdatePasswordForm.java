package uk.gov.cshr.controller.form;

import lombok.Data;
import uk.gov.cshr.validation.annotation.IsCurrentPassword;

import javax.validation.constraints.NotBlank;

@Data
public class UpdatePasswordForm {
    @NotBlank(message = "{validation.updatePassword.password.NotBlank}")
    @IsCurrentPassword(message = "{validation.updatePassword.password.IsCurrentPassword}")
    private String password;

    @NotBlank(message = "{validation.updatePassword.newPassword.NotBlank}")
    //@MatchesPolicy
    private String newPassword;
    private String confirm;
}
