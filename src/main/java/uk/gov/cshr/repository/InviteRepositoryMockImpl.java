package uk.gov.cshr.repository;

import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Invite;
import uk.gov.cshr.domain.InviteStatus;

import java.util.Optional;

@Repository
public class InviteRepositoryMockImpl implements InviteRepository {

    @Override
    public Invite findByForEmail(String forEmail) {
        return null;
    }

    @Override
    public Invite findByCode(String code) {
        return null;
    }

    @Override
    public boolean existsByCode(String code) {
        return false;
    }

    @Override
    public boolean existsByForEmailAndStatus(String email, InviteStatus status) {
        return false;
    }

    @Override
    public <S extends Invite> S save(S entity) {
        return null;
    }

    @Override
    public <S extends Invite> Iterable<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<Invite> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
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
        return 0;
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
}
