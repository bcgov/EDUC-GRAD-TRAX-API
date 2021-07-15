package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.SchoolEntity;
import ca.bc.gov.educ.api.trax.util.criteria.CriteriaQueryRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolCriteriaQueryRepository extends CriteriaQueryRepository<SchoolEntity> {

}
