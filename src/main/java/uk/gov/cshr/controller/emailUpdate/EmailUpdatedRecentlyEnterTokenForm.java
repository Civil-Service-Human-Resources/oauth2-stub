package uk.gov.cshr.controller.emailUpdate;

import lombok.Data;

@Data
public class EmailUpdatedRecentlyEnterTokenForm {
    private String organisation;
    private String token;
    private String domain;
    private String uid;
}
