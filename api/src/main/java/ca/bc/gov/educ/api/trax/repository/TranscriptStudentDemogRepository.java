package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.TranscriptStudentDemogEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * The interface TRAX Student repository.
 */
@Repository
public interface TranscriptStudentDemogRepository extends CrudRepository<TranscriptStudentDemogEntity, String> {

    @Query(value = "SELECT COUNT(*) FROM TSW_TRAN_DEMOG WHERE stud_no = :pen AND grad_date <> 0", nativeQuery = true)
    Integer countGradDateByPen(@Param("pen")String pen);
}
