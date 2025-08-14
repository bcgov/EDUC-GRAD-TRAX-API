package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.StudentPsiEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentPsiRepository extends JpaRepository<StudentPsiEntity, String> {
    @Query(value = "SELECT sp.* FROM STUD_PSI sp INNER JOIN ISD_PSI_REGISTRY tab ON tab.PSI_CODE=sp.PSI_CODE WHERE tab.TRANSMISSION_MODE = :transmissionMode AND sp.PSI_YEAR=:psiYear AND (:psiCodeProvided is null or tab.PSI_CODE in :psiCode)",nativeQuery = true)
    List<Object[]> findStudentsUsingPSI(String transmissionMode, String psiYear, String psiCodeProvided, List<String> psiCode);
}