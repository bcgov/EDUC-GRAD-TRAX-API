package ca.bc.gov.educ.api.trax.repository.redis;

import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolFundingGroupCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolFundingGroupCodeRedisRepository extends JpaRepository<SchoolFundingGroupCodeEntity, String> {
    String HASH_KEY = "SchoolFundingGroupCode";
}