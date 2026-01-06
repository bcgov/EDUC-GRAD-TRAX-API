package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.PsiEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Custom repository implementation for PSI search with dynamic table name support.
 * This allows the table name to be configured via environment variable.
 * Spring Data JPA will automatically use this implementation for methods defined in PsiRepositoryCustom.
 */
@Repository
public class PsiRepositoryImpl implements PsiRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Value("${trax.psi.table-name:TAB_POSTSEC}")
    private String psiTableName;
    
    @Override
    public List<PsiEntity> searchForPSI(String psiName) {
        String queryString = "SELECT si.* FROM " + psiTableName + " si where " +
                "(:psiName is null or si.PSI_NAME like '%' || :psiName || '%') and ROWNUM <= 50";
        
        Query query = entityManager.createNativeQuery(queryString, PsiEntity.class);
        query.setParameter("psiName", psiName);

        @SuppressWarnings("unchecked")
        List<PsiEntity> results = query.getResultList();
        return results;
    }
}

