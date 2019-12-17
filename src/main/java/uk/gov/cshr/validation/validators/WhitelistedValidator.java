package uk.gov.cshr.validation.validators;

import org.springframework.stereotype.Component;
import uk.gov.cshr.validation.annotations.Whitelisted;
import uk.gov.cshr.repository.WhiteListRepository;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class WhitelistedValidator implements ConstraintValidator<Whitelisted, String> {
    private final WhiteListRepository whitelistRepository;
    public WhitelistedValidator(WhiteListRepository whitelistRepository) {
        this.whitelistRepository = whitelistRepository;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        String domain = value.substring(value.indexOf('@') + 1);
       return this.whitelistRepository.existsByDomain(domain);
    }
}
