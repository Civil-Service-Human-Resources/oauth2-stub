package uk.gov.cshr.controller.signup;

import lombok.Data;

@Data
public class UpdateTokenForm {
    private String organisation;
    private String token;
    private boolean isRemoveUser;
}
