package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.PsiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PsiRepository extends JpaRepository<PsiEntity, String> {

    @Query(value="SELECT si.* FROM TAB_POSTSEC si where "
			+ "(:psiName is null or si.PSI_NAME like %:psiName%) and ROWNUM <= 50",nativeQuery = true)	
	List<PsiEntity> searchForPSI(String psiName);

    @Query(value="SELECT si.* FROM TAB_POSTSEC si where "
            + "(:psiName is null or (si.PSI_NAME like %:psiName%)) and "
            + "(:psiCode is null or (si.PSI_CODE like :psiCode%)) and "
            + "(:cslCode is null or (si.PSI_CSL_CODE like :cslCode%)) and "
            + "(:transmissionMode is null or (si.TRANSMISSION_MODE like :transmissionMode%)) and "
            + "(:psiGrouping is null or (si.PSI_GROUPING like :psiGrouping%)) and "
            + "(:openFlag is null or si.OPEN_FLAG =:openFlag)",nativeQuery = true)
    List<PsiEntity> findPSIs(String psiCode, String psiName, String cslCode,String transmissionMode, String openFlag, String psiGrouping);

}
