package uk.gov.cshr.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdatePasswordForm {
    @NotBlank(message = "{validation.updatePassword.password.required}")
    private String password;
    private String newPassword;
    private String confirm;
}
