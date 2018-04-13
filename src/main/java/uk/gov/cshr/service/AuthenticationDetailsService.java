package uk.gov.cshr.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uk.gov.cshr.service.security.IdentityDetails;

@Component
public class AuthenticationDetailsService implements AuthenticationDetails {

    @Override
    public IdentityDetails getCurrentIdentity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return ((IdentityDetails) authentication.getPrincipal());
    }

    @Override
    public String getCurrentUsername() {
        return getCurrentIdentity().getUsername();
    }
}
