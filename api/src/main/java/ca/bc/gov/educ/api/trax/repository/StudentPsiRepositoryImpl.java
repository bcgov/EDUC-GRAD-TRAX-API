package ca.bc.gov.educ.api.trax.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Custom repository implementation for Student PSI queries with dynamic table name support.
 * This allows the table name to be configured via environment variable.
 * Spring Data JPA will automatically use this implementation for methods defined in StudentPsiRepositoryCustom.
 */
@Repository
public class StudentPsiRepositoryImpl implements StudentPsiRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Value("${trax.psi.table-name:TAB_POSTSEC}")
    private String psiTableName;
    
    @Override
    public List<Object[]> findStudentsUsingPSI(String transmissionMode, String psiYear, String psiCodeProvided, List<String> psiCode) {
        String queryString = "SELECT sp.* FROM STUD_PSI sp INNER JOIN " + psiTableName + " tab ON tab.PSI_CODE=sp.PSI_CODE WHERE tab.TRANSMISSION_MODE = :transmissionMode AND sp.PSI_YEAR=:psiYear AND (:psiCodeProvided is null or tab.PSI_CODE in :psiCode)";
        
        Query query = entityManager.createNativeQuery(queryString);
        query.setParameter("transmissionMode", transmissionMode);
        query.setParameter("psiYear", psiYear);
        query.setParameter("psiCodeProvided", psiCodeProvided);
        query.setParameter("psiCode", psiCode);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return results;
    }
}
