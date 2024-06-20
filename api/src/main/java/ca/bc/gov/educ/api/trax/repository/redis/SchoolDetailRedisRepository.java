package ca.bc.gov.educ.api.trax.repository.redis;

import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolDetailEntity;
import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolDetailRedisRepository extends CrudRepository<SchoolDetailEntity, String> {
    String HASH_KEY = "SchoolDetail";
}