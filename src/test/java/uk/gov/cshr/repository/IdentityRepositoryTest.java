package uk.gov.cshr.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.cshr.domain.Identity;

import javax.transaction.Transactional;

import java.time.Instant;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class IdentityRepositoryTest {

    public static final String EMAIL = "test@example.org";
    public static final String PASSWORD = "password123";
    public static final String UID = "abc123";
    @Autowired
    private IdentityRepository identityRepository;

    @Test
    public void findByForEmailShouldReturnCorrectInvite() {
        Identity identity = createIdentity();

        identityRepository.save(identity);

        assertThat(identityRepository.existsByEmail(EMAIL), equalTo(true));
        assertThat(identityRepository.existsByEmail("doesntexist@example.com"), equalTo(false));

    }

    private Identity createIdentity() {
        Identity identity = new Identity(UID, EMAIL, PASSWORD, true, false, null, Instant.now(), false);
        return identity;
    }
}