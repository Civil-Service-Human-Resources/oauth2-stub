package uk.gov.cshr.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;

import java.util.Optional;

/*
    Class to allow for testing on various sign up flows, such as Agency Self Sign Up.
    This removes the need to have a valid invite which is only achieved by signing up with a specific email,
    then copying the the url in the email from gov notify which has a random code in it.
 */
@Profile({"runMocks"})
@Repository
public class InviteRepositoryMockImpl implements InviteRepository {

    private Invite mockInvite;

    public InviteRepositoryMockImpl() {
        // save a pending invite
        mockInvite = buildMockInvite();
    }

    @Override
    public Invite findByForEmail(String forEmail) {
        return mockInvite;
    }

    @Override
    public Invite findByCode(String code) {
        return mockInvite;
    }

    @Override
    public boolean existsByCode(String code) {
        return true;
    }

    @Override
    public boolean existsByForEmailAndStatus(String email, InviteStatus status) {
        return true;
    }

    @Override
    public <S extends Invite> S save(S entity) {
        return entity;
    }

    @Override
    public <S extends Invite> Iterable<S> saveAll(Iterable<S> entities) {
        return entities;
    }

    @Override
    public Optional<Invite> findById(Long aLong) {
        return Optional.of(mockInvite);
    }

    @Override
    public boolean existsById(Long aLong) {
        return true;
    }

    @Override
    public Iterable<Invite> findAll() {
        return null;
    }

    @Override
    public Iterable<Invite> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 1;
    }

    @Override
    public void deleteById(Long aLong) {
    }

    @Override
    public void delete(Invite entity) {
    }

    @Override
    public void deleteAll(Iterable<? extends Invite> entities) {
    }

    @Override
    public void deleteAll() {
    }

    private Invite buildMockInvite() {
        Invite mockInvite = new Invite();
        mockInvite.setAuthorisedInvite(false);
        mockInvite.setCode("mockCode");
        mockInvite.setId(123);
        mockInvite.setForEmail("b.hodgson@kainos.com");
        mockInvite.setStatus(InviteStatus.PENDING);
        return mockInvite;
    }
}
