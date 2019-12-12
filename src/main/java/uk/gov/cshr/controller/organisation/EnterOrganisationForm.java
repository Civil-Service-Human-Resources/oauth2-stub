package uk.gov.cshr.controller.organisation;

import lombok.Data;

@Data
public class EnterOrganisationForm {
    private String organisation;
    private String domain;
    private String uid;
}
