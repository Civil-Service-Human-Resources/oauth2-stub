package uk.gov.cshr.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenRequest {

    private String domain;
    private String token;
    private String org;

}
