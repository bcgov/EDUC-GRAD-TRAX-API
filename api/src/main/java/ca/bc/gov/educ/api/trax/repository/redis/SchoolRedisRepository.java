package ca.bc.gov.educ.api.trax.repository.redis;

import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolRedisRepository extends CrudRepository<SchoolEntity, String> {
    String HASH_KEY = "School";

    Optional<SchoolEntity> findByMincode(String mincode);
    List<SchoolEntity> findAllByDistrictIdAndMincode(String districtId, String mincode);
    List<SchoolEntity> findAllByDistrictId(String districtId);
}