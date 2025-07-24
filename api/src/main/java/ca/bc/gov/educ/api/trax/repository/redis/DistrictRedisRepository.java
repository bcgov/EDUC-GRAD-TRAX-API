package ca.bc.gov.educ.api.trax.repository.redis;

import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictEntity;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DistrictRedisRepository extends KeyValueRepository<DistrictEntity, String> {
    String HASH_KEY = "District";

    Optional<DistrictEntity> findByDistrictNumber(String districtNumber);
}
