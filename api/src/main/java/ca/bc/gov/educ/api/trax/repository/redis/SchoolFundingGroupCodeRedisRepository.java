package ca.bc.gov.educ.api.trax.repository.redis;

import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolFundingGroupCodeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolFundingGroupCodeRedisRepository extends CrudRepository<SchoolFundingGroupCodeEntity, String> {
    String HASH_KEY = "SchoolFundingGroupCode";
}