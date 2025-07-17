package ca.bc.gov.educ.api.trax.repository.redis;

import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolCategoryCodeEntity;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolCategoryCodeRedisRepository extends KeyValueRepository<SchoolCategoryCodeEntity, String> {
    String HASH_KEY = "SchoolCategoryCode";
}