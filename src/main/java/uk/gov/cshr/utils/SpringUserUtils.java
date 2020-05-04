package uk.gov.cshr.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.security.IdentityDetails;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class SpringUserUtils {

    public Identity getIdentityFromSpringAuthentication () {
        Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
        IdentityDetails existingIdentityDetails = (IdentityDetails) existingAuth.getPrincipal();
        return existingIdentityDetails.getIdentity();
    }

    public void updateSpringAuthenticationAndSpringSessionWithUpdatedIdentity(HttpServletRequest request, Identity updatedIdentity) {
        IdentityDetails updatedIdentityDetails = new IdentityDetails(updatedIdentity);
        Authentication updatedAuthentication = new UsernamePasswordAuthenticationToken(updatedIdentityDetails, updatedIdentityDetails.getPassword(), updatedIdentityDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
        log.debug("spring session successfully updated");
    }

}
