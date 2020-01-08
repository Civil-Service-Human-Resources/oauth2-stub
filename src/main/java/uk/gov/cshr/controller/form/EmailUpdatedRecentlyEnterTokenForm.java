package uk.gov.cshr.controller.form;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class EmailUpdatedRecentlyEnterTokenForm {
    @NotBlank(message = "{validation.emailUpdatedEnterToken.organisation.NotBlank}")
    private String organisation;
    @NotBlank(message = "{validation.emailUpdatedEnterToken.token.NotBlank}")
    private String token;
    private String domain;
    private String uid;
}
