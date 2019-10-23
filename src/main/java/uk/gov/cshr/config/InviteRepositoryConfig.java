package uk.gov.cshr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.cshr.repository.InviteRepositoryMockImpl;

@Configuration
public class InviteRepositoryConfig {

    @Autowired
    private InviteRepository inviteRepository;

    @Profile({"mockInvite"})
    @Bean
    public InviteRepository mockInviteRepo() {
        return new InviteRepositoryMockImpl();
    }

    @Profile({"default"})
    @Bean
    public InviteRepository realInviteRepo() {
        return inviteRepository;
    }
}
