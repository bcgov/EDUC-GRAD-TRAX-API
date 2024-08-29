package ca.bc.gov.educ.api.trax.repository.redis;

import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolRedisRepository extends CrudRepository<SchoolEntity, String> {
    String HASH_KEY = "School";

    SchoolEntity findByMincode(String mincode);
}