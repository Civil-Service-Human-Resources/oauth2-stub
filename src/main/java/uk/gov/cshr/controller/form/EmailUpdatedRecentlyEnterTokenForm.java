package uk.gov.cshr.controller.form;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.validation.constraints.NotBlank;

@Data
public class EmailUpdatedRecentlyEnterTokenForm {
    @NotBlank(message = "{validation.emailUpdatedEnterToken.organisation.NotBlank}")
    private String organisation;
    @NotBlank(message = "{validation.emailUpdatedEnterToken.token.NotBlank}")
    private String token;
    private String domain;
    private String uid;
    private String code;

    public String toString() {
        return new ToStringBuilder(this).
                append("organisation", organisation).
                append("token", token).
                append("domain", domain).
                append("uid", uid).
                append("code", code).
                toString();
    }
}
