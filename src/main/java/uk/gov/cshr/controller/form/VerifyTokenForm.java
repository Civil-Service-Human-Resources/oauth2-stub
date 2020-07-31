package uk.gov.cshr.controller.form;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Data
@ToString
public class VerifyTokenForm {
    @NotBlank(message = "{validation.emailUpdatedEnterToken.organisation.NotBlank}")
    private String organisation;
    @NotBlank(message = "{validation.emailUpdatedEnterToken.token.NotBlank}")
    private String token;
    private String uid;
    private String code;
}
