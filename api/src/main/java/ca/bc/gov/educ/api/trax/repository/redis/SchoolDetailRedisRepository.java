package ca.bc.gov.educ.api.trax.repository.redis;

import ca.bc.gov.educ.api.trax.model.entity.institute.SchoolDetailEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchoolDetailRedisRepository extends CrudRepository<SchoolDetailEntity, String> {
    String HASH_KEY = "SchoolDetail";

    List<SchoolDetailEntity> findBySchoolCategoryCode(String schoolCategoryCode);

    SchoolDetailEntity findByMincode(String mincode);
}