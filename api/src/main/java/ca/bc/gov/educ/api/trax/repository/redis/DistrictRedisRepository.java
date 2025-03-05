package ca.bc.gov.educ.api.trax.repository.redis;

import ca.bc.gov.educ.api.trax.model.entity.institute.DistrictEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DistrictRedisRepository extends CrudRepository<DistrictEntity, String> {
    String HASH_KEY = "District";

    Optional<DistrictEntity> findByDistrictNumber(String districtNumber);
}
