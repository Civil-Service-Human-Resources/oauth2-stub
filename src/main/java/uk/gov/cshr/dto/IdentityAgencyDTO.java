package uk.gov.cshr.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class IdentityAgencyDTO {

    private String uid;
    private String agencyTokenUid;

    public IdentityAgencyDTO(String uid, String agencyTokenUid) {
        this.uid = uid;
        this.agencyTokenUid = agencyTokenUid;
    }

}
