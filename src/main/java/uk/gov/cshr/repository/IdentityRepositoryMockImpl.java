package uk.gov.cshr.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.Identity;
import uk.gov.cshr.dto.IdentityDTO;

import java.util.List;
import java.util.Optional;

@Slf4j
@Profile({"runMocks"})
@Repository
public class IdentityRepositoryMockImpl implements IdentityRepository {
    @Override
    public Identity findFirstByActiveTrueAndEmailEquals(String email) {
        return null;
    }

    @Override
    public Identity findFirstByEmailEquals(String email) {
        return null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return false;
    }

    @Override
    public Optional<Identity> findFirstByUid(String uid) {
        return Optional.empty();
    }

    @Override
    public List<IdentityDTO> findAllNormalised() {
        return null;
    }

    @Override
    public Long countByAgencyTokenUid(String name) {
        return null;
    }

    @Override
    public List<Identity> findAll() {
        return null;
    }

    @Override
    public List<Identity> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<Identity> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<Identity> findAllById(Iterable<Long> longs) {
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
    public void delete(Identity entity) {

    }

    @Override
    public void deleteAll(Iterable<? extends Identity> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public <S extends Identity> S save(S entity) {
        log.info("pretend to save this identity");
        return entity;
    }

    @Override
    public <S extends Identity> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<Identity> findById(Long aLong) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public void flush() {

    }

    @Override
    public <S extends Identity> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public void deleteInBatch(Iterable<Identity> entities) {

    }

    @Override
    public void deleteAllInBatch() {

    }

    @Override
    public Identity getOne(Long aLong) {
        return null;
    }

    @Override
    public <S extends Identity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Identity> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends Identity> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends Identity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Identity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Identity> boolean exists(Example<S> example) {
        return false;
    }
}
