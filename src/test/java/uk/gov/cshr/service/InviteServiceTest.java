package uk.gov.cshr.service;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.factory.InviteFactory;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.Date;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.*;

public class InviteServiceTest {

    private static final String EMAIL = "test@example.com";
    private final String govNotifyTemplateId = "template-id";
    private final int validityInSeconds = 259200;
    private final String signupUrlFormat = "invite-url";
    private InviteRepository inviteRepository = mock(InviteRepository.class);
    private InviteFactory inviteFactory = mock(InviteFactory.class);
    private NotifyService notifyService = mock(NotifyService.class);
    private InviteService inviteService = new InviteService(govNotifyTemplateId, validityInSeconds,
            signupUrlFormat, notifyService, inviteRepository, inviteFactory);

    @Test
    public void updateInviteByCodeShouldUpdateStatusCorrectly() {
        final String code = "123abc";

        Invite invite = new Invite();
        invite.setStatus(InviteStatus.PENDING);
        invite.setCode(code);

        when(inviteRepository.findByCode(code))
                .thenReturn(invite);

        inviteService.updateInviteByCode(code, InviteStatus.ACCEPTED);

        ArgumentCaptor<Invite> inviteArgumentCaptor = ArgumentCaptor.forClass(Invite.class);

        verify(inviteRepository).save(inviteArgumentCaptor.capture());

        invite = inviteArgumentCaptor.getValue();
        MatcherAssert.assertThat(invite.getCode(), equalTo(code));
        MatcherAssert.assertThat(invite.getStatus(), equalTo(InviteStatus.ACCEPTED));
    }

    @Test
    public void inviteCodesLessThan24HrsShouldNotBeExpired() {
        final String code = "123abc";

        Invite invite = new Invite();
        invite.setStatus(InviteStatus.PENDING);
        invite.setCode(code);
        invite.setInvitedAt(new Date(2323223232L));

        when(inviteRepository.findByCode(code))
                .thenReturn(invite);

        MatcherAssert.assertThat(inviteService.isCodeExpired(code), equalTo(true));
    }

    @Test
    public void inviteCodesOlderThanValidityDurationShouldBeExpired() {
        final String code = "123abc";

        Invite invite = new Invite();
        invite.setStatus(InviteStatus.PENDING);
        invite.setCode(code);
        invite.setInvitedAt(new Date(new Date().getTime() - 1000 * 60 * 60 * 24));

        when(inviteRepository.findByCode(code))
                .thenReturn(invite);

        MatcherAssert.assertThat(inviteService.isCodeExpired(code), equalTo(false));
    }

    @Test
    public void shouldSendAndSaveSelfSignupInvite() throws NotificationClientException {
        String email = "use@domain.org";
        String code = "invite-code";

        Invite invite = new Invite();
        invite.setForEmail(email);
        invite.setCode(code);
        when(inviteFactory.createSelfSignUpInvite(email)).thenReturn(invite);

        inviteService.sendSelfSignupInvite(email, true);

        verify(notifyService).notify(email, code, govNotifyTemplateId, signupUrlFormat);
        verify(inviteRepository).save(invite);
    }

    @Test
    public void shouldReturnTrueIfEmailInvited() {
        when(inviteRepository.existsByForEmailAndInviterIdIsNotNull(EMAIL)).thenReturn(true);
        assertTrue(inviteService.isEmailInvited(EMAIL));
    }

    @Test
    public void shouldReturnFalseIfEmailNotInvited() {
        when(inviteRepository.existsByForEmailAndInviterIdIsNotNull(EMAIL)).thenReturn(false);
        assertFalse(inviteService.isEmailInvited(EMAIL));
    }
}