package ca.bc.gov.educ.api.trax.repository;

import ca.bc.gov.educ.api.trax.model.entity.TranscriptStudentDemogEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * The interface TSW Student Demog repository.
 */
@Repository
public interface TranscriptStudentDemogRepository extends CrudRepository<TranscriptStudentDemogEntity, String> {

}
