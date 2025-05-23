package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.PsiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PsiRepository extends JpaRepository<PsiEntity, String>, JpaSpecificationExecutor<PsiEntity> {

    @Query(value="SELECT si.* FROM ISD_PSI_REGISTRY si where "
			+ "(:psiName is null or si.PSI_NAME like %:psiName%) and ROWNUM <= 50",nativeQuery = true)
	List<PsiEntity> searchForPSI(String psiName);

}
