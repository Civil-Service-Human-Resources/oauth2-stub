package uk.gov.cshr.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.util.Assert;

public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {

    private UserDetailsService userDetailsService;

    @Autowired
    public WithMockCustomUserSecurityContextFactory(/*@Qualifier("userDetailsService") */UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser withUser) {
        String username = withUser.username();
        Assert.hasLength(username, "value() must be non empty String");
        UserDetails principal = userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, principal.getPassword(), principal.getAuthorities());
        //SecurityContext context = SecurityContextHolder.createEmptyContext();
       // SecurityContext context = SecurityContextHolder.getContext();//.setAuthentication(authentication);
        SecurityContext context = SecurityContextHolder.getContext();
        if(context == null){
            context = SecurityContextHolder.createEmptyContext();
        }
        context.setAuthentication(authentication);
        return context;
    }
}
