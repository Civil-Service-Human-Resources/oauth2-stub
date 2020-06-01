package uk.gov.cshr.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsChecker extends AccountStatusUserDetailsChecker {

    @Autowired
    private IdentityService identityService;

    @Override
    public void check(UserDetails user) {
        super.check(user);

        // if not (user is whitelisted) and  not (user is invited) and not (user has agency token)
        // throw auth exception (runtime)

        if (user instanceof IdentityDetails) {
            IdentityDetails userDetails = (IdentityDetails) user;
            userDetails.getIdentity().getId();

            if (!identityService.isWhitelistedDomain(identityService.getDomainFromEmailAddress(((IdentityDetails) user).getIdentity().getEmail()))) {
                throw new RuntimeException("Not whitelised");
            }

        } else {
            throw new RuntimeException("Wrong user type received");
        }
    }
}
