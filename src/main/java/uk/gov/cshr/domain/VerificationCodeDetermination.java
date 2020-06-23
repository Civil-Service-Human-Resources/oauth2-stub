package uk.gov.cshr.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class VerificationCodeDetermination {
    private String email;
    private VerificationCodeType verificationCodeType;

    public VerificationCodeDetermination(String email, VerificationCodeType verificationCodeType) {
        this.email = email;
        this.verificationCodeType = verificationCodeType;
    }
}
