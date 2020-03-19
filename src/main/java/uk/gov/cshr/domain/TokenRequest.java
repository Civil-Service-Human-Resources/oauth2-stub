package uk.gov.cshr.domain;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenRequest implements Serializable {

    private static long serialVersionUID = 1l;

    private String domain;
    private String token;
    private String org;

}
