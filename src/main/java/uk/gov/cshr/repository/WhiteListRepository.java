package uk.gov.cshr.repository;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.domain.WhiteList;



@Repository
public interface WhitleListRepository extends CrudRepository<WhiteList, Long> {

    WhiteList existsByCode(String code);

}
