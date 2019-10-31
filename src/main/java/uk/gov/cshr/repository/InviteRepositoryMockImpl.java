package uk.gov.cshr.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;
import uk.gov.cshr.domain.Role;

import java.util.*;

/*
    Class to allow for testing on various sign up flows, such as Agency Self Sign Up.
    This removes the need to have a valid invite which is only achieved by signing up with a specific email,
    then copying the the url in the email from gov notify which has a random code in it.
 */
@Profile({"runMocks"})
@Repository
public class InviteRepositoryMockImpl implements InviteRepository {

    private Invite mockInvite;

    private Map<String, String> mockedUsers;

    public InviteRepositoryMockImpl() {
        // build mocked user map
        mockedUsers = new HashMap<String, String>();
        populateMockedUsers();
        // save a pending invite
        mockInvite = buildMockInvite();
    }

    private void populateMockedUsers() {
        // note code must be 16 in length, email can be anything
        mockedUsers.put("joebloggsatpeo16", "joebloggs@peoplemakeglasgow.scot");
        mockedUsers.put("joebloggsatnhs16", "joebloggs@nhsglasgow.gov.uk");
        mockedUsers.put("joebloggsatgla16", "joebloggs@glasgownhs.gov.uk");
    }

    @Override
    public Invite findByForEmail(String forEmail) {
        return mockInvite;
    }

    @Override
    public Invite findByCode(String code) {
        // ensure an invite with the right domain and email address is returned for the mocks.
        // hack to get this to work is to use a certain code in the url, which is then passed to this method.
        String emailAddress = workOutEmailAddressFromCode(code);
        // update the invite, so next bit of code works
        mockInvite.setCode(code);
        mockInvite.setForEmail(emailAddress);
        Set<Role> roles = new HashSet<Role>();
        roles.add(new Role());
        mockInvite.setForRoles(roles);
        return mockInvite;
    }

    @Override
    public boolean existsByCode(String code) {
        return true;
    }

    @Override
    public boolean existsByForEmailAndStatus(String email, InviteStatus status) {
        return false;
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

    private String workOutEmailAddressFromCode(String code) {
        return mockedUsers.get(code);
    }

}
