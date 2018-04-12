package uk.gov.cshr.service.security;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.repository.IdentityRepository;

import static java.util.Collections.emptySet;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class IdentityServiceTest {

    @InjectMocks
    private IdentityService identityService;

    @Mock
    private IdentityRepository identityRepository;

    @Test
    public void shouldLoadIdentityByEmailAddress() {

        final String emailAddress = "test@example.org";
        final Identity identity = new Identity("uid", emailAddress, "password", true, emptySet());

        when(identityRepository.findFirstByActiveTrueAndEmailEquals(emailAddress))
                .thenReturn(identity);

        IdentityDetails identityDetails = (IdentityDetails) identityService.loadUserByUsername(emailAddress);

        assertThat(identityDetails, notNullValue());
        assertThat(identityDetails.getUsername(), equalTo(emailAddress));
        assertThat(identityDetails.getIdentity(), equalTo(identity));
    }

    @Test(expected = UsernameNotFoundException.class)
    public void shouldThrowErrorWhenNoClientFound() {

        final String emailAddress = "test@example.org";

        when(identityRepository.findFirstByActiveTrueAndEmailEquals(emailAddress))
                .thenReturn(null);

        identityService.loadUserByUsername(emailAddress);
    }
}
