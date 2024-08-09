package ca.bc.gov.educ.api.trax.repository.redis;

import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistrictRedisRepository extends CrudRepository<DistrictEntity, String> {
    String HASH_KEY = "District";

    DistrictEntity findByDistrictNumber(String districtNumber);
}
