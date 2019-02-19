package uk.gov.cshr.service;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.factory.InviteFactory;
import uk.gov.cshr.repository.InviteRepository;
import uk.gov.service.notify.NotificationClientException;

import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.*;

public class InviteServiceTest {

    private InviteRepository inviteRepository = mock(InviteRepository.class);
    private InviteFactory inviteFactory = mock(InviteFactory.class);
    private NotifyService notifyService = mock(NotifyService.class);
    private final String govNotifyTemplateId = "template-id";
    private final int validityInSeconds = 30;
    private final String signupUrlFormat = "invite-url";

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

        ArgumentCaptor<Invite> inviteArgumentCaptor = ArgumentCaptor.forClass(Invite.class);

        verify(inviteRepository).save(inviteArgumentCaptor.capture());

        invite = inviteArgumentCaptor.getValue();
        MatcherAssert.assertThat(invite.getCode(), equalTo(code));
        MatcherAssert.assertThat(invite.getStatus(), equalTo(InviteStatus.ACCEPTED));
    }

    @Test
    public void shouldSendAndSaveSelfSignupInvite() throws NotificationClientException {
        String email = "use@domain.org";
        String code = "invite-code";
        Identity identity = new Identity();
        Invite invite = new Invite();
        invite.setForEmail(email);
        invite.setCode(code);
        when(inviteFactory.createSelfSignUpInvite(email, identity)).thenReturn(invite);

        inviteService.sendSelfSignupInvite(email, identity);

        verify(notifyService).notify(email, code, govNotifyTemplateId, signupUrlFormat);
        verify(inviteRepository).save(invite);
    }
}