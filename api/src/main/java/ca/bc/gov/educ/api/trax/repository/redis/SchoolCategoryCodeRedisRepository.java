package ca.bc.gov.educ.api.trax.repository.redis;

import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolCategoryCodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolCategoryCodeRedisRepository extends CrudRepository<SchoolCategoryCodeEntity, String> {
    String HASH_KEY = "SchoolCategoryCode";
}