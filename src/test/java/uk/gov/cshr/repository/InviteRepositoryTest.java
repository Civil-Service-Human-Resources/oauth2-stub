package uk.gov.cshr.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.domain.Invite;

import javax.transaction.Transactional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class InviteRepositoryTest {

    @Autowired
    private InviteRepository inviteRepository;

    @Test
    public void findByForEmailShouldReturnCorrectInvite() {
        Invite invite = createInvite();

        inviteRepository.save(invite);

        Invite actualInvite = inviteRepository.findByForEmail("test@example.org");

        assertThat(actualInvite.getCode(), equalTo("123abc"));
        assertThat(actualInvite.getForEmail(), equalTo("test@example.org"));

    }

    @Test
    public void findByCodeShouldReturnCorrectInvite() {
        Invite invite = createInvite();

        inviteRepository.save(invite);

        Invite actualInvite = inviteRepository.findByCode("123abc");

        assertThat(actualInvite.getCode(), equalTo("123abc"));
        assertThat(actualInvite.getForEmail(), equalTo("test@example.org"));

    }

    @Test
    public void inviteShouldNotExistByCodeIfNotPresent() {
        assertThat(inviteRepository.existsByCode("123abc"), equalTo(false));
    }

    @Test
    public void inviteShouldExistByCode() {
        Invite invite = createInvite();

        inviteRepository.save(invite);

        assertThat(inviteRepository.existsByCode("123abc"), equalTo(true));
    }

    private Invite createInvite() {
        return createInvite("123abc", "test@example.org");
    }

    private Invite createInvite(String code, String forEmail) {
        Invite invite = new Invite();
        invite.setCode(code);
        invite.setForEmail(forEmail);

        return invite;
    }
}