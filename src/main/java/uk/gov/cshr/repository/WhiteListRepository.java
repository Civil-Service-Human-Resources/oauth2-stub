package uk.gov.cshr.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.WhiteList;

@Repository
public interface WhiteListRepository extends CrudRepository<WhiteList, Long> {

    boolean existsByDomain(String domain);

}
