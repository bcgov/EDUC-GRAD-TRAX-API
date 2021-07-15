package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.SchoolEntity;
import ca.bc.gov.educ.api.trax.util.criteria.CriteriaQueryRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class SchoolCriteriaQueryRepositoryImpl extends CriteriaQueryRepositoryImpl<SchoolEntity> implements SchoolCriteriaQueryRepository {

}
