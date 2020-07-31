package uk.gov.cshr.service.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.AccountBlockedException;
import uk.gov.cshr.exception.InvalidUserDetailsType;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.InviteService;

@Component
public class UserDetailsChecker extends AccountStatusUserDetailsChecker {

    @Autowired
    private IdentityService identityService;

    @Autowired
    private CsrsService csrsService;

    @Autowired
    private InviteService inviteService;

    @Override
    public void check(UserDetails user) {
        super.check(user);

        if (user instanceof IdentityDetails) {
            IdentityDetails userDetails = (IdentityDetails) user;
            Identity identity = userDetails.getIdentity();
            String email = identity.getEmail();
            final String domain = identityService.getDomainFromEmailAddress(email);

            if (!isWhitelistedDomain(domain) && !isAgencyDomain(domain, identity) && !isEmailInvited(email)) {
                throw new AccountBlockedException(messages.getMessage("UserDetailsChecker.blocked", "User account is blocked"));
            }

        } else {
            throw new InvalidUserDetailsType("Wrong user type received");
        }
    }

    private boolean isWhitelistedDomain(String domain) {
        return identityService.isWhitelistedDomain(domain);
    }

    private boolean isAgencyDomain(String domain, Identity identity) {
        return csrsService.isDomainInAgency(domain) && identity.getAgencyTokenUid() != null;
    }

    private boolean isEmailInvited(String email) {
        return inviteService.isEmailInvited(email);
    }
}
