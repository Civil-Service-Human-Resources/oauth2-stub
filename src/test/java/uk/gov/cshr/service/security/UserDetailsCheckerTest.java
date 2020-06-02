package uk.gov.cshr.service.security;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.exception.AccountBlockedException;
import uk.gov.cshr.service.CsrsService;
import uk.gov.cshr.service.InviteService;

import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserDetailsCheckerTest {

    private static final String EMAIL_ADDRESS = "test@example.com";
    private static final String DOMAIN = "example.com";
    private static final String UID = "UID";

    @Mock
    private IdentityService identityService;

    @Mock
    private CsrsService csrsService;

    @Mock
    private InviteService inviteService;

    @InjectMocks
    private UserDetailsChecker userDetailsChecker;

    @Test
    public void shouldNotThrowExceptionIfEmailIsWhitelisted() {
        Identity identity = new Identity();
        identity.setLocked(false);
        identity.setEmail(EMAIL_ADDRESS);
        UserDetails userDetails = new IdentityDetails(identity);

        when(identityService.getDomainFromEmailAddress(EMAIL_ADDRESS)).thenReturn(DOMAIN);
        when(identityService.isWhitelistedDomain(DOMAIN)).thenReturn(true);

        Assertions.assertThatCode(() -> userDetailsChecker.check(userDetails))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldNotThrowExceptionIfEmailIsInAgency() {
        Identity identity = new Identity();
        identity.setLocked(false);
        identity.setEmail(EMAIL_ADDRESS);
        identity.setAgencyTokenUid(UID);
        UserDetails userDetails = new IdentityDetails(identity);

        when(identityService.getDomainFromEmailAddress(EMAIL_ADDRESS)).thenReturn(DOMAIN);
        when(identityService.isWhitelistedDomain(DOMAIN)).thenReturn(false);
        when(csrsService.isDomainInAgency(DOMAIN)).thenReturn(true);

        Assertions.assertThatCode(() -> userDetailsChecker.check(userDetails))
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldNotThrowExceptionIfEmailIsInvited() {
        Identity identity = new Identity();
        identity.setLocked(false);
        identity.setEmail(EMAIL_ADDRESS);
        UserDetails userDetails = new IdentityDetails(identity);

        when(identityService.getDomainFromEmailAddress(EMAIL_ADDRESS)).thenReturn(DOMAIN);
        when(identityService.isWhitelistedDomain(DOMAIN)).thenReturn(false);
        when(csrsService.isDomainInAgency(DOMAIN)).thenReturn(false);
        when(inviteService.isEmailInvited(EMAIL_ADDRESS)).thenReturn(true);

        Assertions.assertThatCode(() -> userDetailsChecker.check(userDetails))
                .doesNotThrowAnyException();
    }

    @Test(expected = AccountBlockedException.class)
    public void shouldThrowExceptionIfEmailIsNotWhitelistedOrAgencyOrInvited() {
        Identity identity = new Identity();
        identity.setLocked(false);
        identity.setEmail(EMAIL_ADDRESS);
        UserDetails userDetails = new IdentityDetails(identity);

        when(identityService.getDomainFromEmailAddress(EMAIL_ADDRESS)).thenReturn(DOMAIN);
        when(identityService.isWhitelistedDomain(DOMAIN)).thenReturn(false);
        when(csrsService.isDomainInAgency(DOMAIN)).thenReturn(false);
        when(inviteService.isEmailInvited(EMAIL_ADDRESS)).thenReturn(false);

        userDetailsChecker.check(userDetails);
    }
}
