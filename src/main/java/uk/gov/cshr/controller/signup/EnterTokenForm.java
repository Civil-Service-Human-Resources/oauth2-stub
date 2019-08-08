package uk.gov.cshr.controller.signup;

import lombok.Data;

@Data
public class EnterTokenForm {
    private String organisation;
    private String token;
    private String emailAddress;
}
