package uk.gov.cshr.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.domain.Identity;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class IdentityRepositoryTest {

    public static final String EMAIL_TEMPLATE = "%s@example.org";
    public static final String PASSWORD = "password123";

    @Autowired
    private IdentityRepository identityRepository;

    @Test
    public void findByForEmailShouldReturnCorrectInvite() {
        Identity identity = createIdentity();
        identityRepository.save(identity);

        assertThat(identityRepository.existsByEmail(identity.getEmail()), equalTo(true));
        assertThat(identityRepository.existsByEmail("doesntexist@example.com"), equalTo(false));

    }

    @Test
    public void removeAgencyToken_shouldRemoveAgencyTokenAndSetInactiveOnSingleMatch() {

        Identity originalIdentity = createIdentity(UUID.randomUUID().toString());
        Identity otherAgencyTokenIdentity = createIdentity(UUID.randomUUID().toString());
        Identity otherNonAgencyIdentity = createIdentity();

        identityRepository.saveAndFlush(originalIdentity);
        identityRepository.saveAndFlush(otherAgencyTokenIdentity);
        identityRepository.saveAndFlush(otherNonAgencyIdentity);

        identityRepository.removeAgencyToken(originalIdentity.getAgencyTokenUid());

        Identity updatedIdentity = identityRepository.getOne(originalIdentity.getId());
        Identity postUpdateOtherAgencyTokenIdentity = identityRepository.getOne(otherAgencyTokenIdentity.getId());
        Identity postUpdateOtherNonAgencyIdentity = identityRepository.getOne(otherNonAgencyIdentity.getId());

        assertTrue(updatedIdentity.isActive());
        assertNull(updatedIdentity.getAgencyTokenUid());

        assertEquals(otherAgencyTokenIdentity.toString(), postUpdateOtherAgencyTokenIdentity.toString());
        assertEquals(otherNonAgencyIdentity.toString(), postUpdateOtherNonAgencyIdentity.toString());
    }

    @Test
    public void removeAgencyToken_shouldRemoveAgencyTokenAndSetInactiveOnMultiMatch() {

        String agencyTokenUid = UUID.randomUUID().toString();

        Identity originalIdentityOne = createIdentity(agencyTokenUid);
        Identity originalIdentityTwo = createIdentity(agencyTokenUid);
        Identity otherAgencyTokenIdentity = createIdentity(UUID.randomUUID().toString());
        Identity otherNonAgencyIdentity = createIdentity();

        identityRepository.saveAndFlush(originalIdentityOne);
        identityRepository.saveAndFlush(originalIdentityTwo);
        identityRepository.saveAndFlush(otherAgencyTokenIdentity);
        identityRepository.saveAndFlush(otherNonAgencyIdentity);

        identityRepository.removeAgencyToken(agencyTokenUid);

        Identity updatedIdentityOne = identityRepository.getOne(originalIdentityOne.getId());
        Identity updatedIdentityTwo = identityRepository.getOne(originalIdentityTwo.getId());
        Identity postUpdateOtherAgencyTokenIdentity = identityRepository.getOne(otherAgencyTokenIdentity.getId());
        Identity postUpdateOtherNonAgencyIdentity = identityRepository.getOne(otherNonAgencyIdentity.getId());

        assertTrue(updatedIdentityOne.isActive());
        assertNull(updatedIdentityOne.getAgencyTokenUid());

        assertTrue(updatedIdentityTwo.isActive());
        assertNull(updatedIdentityTwo.getAgencyTokenUid());

        assertEquals(otherAgencyTokenIdentity.toString(), postUpdateOtherAgencyTokenIdentity.toString());
        assertEquals(otherNonAgencyIdentity.toString(), postUpdateOtherNonAgencyIdentity.toString());
    }

    @Test
    public void removeAgencyToken_doesNotSetNonAgencyTokenIdentityToInactiveOnNullToken() {
        Identity originalIdentity = createIdentity(UUID.randomUUID().toString());
        Identity otherAgencyTokenIdentity = createIdentity(UUID.randomUUID().toString());
        Identity otherNonAgencyIdentity = createIdentity();

        identityRepository.saveAndFlush(originalIdentity);
        identityRepository.saveAndFlush(otherAgencyTokenIdentity);
        identityRepository.saveAndFlush(otherNonAgencyIdentity);

        identityRepository.removeAgencyToken(null);

        Identity updatedIdentity = identityRepository.getOne(originalIdentity.getId());
        Identity postUpdateOtherAgencyTokenIdentity = identityRepository.getOne(otherAgencyTokenIdentity.getId());
        Identity postUpdateOtherNonAgencyIdentity = identityRepository.getOne(otherNonAgencyIdentity.getId());

        assertEquals(originalIdentity.toString(), updatedIdentity.toString());
        assertEquals(otherAgencyTokenIdentity.toString(), postUpdateOtherAgencyTokenIdentity.toString());
        assertEquals(otherNonAgencyIdentity.toString(), postUpdateOtherNonAgencyIdentity.toString());
    }

    private Identity createIdentity() {
        return createIdentity(null);
    }

    private Identity createIdentity(String agencyTokenUid) {
        Identity identity = new Identity(UUID.randomUUID().toString(), String.format(EMAIL_TEMPLATE, UUID.randomUUID().toString()), PASSWORD, true, false, null, Instant.now(), false, false, agencyTokenUid);
        return identity;
    }
}