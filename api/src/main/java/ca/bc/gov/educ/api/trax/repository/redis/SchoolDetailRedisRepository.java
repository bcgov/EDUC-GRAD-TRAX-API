package ca.bc.gov.educ.api.trax.repository.redis;

import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolDetailEntity;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchoolDetailRedisRepository extends KeyValueRepository<SchoolDetailEntity, String> {
    String HASH_KEY = "SchoolDetail";

    List<SchoolDetailEntity> findBySchoolCategoryCode(String schoolCategoryCode);

    List<SchoolDetailEntity> findByDistrictId(String districtId);

    Optional<SchoolDetailEntity> findByMincode(String mincode);
}