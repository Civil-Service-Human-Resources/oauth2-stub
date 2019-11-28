package uk.gov.cshr.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.service.security.IdentityDetails;

import java.time.Instant;

import static java.util.Collections.emptySet;

@TestConfiguration
public class SpringSecurityTestConfig {

    @Bean(name ="userDetailsService")
    @Primary
    public UserDetailsService fakeUserDetailsService() {
        Identity basicIdentity = new Identity("uid", "emailaddress", "password", true, false, emptySet(), Instant.now(), false, false);
        IdentityDetails basicActiveUser = new IdentityDetails(basicIdentity);

        Identity recentlyUpdatedTheirEmailIdentity = new Identity("specialuid", "specialemailaddress", "password", true, false, emptySet(), Instant.now(), false, true);
        IdentityDetails recentlyUpdatedTheirEmailActiveUser = new IdentityDetails(recentlyUpdatedTheirEmailIdentity);

        return new UserDetailsManager() {
            @Override
            public void createUser(UserDetails user) {

            }

            @Override
            public void updateUser(UserDetails user) {

            }

            @Override
            public void deleteUser(String username) {

            }

            @Override
            public void changePassword(String oldPassword, String newPassword) {

            }

            @Override
            public boolean userExists(String username) {
                return false;
            }

            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                if(basicActiveUser.getUsername().equals(username)) {
                    return basicActiveUser;
                }
                if(recentlyUpdatedTheirEmailActiveUser.getUsername().equals(username)) {
                    return recentlyUpdatedTheirEmailActiveUser;
                }
                throw new UsernameNotFoundException("No user found with email address " + username);
            }
        };

    }

}