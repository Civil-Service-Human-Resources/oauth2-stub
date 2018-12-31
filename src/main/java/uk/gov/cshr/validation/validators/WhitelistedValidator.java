package uk.gov.cshr.validation.validators;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.cshr.validation.annotations.Whitelisted;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

@Component
public class WhitelistedValidator implements ConstraintValidator<Whitelisted, String> {
    private final String[] whitelistedDomains;

    public WhitelistedValidator(@Value("${invite.whitelist.domains}") String[] whitelistedDomains) {
        this.whitelistedDomains = whitelistedDomains;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        String domain = value.substring(value.indexOf('@') + 1);

        return Arrays.asList(whitelistedDomains).contains(domain);
    }

}
