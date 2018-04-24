package uk.gov.cshr.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.security.IdentityDetails;

@Component
public class AuthenticationDetailsService implements AuthenticationDetails {

    @Override
    public IdentityDetails getCurrentIdentityDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return ((IdentityDetails) authentication.getPrincipal());
    }

    @Override
    public Identity getCurrentIdentity() {
        return getCurrentIdentityDetails().getIdentity();
    }

    @Override
    public String getCurrentUsername() {
        return getCurrentIdentityDetails().getUsername();
    }
}