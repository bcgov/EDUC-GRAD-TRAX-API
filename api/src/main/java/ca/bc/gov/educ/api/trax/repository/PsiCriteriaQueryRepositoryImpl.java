package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.PsiEntity;
import ca.bc.gov.educ.api.trax.util.criteria.CriteriaQueryRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class PsiCriteriaQueryRepositoryImpl extends CriteriaQueryRepositoryImpl<PsiEntity> implements PsiCriteriaQueryRepository {

}
