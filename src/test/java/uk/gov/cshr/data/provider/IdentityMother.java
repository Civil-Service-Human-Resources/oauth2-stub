package uk.gov.cshr.data.provider;

import org.springframework.stereotype.Service;
import uk.gov.cshr.domain.Identity;

import java.time.Instant;
import java.util.HashSet;

@Service
public class IdentityMother {

    public Identity provideIdentity(String uid, String email, String password) {
        return new Identity.Builder()
                .setUid(uid)
                .setEmail(email)
                .setPassword(password)
                .setActive(true)
                .setDeletionNotificationSent(false)
                .setFailedLoginAttempts(0L)
                .setLastLoggedIn(Instant.now())
                .setRoles(new HashSet())
                .setLocked(false)
                .build();
    }

    public Identity provideIdentity(String uid, String email) {
        return provideIdentity(uid, email, "password");
    }
}
