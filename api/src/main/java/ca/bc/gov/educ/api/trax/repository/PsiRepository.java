package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.PsiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PsiRepository extends JpaRepository<PsiEntity, String>, JpaSpecificationExecutor<PsiEntity>, PsiRepositoryCustom {

}
